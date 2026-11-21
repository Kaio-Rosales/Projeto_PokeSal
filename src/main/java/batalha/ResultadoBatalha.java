package batalha;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.jogador.Jogador;
import model.pokesal.PokeSal;

/**
 * Registro imutável do desfecho de uma batalha entre treinadores: é o que o
 * {@link RepositorioResultados} guarda.
 */
public class ResultadoBatalha {

  private final Jogador vencedor;
  private final Jogador perdedor;
  private final int turnos;
  private final List<PokeSal> pokesalUtilizadosVencedor;
  private final List<PokeSal> pokesalUtilizadosPerdedor;

  /** Cria o resultado; as listas são copiadas para que ninguém as altere depois. */
  public ResultadoBatalha(Jogador vencedor, Jogador perdedor, int turnos,
      List<PokeSal> pokesalUtilizadosVencedor, List<PokeSal> pokesalUtilizadosPerdedor) {
    this.vencedor = vencedor;
    this.perdedor = perdedor;
    this.turnos = turnos;
    this.pokesalUtilizadosVencedor =
        Collections.unmodifiableList(new ArrayList<>(pokesalUtilizadosVencedor));
    this.pokesalUtilizadosPerdedor =
        Collections.unmodifiableList(new ArrayList<>(pokesalUtilizadosPerdedor));
  }

  public Jogador getVencedor() {
    return vencedor;
  }

  public Jogador getPerdedor() {
    return perdedor;
  }

  public int getTurnos() {
    return turnos;
  }

  public List<PokeSal> getPokesalUtilizadosVencedor() {
    return pokesalUtilizadosVencedor;
  }

  public List<PokeSal> getPokesalUtilizadosPerdedor() {
    return pokesalUtilizadosPerdedor;
  }
}
