package batalha;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import model.elementos.EfeitoTerreno;
import model.itens.Item;
import model.itens.ItemCurativo;
import model.jogador.Jogador;
import model.pokesal.PokeSal;
import registro.RegistroBatalha;
import terreno.ServicoTerreno;

/**
 * Batalha AUTOMÁTICA entre dois treinadores (usada no torneio). Cada objeto representa UMA
 * batalha: guarda o número de turnos, os PokeSals que já entraram em campo e, quando acaba, o
 * resultado.
 *
 * <p>Um turno acontece em fases, sempre nesta ordem (requisito 2.4):
 * <ol>
 *   <li>GOLPES: quem tem mais velocidade ataca primeiro (evento de ataque, dano e, se houver,
 *       status aplicado);</li>
 *   <li>STATUS de fim de turno (queimadura, veneno, resfriado);</li>
 *   <li>TERRENO de fim de turno (cura do Terreno Florido);</li>
 *   <li>ITENS: o treinador usa uma poção se o PokeSal ativo estiver com 30% de HP ou menos.</li>
 * </ol>
 *
 * <p>Quando um treinador fica sem PokeSal vivo, a batalha termina e o resultado é salvo no
 * {@link RepositorioResultados}. Se o repositório falhar, o erro é registrado e o resultado
 * continua válido (requisito 2.3). Se a batalha NÃO terminou, o repositório não é chamado.
 */
public class Batalha {

  /** Segurança contra batalhas infinitas (ex.: dois PokeSals que só erram golpes). */
  public static final int LIMITE_TURNOS_PADRAO = 200;
  static final int LIMITE_ITENS_POR_BATALHA = 2;
  static final int PERCENTUAL_HP_PARA_USAR_ITEM = 30;

  /** Agrupa o que a batalha precisa lembrar sobre cada treinador (evita código duplicado A/B). */
  private static final class Lado {
    private final Jogador treinador;
    private final Set<PokeSal> utilizados = new LinkedHashSet<>();
    private int itensUsados;

    private Lado(Jogador treinador) {
      this.treinador = treinador;
    }

    private int hpTotal() {
      int total = 0;
      for (PokeSal pokeSal : treinador.getTime()) {
        total += pokeSal.getHpAtual();
      }
      return total;
    }
  }

  private final Lado ladoA;
  private final Lado ladoB;
  private final MecanicaCombate mecanica;
  private final ServicoTerreno servicoTerreno;
  private final RepositorioResultados repositorio;
  private final RegistroBatalha registro;
  private final int limiteTurnos;

  private EfeitoTerreno terreno;
  private int turnos;
  private ResultadoBatalha resultado;

  /** Cria uma batalha com o limite de turnos padrão. */
  public Batalha(Jogador treinadorA, Jogador treinadorB, MecanicaCombate mecanica,
      ServicoTerreno servicoTerreno, RepositorioResultados repositorio, RegistroBatalha registro) {
    this(treinadorA, treinadorB, mecanica, servicoTerreno, repositorio, registro,
        LIMITE_TURNOS_PADRAO);
  }

  /**
   * Cria uma batalha.
   *
   * @throws IllegalArgumentException se algum treinador for nulo, se forem o mesmo treinador, se
   *     algum não tiver PokeSal vivo ou se o limite de turnos for menor que 1
   */
  public Batalha(Jogador treinadorA, Jogador treinadorB, MecanicaCombate mecanica,
      ServicoTerreno servicoTerreno, RepositorioResultados repositorio, RegistroBatalha registro,
      int limiteTurnos) {
    validarTreinadores(treinadorA, treinadorB);
    if (limiteTurnos < 1) {
      throw new IllegalArgumentException("O limite de turnos deve ser pelo menos 1");
    }
    this.ladoA = new Lado(treinadorA);
    this.ladoB = new Lado(treinadorB);
    this.mecanica = Objects.requireNonNull(mecanica, "mecanica");
    this.servicoTerreno = Objects.requireNonNull(servicoTerreno, "servicoTerreno");
    this.repositorio = Objects.requireNonNull(repositorio, "repositorio");
    this.registro = Objects.requireNonNull(registro, "registro");
    this.limiteTurnos = limiteTurnos;
  }

  private static void validarTreinadores(Jogador a, Jogador b) {
    if (a == null || b == null) {
      throw new IllegalArgumentException("Os dois treinadores são obrigatórios");
    }
    if (a == b) {
      throw new IllegalArgumentException("Um treinador não pode lutar contra si mesmo");
    }
    if (!a.possuiPokeSalVivo() || !b.possuiPokeSalVivo()) {
      throw new IllegalArgumentException("Os dois treinadores precisam de um PokeSal vivo");
    }
  }

  /** Indica se a batalha já tem um vencedor. */
  public boolean terminou() {
    return resultado != null;
  }

  /** Resultado da batalha, ou {@code null} enquanto ela não terminou. */
  public ResultadoBatalha getResultado() {
    return resultado;
  }

  public int getTurnos() {
    return turnos;
  }

  /** Executa turnos até a batalha terminar e devolve o resultado. */
  public ResultadoBatalha executarAteOFim() {
    while (!terminou()) {
      executarTurno();
    }
    return resultado;
  }

