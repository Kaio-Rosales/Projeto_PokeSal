package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import aleatorio.GeradorAleatorioPadrao;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Teste do adaptador de produção. Sem Mockito: um {@code Random} com SEMENTE fixa já é
 * determinístico, então não há o que mockar.
 */
class GeradorAleatorioPadraoTest {

  @Test
  void testRepassaOsSorteiosDoRandomRecebido() {
    GeradorAleatorioPadrao gerador = new GeradorAleatorioPadrao(new Random(42));
    Random referencia = new Random(42);

    for (int i = 0; i < 20; i++) {
      assertEquals(referencia.nextInt(100), gerador.proximoInt(100));
    }
  }

  @Test
  void testSorteioRespeitaOsLimites() {
    GeradorAleatorioPadrao gerador = new GeradorAleatorioPadrao(new Random(7));

    for (int i = 0; i < 500; i++) {
      int valor = gerador.proximoInt(5);
      assertTrue(valor >= 0 && valor < 5, "valor fora de [0, 5): " + valor);
    }
  }

  @Test
  void testLimiteInvalidoLancadoPeloRandom() {
    GeradorAleatorioPadrao gerador = new GeradorAleatorioPadrao(new Random(1));
    assertThrows(IllegalArgumentException.class, () -> gerador.proximoInt(0));
  }

  @Test
  void testRandomObrigatorio() {
    assertThrows(NullPointerException.class, () -> new GeradorAleatorioPadrao(null));
  }
}
