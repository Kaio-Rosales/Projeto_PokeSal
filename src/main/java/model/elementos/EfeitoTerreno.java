package model.elementos;

import model.pokesal.PokeSal;

/**
 * Efeito que o terreno exerce sobre a batalha.
 *
 * <p>FASE 03: esta enumeração substitui a antiga classe {@code Terrenos}, que guardava o estado
 * em campos {@code static} (três booleanos globais). Estado global é ruim para testes: um teste
 * "vaza" o terreno para o próximo. Aqui cada constante é imutável e só contém REGRAS
 * (multiplicador e cura). Quem decide QUAL efeito está ativo é o {@code ServicoTerreno}.
 */
public enum EfeitoTerreno {
  NENHUM(""),
  ASFALTO_QUENTE("O sol castiga aqueles que permanecem desprotegidos!\nO dia está quente!"),
  CHAO_MOLHADO("A chuva deixou sua marca!\nO chão está molhado!"),
  TERRENO_FLORIDO("A vida desabrocha, com ou sem o homem!\nAs plantas tomam o terreno!");

  private static final double BONUS_FOGO_ASFALTO_QUENTE = 1.15;
  private static final double BONUS_AGUA_CHAO_MOLHADO = 1.10;
  private static final int PERCENTUAL_CURA_FLORIDO = 5;

  private final String mensagem;

  EfeitoTerreno(String mensagem) {
    this.mensagem = mensagem;
  }

  /** Texto exibido ao jogador quando este terreno entra em cena (vazio para NENHUM). */
  public String getMensagem() {
    return mensagem;
  }

  /** Multiplicador de dano do golpe: Asfalto Quente +15% para Fogo, Chão Molhado +10% para Água. */
  public double multiplicadorDano(TipoElemental tipoGolpe) {
    if (this == ASFALTO_QUENTE && tipoGolpe == TipoElemental.FOGO) {
      return BONUS_FOGO_ASFALTO_QUENTE;
    }
    if (this == CHAO_MOLHADO && tipoGolpe == TipoElemental.AGUA) {
      return BONUS_AGUA_CHAO_MOLHADO;
    }
    return 1.0;
  }

  /**
   * Quantidade de HP que o terreno devolve ao PokeSal no fim do turno.
   *
   * <p>Só o Terreno Florido cura: 5% do HP máximo (mínimo 1), apenas para PokeSal vivo do tipo
   * Grama, sem ultrapassar o HP máximo. Este método apenas CALCULA; quem altera o HP é a
   * {@code MecanicaCombate} (assim a regra fica testável sem efeitos colaterais).
   */
  public int calcularCura(PokeSal pokeSal) {
    if (this != TERRENO_FLORIDO
        || pokeSal.getElemento() != TipoElemental.GRAMA
        || !pokeSal.estaVivo()) {
      return 0;
    }
    int cura = Math.max(1, pokeSal.getHpMax() * PERCENTUAL_CURA_FLORIDO / 100);
    int novoHp = Math.min(pokeSal.getHpMax(), pokeSal.getHpAtual() + cura);
    return Math.max(0, novoHp - pokeSal.getHpAtual());
  }
}
