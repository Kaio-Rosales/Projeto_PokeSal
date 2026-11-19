package tests;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import torneio.NotificadorTorneioConsole;

/** O notificador de console escreve o anúncio do campeão na saída recebida. */
class NotificadorTorneioConsoleTest {

  @Test
  void testAnunciaCampeaoPremios() {
    ByteArrayOutputStream saida = new ByteArrayOutputStream();
    NotificadorTorneioConsole notificador =
        new NotificadorTorneioConsole(new PrintStream(saida, true, StandardCharsets.UTF_8));

    notificador.notificarCampeao("Ana", Arrays.asList("1x Super Poção", "1x Antidoto"));

    String texto = new String(saida.toByteArray(), StandardCharsets.UTF_8);
    assertTrue(texto.contains("CAMPEÃO DO TORNEIO: Ana"));
    assertTrue(texto.contains("Prêmios: 1x Super Poção, 1x Antidoto"));
  }

  @Test
  void testSaidaObrigatoria() {
    assertThrows(NullPointerException.class, () -> new NotificadorTorneioConsole(null));
  }
}
