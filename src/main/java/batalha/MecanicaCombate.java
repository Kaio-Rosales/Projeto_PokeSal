package batalha;

import aleatorio.GeradorAleatorio;
import java.util.List;
import java.util.Objects;
import model.elementos.EfeitoTerreno;
import model.elementos.StatusEfeito;
import model.elementos.TipoElemental;
import model.pokesal.Ataque;
import model.pokesal.PokeSal;
import registro.RegistroBatalha;

/**
 * Regras de combate entre dois PokeSals: precisão, crítico, dano, status e terreno.
 *
 * <p>FASE 03: estas regras estavam todas dentro da classe {@code Servicos} (que também lia o
 * teclado, desenhava o mapa e sorteava itens). Foram extraídas para cá por três motivos:
 * (1) Responsabilidade Única: esta classe só conhece COMBATE; (2) a batalha selvagem interativa e
 * a batalha automática do torneio usam o MESMO código, sem duplicação; (3) as duas dependências
 * externas (sorteio e registro) chegam pelo construtor, então um teste injeta mocks.
 *
 * <p>Ordem das chamadas ao gerador dentro de {@link #executarAtaque}: primeiro a PRECISÃO
 * ({@code proximoInt(100)}); se errar, nada mais é sorteado. Depois o CRÍTICO
 * ({@code proximoInt(10000)}) e, por fim, a chance do STATUS do golpe ({@code proximoInt(100)}).
 */
public class MecanicaCombate {

  /** Os valores sorteados para precisão e status vão de 0 a 99 (um por cento cada). */
  static final int ESCALA_PERCENTUAL = 100;
  /** Para o crítico usamos 10000 posições, pois 6,25% = 625 de 10000 (exato, sem arredondar). */
  static final int ESCALA_CRITICO = 10_000;
  /** Sorteio abaixo de 625 (de 0 a 9999) é acerto crítico: chance de 6,25%. */
  static final int FAIXA_CRITICO = 625;
  static final double MULTIPLICADOR_CRITICO = 1.5;
  private static final int CHANCE_CURA_RESFRIADO = 50;

  private final GeradorAleatorio gerador;
  private final RegistroBatalha registro;

  /** Recebe os colaboradores externos (nunca os cria). */
  public MecanicaCombate(GeradorAleatorio gerador, RegistroBatalha registro) {
    this.gerador = Objects.requireNonNull(gerador, "gerador");
    this.registro = Objects.requireNonNull(registro, "registro");
  }

  /**
   * Executa um ataque completo: precisão, crítico, dano, desmaio e chance de status.
   * Avisa o {@link RegistroBatalha} na ordem: ataque, dano, efeito de status.
   */
  public ResultadoAtaque executarAtaque(PokeSal atacante, PokeSal alvo, Ataque ataque,
      EfeitoTerreno terreno) {
    if (atacante.getStatus() == StatusEfeito.RESFRIADO) {
      registro.registrarEfeitoStatus(atacante, StatusEfeito.RESFRIADO,
          atacante.getClass().getSimpleName() + " está resfriado e não conseguiu agir!");
      return ResultadoAtaque.semAcao();
    }

    boolean acertou = gerador.proximoInt(ESCALA_PERCENTUAL) < ataque.getPrecisao();
    registro.registrarAtaque(atacante, ataque, acertou);
    if (!acertou) {
      return ResultadoAtaque.errou();
    }

    boolean critico = gerador.proximoInt(ESCALA_CRITICO) < FAIXA_CRITICO;
    double efetividade = ataque.getTipo().danoElemental(ataque.getTipo(), alvo.getElemento());
    int dano = calcularDano(atacante, alvo, ataque, terreno);
    if (critico) {
      dano = (int) Math.round(dano * MULTIPLICADOR_CRITICO);
    }

    alvo.setHpAtual(Math.max(0, alvo.getHpAtual() - dano));
    registro.registrarDano(alvo, dano, critico, efetividade);

    StatusEfeito statusAplicado = StatusEfeito.NENHUM;
    if (alvo.estaVivo()) {
      statusAplicado = tentarAplicarStatus(alvo, ataque);
    }
    return ResultadoAtaque.acertou(dano, critico, statusAplicado);
  }

