package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import model.elementos.EfeitoTerreno;
import model.elementos.TipoElemental;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import model.pokesal.TotoSal;
import org.junit.jupiter.api.Test;

/**
 * Testes do efeito de terreno. MIGRADO da Fase 02 ({@code TerrenosTest}).
 *
 * <p>Antes: o teste precisava chamar {@code Terrenos.limparTerrenos()} em @BeforeEach/@AfterEach
 * porque o estado era {@code static} e "vazava" entre testes. Agora cada constante da enumeração
 * é imutável, então NÃO existe estado para limpar: as mesmas asserções ficaram mais simples.
 */
class EfeitoTerrenoTest {

  @Test
  void testEfeitoTerrenoEstacionamentoUcsal() {
    // Sem nenhum terreno ativo, não há bônus de dano para nenhum tipo.
    assertEquals(1.0, EfeitoTerreno.NENHUM.multiplicadorDano(TipoElemental.FOGO), 0.0001);
    assertEquals(1.0, EfeitoTerreno.NENHUM.multiplicadorDano(TipoElemental.AGUA), 0.0001);
    assertEquals(1.0, EfeitoTerreno.NENHUM.multiplicadorDano(TipoElemental.GRAMA), 0.0001);

    // Asfalto quente: +15% de dano para golpes de Fogo, só para Fogo.
    assertEquals(1.15, EfeitoTerreno.ASFALTO_QUENTE.multiplicadorDano(TipoElemental.FOGO), 0.0001);
    assertEquals(1.0, EfeitoTerreno.ASFALTO_QUENTE.multiplicadorDano(TipoElemental.AGUA), 0.0001);
    assertEquals(1.0, EfeitoTerreno.ASFALTO_QUENTE.multiplicadorDano(TipoElemental.GRAMA), 0.0001);

    // Chão molhado: +10% de dano para golpes de Água, só para Água.
    assertEquals(1.10, EfeitoTerreno.CHAO_MOLHADO.multiplicadorDano(TipoElemental.AGUA), 0.0001);
    assertEquals(1.0, EfeitoTerreno.CHAO_MOLHADO.multiplicadorDano(TipoElemental.FOGO), 0.0001);

    // Terreno florido: não concede bônus de dano (só cura).
    assertEquals(1.0, EfeitoTerreno.TERRENO_FLORIDO.multiplicadorDano(TipoElemental.FOGO), 0.0001);
    assertEquals(1.0, EfeitoTerreno.TERRENO_FLORIDO.multiplicadorDano(TipoElemental.AGUA), 0.0001);
    assertEquals(1.0, EfeitoTerreno.TERRENO_FLORIDO.multiplicadorDano(TipoElemental.GRAMA), 0.0001);
  }

  @Test
  void testCuraFimDeTurnoFloridoCuraApenasPokeSalDeGramaVivoNaoUltrapassaHpMax() {
    EfeitoTerreno florido = EfeitoTerreno.TERRENO_FLORIDO;

    ChikoSal grama = new ChikoSal(); // hpMax 40, elemento GRAMA
    grama.setHpAtual(10);
    // Cura = max(1, 40 * 5 / 100) = 2
    assertEquals(2, florido.calcularCura(grama), "Deveria curar 5% do HP máximo");

    // Não passa do hpMax mesmo perto do limite: só 1 ponto cabe.
    grama.setHpAtual(39);
    assertEquals(1, florido.calcularCura(grama), "Cura não deveria ultrapassar o HP máximo");

    // Já no HP máximo: nada a curar.
    grama.setHpAtual(grama.getHpMax());
    assertEquals(0, florido.calcularCura(grama));

    // Tipo diferente de Grama não é curado, mesmo com florido ativo.
    CyndaSal fogo = new CyndaSal();
    fogo.setHpAtual(1);
    assertEquals(0, florido.calcularCura(fogo), "Quem não é do tipo Grama não deveria curar");

    // PokeSal desmaiado não é curado, mesmo sendo do tipo Grama.
    ChikoSal gramaDesmaiado = new ChikoSal();
    gramaDesmaiado.setHpAtual(0);
    assertEquals(0, florido.calcularCura(gramaDesmaiado), "PokeSal desmaiado não deveria curar");
    assertFalse(gramaDesmaiado.estaVivo());
  }

  @Test
  void testCuraFimDeTurnoNaoOcorreSemTerrenoFlorido() {
    ChikoSal grama = new ChikoSal();
    grama.setHpAtual(10);
    assertEquals(0, EfeitoTerreno.NENHUM.calcularCura(grama),
        "Sem terreno florido não deveria haver cura");

    TotoSal agua = new TotoSal();
    agua.setHpAtual(agua.getHpMax() - 1);
    assertEquals(0, EfeitoTerreno.CHAO_MOLHADO.calcularCura(agua),
        "Terreno molhado não concede cura de fim de turno");
  }

  @Test
  void testCuraNuncaNegativaQuandoHpAtualExcedeHpMax() {
    ChikoSal anomalo = new ChikoSal();
    anomalo.setHpAtual(anomalo.getHpMax() + 10); // estado inválido, mas não pode gerar cura < 0
    assertEquals(0, EfeitoTerreno.TERRENO_FLORIDO.calcularCura(anomalo));
  }

  @Test
  void testMensagemApenasQuandoHaTerreno() {
    assertEquals("", EfeitoTerreno.NENHUM.getMensagem());
    assertFalse(EfeitoTerreno.ASFALTO_QUENTE.getMensagem().isEmpty());
    assertFalse(EfeitoTerreno.CHAO_MOLHADO.getMensagem().isEmpty());
    assertFalse(EfeitoTerreno.TERRENO_FLORIDO.getMensagem().isEmpty());
  }
}
