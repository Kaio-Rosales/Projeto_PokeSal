package torneio;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

/** Notificador de produção: escreve o anúncio do campeão no console. */
public class NotificadorTorneioConsole implements NotificadorTorneio {

  private final PrintStream saida;

  /** Recebe o fluxo de saída ({@code System.out} em produção). */
  public NotificadorTorneioConsole(PrintStream saida) {
    this.saida = Objects.requireNonNull(saida, "saida");
  }

  @Override
  public void notificarCampeao(String nomeCampeao, List<String> premios) {
    saida.println("*** CAMPEÃO DO TORNEIO: " + nomeCampeao + " ***");
    saida.println("Prêmios: " + String.join(", ", premios));
  }
}
