package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import model.elementos.StatusEfeito;
import model.elementos.TipoElemental;
import model.pokesal.Ataque;
import org.junit.jupiter.api.Test;

/** Testes do atributo de PRECISÃO (0 a 100) acrescentado ao golpe na Fase 03. */
class AtaqueTest {

  @Test
  void testConstrutoresAntigosContinuamComPrecisaoCem() {
    Ataque simples = new Ataque("A", TipoElemental.NORMAL, 10, 5, 1);
    Ataque comStatus = new Ataque("B", TipoElemental.FOGO, 10, 5, 1, StatusEfeito.QUEIMADO, 30);

    assertEquals(100, simples.getPrecisao());
    assertEquals(100, comStatus.getPrecisao());
    assertEquals(StatusEfeito.QUEIMADO, comStatus.getEfeito());
    assertEquals(30, comStatus.getChanceEfeito());
  }

  @Test
  void testPrecisaoInformadaGuardada() {
    assertEquals(80, new Ataque("A", TipoElemental.AGUA, 1, 1, 1, 80).getPrecisao());
    Ataque completo = new Ataque("B", TipoElemental.AGUA, 1, 1, 1, 85, StatusEfeito.RESFRIADO, 20);
    assertEquals(85, completo.getPrecisao());
    assertEquals(StatusEfeito.RESFRIADO, completo.getEfeito());
  }

  @Test
  void testPrecisaoNosLimitesAceitaForaDelesLancadaExcecao() {
    assertEquals(0, new Ataque("A", TipoElemental.NORMAL, 1, 1, 1, 0).getPrecisao());
    assertEquals(100, new Ataque("A", TipoElemental.NORMAL, 1, 1, 1, 100).getPrecisao());

    assertThrows(IllegalArgumentException.class,
        () -> new Ataque("A", TipoElemental.NORMAL, 1, 1, 1, -1));
    assertThrows(IllegalArgumentException.class,
        () -> new Ataque("A", TipoElemental.NORMAL, 1, 1, 1, 101));
  }

  @Test
  void testNivelMinimoDanoBruto() {
    Ataque ataque = new Ataque("A", TipoElemental.NORMAL, 10, 5, 3);

    assertEquals("A", ataque.getNome());
    assertEquals(TipoElemental.NORMAL, ataque.getTipo());
    assertEquals(3, ataque.getNivelMinimo());
    assertFalse(ataque.podeUsar(2));
    assertTrue(ataque.podeUsar(3));
    assertEquals(20, ataque.calcularDano(50)); // 10 + 50/5
  }
}