  /**
   * Executa um único turno. Se o turno encerrar a batalha, o resultado é salvo.
   *
   * @throws IllegalStateException se a batalha já terminou
   */
  public void executarTurno() {
    if (terminou()) {
      throw new IllegalStateException("A batalha já terminou");
    }
    if (terreno == null) {
      registro.registrarInicioBatalha(ladoA.treinador.getNome(), ladoB.treinador.getNome());
      terreno = servicoTerreno.obterEfeitoAtual();
    }
    turnos++;
    PokeSal pokeSalA = entrarEmCampo(ladoA);
    PokeSal pokeSalB = entrarEmCampo(ladoB);

    executarFaseDeGolpes(pokeSalA, pokeSalB);
    executarFaseDeStatus(pokeSalA, pokeSalB);
    executarFaseDeTerreno(pokeSalA, pokeSalB);
    executarFaseDeItens(pokeSalA, pokeSalB);

    verificarFim();
  }

  private PokeSal entrarEmCampo(Lado lado) {
    PokeSal ativo = lado.treinador.getPrimeiroPokeSalVivo();
    lado.utilizados.add(ativo);
    return ativo;
  }

  private void executarFaseDeGolpes(PokeSal pokeSalA, PokeSal pokeSalB) {
    PokeSal primeiro = mecanica.atacaPrimeiro(pokeSalA, pokeSalB) ? pokeSalA : pokeSalB;
    PokeSal segundo = primeiro == pokeSalA ? pokeSalB : pokeSalA;
    mecanica.executarAtaque(primeiro, segundo, mecanica.escolherAtaqueIa(primeiro), terreno);
    if (segundo.estaVivo()) {
      mecanica.executarAtaque(segundo, primeiro, mecanica.escolherAtaqueIa(segundo), terreno);
    }
  }

  private void executarFaseDeStatus(PokeSal pokeSalA, PokeSal pokeSalB) {
    if (pokeSalA.estaVivo()) {
      mecanica.aplicarStatusFimDeTurno(pokeSalA);
    }
    if (pokeSalB.estaVivo()) {
      mecanica.aplicarStatusFimDeTurno(pokeSalB);
    }
  }

  private void executarFaseDeTerreno(PokeSal pokeSalA, PokeSal pokeSalB) {
    if (pokeSalA.estaVivo()) {
      mecanica.aplicarTerrenoFimDeTurno(pokeSalA, terreno);
    }
    if (pokeSalB.estaVivo()) {
      mecanica.aplicarTerrenoFimDeTurno(pokeSalB, terreno);
    }
  }

  private void executarFaseDeItens(PokeSal pokeSalA, PokeSal pokeSalB) {
    usarItemSeNecessario(ladoA, pokeSalA);
    usarItemSeNecessario(ladoB, pokeSalB);
  }

  /** IA simples: com 30% de HP ou menos, usa a primeira poção disponível (máx. 2 por batalha). */
  private void usarItemSeNecessario(Lado lado, PokeSal ativo) {
    if (!ativo.estaVivo() || lado.itensUsados >= LIMITE_ITENS_POR_BATALHA) {
      return;
    }
    if (ativo.getHpAtual() * 100 > ativo.getHpMax() * PERCENTUAL_HP_PARA_USAR_ITEM) {
      return;
    }
    for (Item item : lado.treinador.getMochila()) {
      if (item instanceof ItemCurativo && item.getQuantidade() > 0) {
        ((ItemCurativo) item).usar(ativo);
        lado.itensUsados++;
        registro.registrarUsoItem(item, ativo);
        return;
      }
    }
  }

  private void verificarFim() {
    boolean aVivo = ladoA.treinador.possuiPokeSalVivo();
    boolean bVivo = ladoB.treinador.possuiPokeSalVivo();
    if (aVivo && bVivo) {
      if (turnos >= limiteTurnos) {
        // Tempo esgotado: vence quem tiver mais HP somado no time (empate: treinador A).
        finalizar(ladoA.hpTotal() >= ladoB.hpTotal() ? ladoA : ladoB);
      }
      return;
    }
    // Só A vivo -> A. Só B vivo -> B. Ambos desmaiaram no mesmo turno -> A (cabeça de chave).
    finalizar(aVivo || !bVivo ? ladoA : ladoB);
  }

  private void finalizar(Lado vencedor) {
    Lado perdedor = vencedor == ladoA ? ladoB : ladoA;
    resultado = new ResultadoBatalha(vencedor.treinador, perdedor.treinador, turnos,
        new ArrayList<>(vencedor.utilizados), new ArrayList<>(perdedor.utilizados));
    salvarResultado();
  }

  /**
   * Falha no repositório NÃO invalida a batalha: registramos o erro e seguimos. Capturamos
   * {@code RuntimeException} de propósito, pois qualquer implementação de repositório (arquivo,
   * banco, rede) pode lançar exceções diferentes e nenhuma delas pode derrubar o torneio.
   */
  private void salvarResultado() {
    try {
      repositorio.salvar(resultado);
    } catch (RuntimeException falha) {
      registro.registrarErro("Não foi possível salvar o resultado da batalha entre "
          + ladoA.treinador.getNome() + " e " + ladoB.treinador.getNome()
          + "; o torneio continua", falha);
    }
  }
}
