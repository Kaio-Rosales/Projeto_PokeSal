package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import model.elementos.TipoElemental;
import org.junit.jupiter.api.Test;

/** Testes de vantagem e desvantagem elemental. */
public class TipoElementalTest {
  private static final double DELTA = 0.0001;

  @Test
  void testVantagemElemental() {
    // Super efetivo (2.0x): Fogo > Grama, Água > Fogo, Grama > Água.
    assertEquals(2.0, TipoElemental.FOGO.danoElemental(TipoElemental.FOGO, TipoElemental.GRAMA),
        DELTA, "Fogo deveria ser super efetivo contra Grama");
    assertEquals(2.0, TipoElemental.AGUA.danoElemental(TipoElemental.AGUA, TipoElemental.FOGO),
        DELTA, "Água deveria ser super efetiva contra Fogo");
    assertEquals(2.0, TipoElemental.GRAMA.danoElemental(TipoElemental.GRAMA, TipoElemental.AGUA),
        DELTA, "Grama deveria ser super efetiva contra Água");

    // Pouco efetivo (0.5x): o sentido inverso de cada matchup acima.
    assertEquals(0.5, TipoElemental.FOGO.danoElemental(TipoElemental.FOGO, TipoElemental.AGUA),
        DELTA, "Fogo deveria ser pouco efetivo contra Água");
    assertEquals(0.5, TipoElemental.AGUA.danoElemental(TipoElemental.AGUA, TipoElemental.GRAMA),
        DELTA, "Água deveria ser pouco efetiva contra Grama");
    assertEquals(0.5, TipoElemental.GRAMA.danoElemental(TipoElemental.GRAMA, TipoElemental.FOGO),
        DELTA, "Grama deveria ser pouco efetiva contra Fogo");

    // Neutro (1.0x): mesmo tipo, e qualquer combinação contra/com Normal.
    assertEquals(1.0, TipoElemental.FOGO.danoElemental(TipoElemental.FOGO, TipoElemental.FOGO),
        DELTA, "Mesmo tipo deveria ser neutro");
    assertEquals(1.0, TipoElemental.NORMAL.danoElemental(TipoElemental.NORMAL, TipoElemental.AGUA),
        DELTA, "Normal contra Água deveria ser neutro");
    assertEquals(1.0, TipoElemental.FOGO.danoElemental(TipoElemental.FOGO, TipoElemental.NORMAL),
        DELTA, "Fogo contra Normal deveria ser neutro");
    assertEquals(1.0,
        TipoElemental.NORMAL.danoElemental(TipoElemental.NORMAL, TipoElemental.NORMAL),
        DELTA, "Normal contra Normal deveria ser neutro");
  }
}
