package batalha;

import model.jogador.Jogador;

/**
 * Sabe disputar uma batalha completa entre dois treinadores. O {@code Torneio} depende desta
 * interface (e não de {@link Batalha}), então nos testes do chaveamento basta mockar o resultado
 * de cada luta, sem simular golpe por golpe.
 */
public interface ExecutorBatalha {

  /** Luta até o fim e devolve o resultado (já salvo no repositório). */
  ResultadoBatalha lutar(Jogador treinadorA, Jogador treinadorB);
}
