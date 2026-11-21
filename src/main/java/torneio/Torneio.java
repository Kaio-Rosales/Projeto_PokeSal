package torneio;

import batalha.ExecutorBatalha;
import batalha.ResultadoBatalha;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import model.jogador.Jogador;

/**
 * Torneio ELIMINATÓRIO com 4 ou 8 treinadores. Os treinadores se enfrentam em duplas, na ordem
 * da lista (1º x 2º, 3º x 4º...), e só os vencedores avançam, até sobrar o campeão.
 *
 * <p>O torneio NÃO sabe lutar: ele delega cada confronto a um {@link ExecutorBatalha}. Isso o
 * deixa testável (o executor é mockado) e respeita a Inversão de Dependência.
 */
public class Torneio {

  private static final int MENOR_TORNEIO = 4;
  private static final int MAIOR_TORNEIO = 8;

  private final ExecutorBatalha executor;
  private final List<ResultadoBatalha> historico = new ArrayList<>();

  /** Recebe quem disputa cada batalha. */
  public Torneio(ExecutorBatalha executor) {
    this.executor = Objects.requireNonNull(executor, "executor");
  }

  /** Um torneio só pode ter 4 ou 8 participantes. */
  public static boolean tamanhoValido(int quantidade) {
    return quantidade == MENOR_TORNEIO || quantidade == MAIOR_TORNEIO;
  }

  /**
   * Disputa o torneio e devolve o campeão. Antes de cada luta os dois times são curados (o
   * "Centro PokeSal" entre as rodadas), para que o cansaço de uma rodada não decida a seguinte.
   *
   * @throws IllegalArgumentException se não houver 4 ou 8 participantes, ou se houver nulo
   */
  public Jogador disputar(List<Jogador> participantes) {
    if (participantes == null || !tamanhoValido(participantes.size())) {
      throw new IllegalArgumentException("O torneio exige 4 ou 8 treinadores");
    }
    if (participantes.contains(null)) {
      throw new IllegalArgumentException("Não pode haver treinador nulo no torneio");
    }
    if (temNomesRepetidos(participantes)) {
      throw new IllegalArgumentException("Os treinadores precisam ter nomes diferentes");
    }

    List<Jogador> rodada = new ArrayList<>(participantes);
    while (rodada.size() > 1) {
      List<Jogador> vencedores = new ArrayList<>();
      for (int i = 0; i < rodada.size(); i += 2) {
        vencedores.add(lutar(rodada.get(i), rodada.get(i + 1)));
      }
      rodada = vencedores;
    }
    return rodada.get(0);
  }

  /** O ranking identifica o treinador pelo nome; por isso nomes repetidos são proibidos. */
  private static boolean temNomesRepetidos(List<Jogador> participantes) {
    Set<String> nomes = new HashSet<>();
    for (Jogador participante : participantes) {
      if (!nomes.add(participante.getNome().toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private Jogador lutar(Jogador a, Jogador b) {
    a.curarTime();
    b.curarTime();
    ResultadoBatalha resultado = executor.lutar(a, b);
    Jogador vencedor = resultado.getVencedor();
    if (vencedor != a && vencedor != b) {
      throw new IllegalStateException("O vencedor informado não participou da batalha");
    }
    historico.add(resultado);
    return vencedor;
  }

  /** Resultados de todas as batalhas já disputadas, na ordem em que aconteceram. */
  public List<ResultadoBatalha> getHistorico() {
    return Collections.unmodifiableList(historico);
  }
}