  /**
   * Dano de um golpe que ACERTOU e NÃO é crítico. Não sorteia nada, por isso é determinístico.
   * Fórmula (igual à Fase 02): arredonda(danoBruto x efetividade x terreno) - DEF/10, mínimo 1.
   */
  public int calcularDano(PokeSal atacante, PokeSal alvo, Ataque ataque, EfeitoTerreno terreno) {
    TipoElemental tipoGolpe = ataque.getTipo();
    double efetividade = tipoGolpe.danoElemental(tipoGolpe, alvo.getElemento());
    double multiplicadorTerreno = terreno.multiplicadorDano(tipoGolpe);
    int danoBruto = ataque.calcularDano(atacante.getAtkEfetivo());
    int danoComBonus = (int) Math.round(danoBruto * efetividade * multiplicadorTerreno);
    return Math.max(1, danoComBonus - (alvo.getDef() / 10));
  }

  /**
   * Aplica dano ou cura de status ao final do turno (Queimado, Envenenado, Resfriado).
   * Paralisado apenas reduz a velocidade efetiva e não causa dano por turno.
   */
  public void aplicarStatusFimDeTurno(PokeSal pokeSal) {
    StatusEfeito status = pokeSal.getStatus();
    if (status == StatusEfeito.QUEIMADO) {
      int dano = Math.max(1, pokeSal.getHpMax() * 2 / 100);
      pokeSal.setHpAtual(Math.max(0, pokeSal.getHpAtual() - dano));
      registro.registrarEfeitoStatus(pokeSal, status,
          pokeSal.getClass().getSimpleName() + " sofreu " + dano + " de dano pela queimadura!");
    } else if (status == StatusEfeito.ENVENENADO) {
      int dano = Math.max(1, pokeSal.getHpMax() * 5 / 100);
      pokeSal.setHpAtual(Math.max(0, pokeSal.getHpAtual() - dano));
      registro.registrarEfeitoStatus(pokeSal, status,
          pokeSal.getClass().getSimpleName() + " sofreu " + dano + " de dano pelo veneno!");
    } else if (status == StatusEfeito.RESFRIADO
        && gerador.proximoInt(ESCALA_PERCENTUAL) < CHANCE_CURA_RESFRIADO) {
      pokeSal.setStatus(StatusEfeito.NENHUM);
      registro.registrarEfeitoStatus(pokeSal, StatusEfeito.RESFRIADO,
          pokeSal.getClass().getSimpleName() + " não está mais resfriado!");
    }
  }

  /** Aplica o efeito do terreno (cura do Terreno Florido) ao final do turno. */
  public void aplicarTerrenoFimDeTurno(PokeSal pokeSal, EfeitoTerreno terreno) {
    int cura = terreno.calcularCura(pokeSal);
    if (cura > 0) {
      pokeSal.setHpAtual(pokeSal.getHpAtual() + cura);
      registro.registrarEfeitoTerreno(pokeSal, terreno,
          pokeSal.getClass().getSimpleName() + " recuperou " + cura + ""
                 + " de HP graças ao terreno florido!");
    }
  }

  /**
   * Determina se o primeiro PokeSal age antes do segundo, pela velocidade efetiva (já com a
   * penalidade de Paralisado). Em caso de empate, o primeiro age primeiro.
   */
  public boolean atacaPrimeiro(PokeSal primeiro, PokeSal segundo) {
    return primeiro.getSpdEfetivo() >= segundo.getSpdEfetivo();
  }

  /** Escolhe, por sorteio, um dos golpes disponíveis. Usado pela IA (oponente e treinadores). */
  public Ataque escolherAtaqueIa(PokeSal atacante) {
    List<Ataque> disponiveis = atacante.ataques();
    return disponiveis.get(gerador.proximoInt(disponiveis.size()));
  }

  private StatusEfeito tentarAplicarStatus(PokeSal alvo, Ataque ataque) {
    if (ataque.getEfeito() == StatusEfeito.NENHUM || alvo.getStatus() != StatusEfeito.NENHUM) {
      return StatusEfeito.NENHUM;
    }
    if (gerador.proximoInt(ESCALA_PERCENTUAL) < ataque.getChanceEfeito()) {
      alvo.setStatus(ataque.getEfeito());
      registro.registrarEfeitoStatus(alvo, ataque.getEfeito(),
          alvo.getClass().getSimpleName() + " foi afetado por " + ataque.getEfeito() + "!");
      return ataque.getEfeito();
    }
    return StatusEfeito.NENHUM;
  }
}
