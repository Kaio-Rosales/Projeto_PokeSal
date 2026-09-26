package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import model.elementos.Terrenos;
import model.elementos.TipoElemental;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import model.pokesal.TotoSal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testes do efeito de terreno.
 */
public class TerrenosTest {

  @BeforeEach
  @AfterEach
  void limparEstadoEstaticoDosTerrenos() {
    // Terrenos guarda estado em campos static, compartilhado entre todos os testes da JVM.
    // Sem isso, um teste poderia "vazar" terreno ativo para o próximo.
    Terrenos.limparTerrenos();
  }

  @Test
  void testEfeitoTerrenoEstacionamentoUcsal() {
    // Sem nenhum terreno ativo, não há bônus de dano para nenhum tipo.
    assertEquals(1.0, Terrenos.multiplicadorDano(TipoElemental.FOGO), 0.0001);
    assertEquals(1.0, Terrenos.multiplicadorDano(TipoElemental.AGUA), 0.0001);
    assertEquals(1.0, Terrenos.multiplicadorDano(TipoElemental.GRAMA), 0.0001);

    // Dia quente (evento 0): +15% de dano para golpes de Fogo, só para Fogo.
    Terrenos.validarTerrenos(0);
    assertEquals(1.15, Terrenos.multiplicadorDano(TipoElemental.FOGO), 0.0001);
    assertEquals(1.0, Terrenos.multiplicadorDano(TipoElemental.AGUA), 0.0001);
    assertEquals(1.0, Terrenos.multiplicadorDano(TipoElemental.GRAMA), 0.0001);

    // limparTerrenos() desativa o terreno anterior antes do próximo evento ser aplicado.
    Terrenos.limparTerrenos();
    assertEquals(1.0, Terrenos.multiplicadorDano(TipoElemental.FOGO), 0.0001);

    // Chão molhado (evento 1): +10% de dano para golpes de Água, só para Água.
    Terrenos.validarTerrenos(1);
    assertEquals(1.10, Terrenos.multiplicadorDano(TipoElemental.AGUA), 0.0001);
    assertEquals(1.0, Terrenos.multiplicadorDano(TipoElemental.FOGO), 0.0001);
    Terrenos.limparTerrenos();

    // Terreno florido (evento 2): não concede bônus de dano (só cura, testado abaixo).
    Terrenos.validarTerrenos(2);
    assertEquals(1.0, Terrenos.multiplicadorDano(TipoElemental.FOGO), 0.0001);
    assertEquals(1.0, Terrenos.multiplicadorDano(TipoElemental.AGUA), 0.0001);
    assertEquals(1.0, Terrenos.multiplicadorDano(TipoElemental.GRAMA), 0.0001);
  }

  @Test
  void testCuraFimDeTurnoFloridoCuraApenasPokeSalDeGramaVivoNaoUltrapassaHpMax() {
    Terrenos.validarTerrenos(2); // florido = true

    ChikoSal grama = new ChikoSal(); // hpMax 40, elemento GRAMA
    grama.setHpAtual(10);
    Terrenos.curaFimDeTurno(grama);
    // Cura = max(1, 40 * 5 / 100) = 2
    assertEquals(12, grama.getHpAtual(), "Deveria curar 5% do HP máximo");

    // Não passa do hpMax mesmo perto do limite.
    grama.setHpAtual(39);
    Terrenos.curaFimDeTurno(grama);
    assertEquals(40, grama.getHpAtual(), "Cura não deveria ultrapassar o HP máximo");

    // Já no HP máximo: nada muda.
    Terrenos.curaFimDeTurno(grama);
    assertEquals(40, grama.getHpAtual());

    // Tipo diferente de Grama não é curado, mesmo com florido ativo.
    CyndaSal fogo = new CyndaSal();
    fogo.setHpAtual(1);
    Terrenos.curaFimDeTurno(fogo);
    assertEquals(1, fogo.getHpAtual(), "PokeSal que não é do tipo Grama não deveria curar");

    // PokeSal desmaiado não é curado, mesmo sendo do tipo Grama.
    ChikoSal gramaDesmaiado = new ChikoSal();
    gramaDesmaiado.setHpAtual(0);
    Terrenos.curaFimDeTurno(gramaDesmaiado);
    assertEquals(0, gramaDesmaiado.getHpAtual(), "PokeSal desmaiado não deveria ser curado");
    assertFalse(gramaDesmaiado.estaVivo());
  }

  @Test
  void testCuraFimDeTurnoNaoOcorreSemTerrenoFlorido() {
    ChikoSal grama = new ChikoSal();
    grama.setHpAtual(10);
    Terrenos.curaFimDeTurno(grama); // florido está false (limpo no @BeforeEach)
    assertEquals(10, grama.getHpAtual(), "Sem terreno florido não deveria haver cura");

    TotoSal agua = new TotoSal();
    agua.setHpAtual(agua.getHpMax() - 1);
    Terrenos.validarTerrenos(1); // molhado, não florido
    Terrenos.curaFimDeTurno(agua);
    assertEquals(agua.getHpMax() - 1, agua.getHpAtual(),
        "Terreno molhado não concede cura de fim de turno");
  }
}
