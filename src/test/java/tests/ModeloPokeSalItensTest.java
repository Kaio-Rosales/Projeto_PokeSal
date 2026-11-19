package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import model.elementos.StatusEfeito;
import model.elementos.TipoElemental;
import model.itens.Antidote;
import model.itens.Cloak;
import model.itens.MegaPotion;
import model.itens.Potion;
import model.itens.ReturnPass;
import model.itens.Spray;
import model.itens.SuperPotion;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import model.pokesal.PokeSal;
import model.pokesal.TotoSal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import servicos.Servicos;

/**
 * Cobre as entidades que NÃO mudaram de regra na Fase 03 (espécies e itens). Servem de rede de
 * segurança: a refatoração mexeu em quem usa essas classes, e aqui provamos que o comportamento
 * delas continua o mesmo. Não há mock de PokeSal de propósito: são entidades simples.
 */
@ExtendWith(MockitoExtension.class)
class ModeloPokeSalItensTest {

  @Mock
  private Servicos servicos;

  private static List<PokeSal> especies() {
    return Arrays.<PokeSal>asList(new ChikoSal(), new CyndaSal(), new TotoSal());
  }

  @Test
  void testCadaEspecieTemElementoCorretoGolpesLiberadosPorNivel() {
    assertEquals(TipoElemental.GRAMA, new ChikoSal().getElemento());
    assertEquals(TipoElemental.FOGO, new CyndaSal().getElemento());
    assertEquals(TipoElemental.AGUA, new TotoSal().getElemento());

    assertEquals(2, new ChikoSal(1).ataques().size());
    assertEquals(3, new ChikoSal(5).ataques().size());
    assertEquals(4, new ChikoSal(8).ataques().size());
    assertEquals(1, new CyndaSal(1).ataques().size());
    assertEquals(2, new CyndaSal(3).ataques().size());
    assertEquals(3, new CyndaSal(8).ataques().size());
    assertEquals(1, new TotoSal(1).ataques().size());
    assertEquals(2, new TotoSal(5).ataques().size());
    assertEquals(3, new TotoSal(8).ataques().size());
  }

  @Test
  void testTodosOsGolpesTemPrecisaoValidaOsMaisFortesSaoMenosPrecisos() {
    for (PokeSal pokeSal : Arrays.<PokeSal>asList(new ChikoSal(10), new CyndaSal(10),
        new TotoSal(10))) {
      for (var ataque : pokeSal.ataques()) {
        assertTrue(ataque.getPrecisao() >= 0 && ataque.getPrecisao() <= 100, ataque.getNome());
      }
    }
    assertEquals(80, new ChikoSal(10).ataques().get(3).getPrecisao(), "Raio Solar");
    assertEquals(85, new CyndaSal(10).ataques().get(2).getPrecisao(), "Lança-Chamas");
    assertEquals(80, new TotoSal(10).ataques().get(2).getPrecisao(), "Bomba Hidráulica");
  }

  @Test
  void testAcessoresDeAtributosEmTodasAsEspecies() {
    for (PokeSal pokeSal : especies()) {
      pokeSal.setAtk(77);
      pokeSal.setHpAtual(5);
      pokeSal.setXp(9);
      pokeSal.setStatus(StatusEfeito.PARALISADO);

      assertEquals(77, pokeSal.getAtk());
      assertEquals(5, pokeSal.getHpAtual());
      assertEquals(9, pokeSal.getXp());
      assertEquals(StatusEfeito.PARALISADO, pokeSal.getStatus());
      assertTrue(pokeSal.getClass().getSimpleName().endsWith("Sal"));
    }
    ChikoSal chiko = new ChikoSal();
    chiko.setHpMax(99);
    chiko.setDef(12);
    chiko.setSpd(13);
    assertEquals(99, chiko.getHpMax());
    assertEquals(12, chiko.getDef());
    assertEquals(13, chiko.getSpd());
    CyndaSal cynda = new CyndaSal();
    cynda.setHpMax(98);
    cynda.setDef(22);
    cynda.setSpd(23);
    assertEquals(98, cynda.getHpMax());
    assertEquals(22, cynda.getDef());
    assertEquals(23, cynda.getSpd());
    TotoSal toto = new TotoSal();
    toto.setHpMax(97);
    toto.setDef(32);
    toto.setSpd(33);
    assertEquals(97, toto.getHpMax());
    assertEquals(32, toto.getDef());
    assertEquals(33, toto.getSpd());
  }

  @Test
  void testSubirDeNivelAteDezDepoisNaoPassaDoLimite() {
    for (PokeSal pokeSal : especies()) {
      for (int i = 0; i < 12; i++) {
        pokeSal.setNivel();
      }
      assertEquals(10, pokeSal.getNivel(), pokeSal.getClass().getSimpleName());
      assertEquals(pokeSal.getHpMax(), pokeSal.getHpAtual(), "Subir de nível mantém HP cheio");
    }
  }

  @Test
  void testGanharXpQueimaduraParalisiaAlteramAtributosEfetivos() {
    ChikoSal chiko = new ChikoSal(1);
    assertEquals(5, chiko.xpParaProximoNivel());
    assertEquals(1, chiko.ganharXp(4), "4 x 2 = 8 de XP: sobe 1 nível e sobra 3");
    assertEquals(2, chiko.getNivel());
    assertEquals(3, chiko.getXp());

    chiko.setAtk(100);
    chiko.setStatus(StatusEfeito.QUEIMADO);
    assertEquals(95, chiko.getAtkEfetivo());
    chiko.setSpd(100);
    chiko.setStatus(StatusEfeito.PARALISADO);
    assertEquals(70, chiko.getSpdEfetivo());
    chiko.setHpAtual(0);
    assertFalse(chiko.estaVivo());
  }

  @Test
  void testPocoesCuramSemUltrapassarHpMaximoNaoUsamSemEstoque() {
    ChikoSal chiko = new ChikoSal();
    chiko.setHpAtual(1);
    Potion pocao = new Potion();
    pocao.setQuantidade(1);
    pocao.usar(chiko);
    assertEquals(21, chiko.getHpAtual());
    assertEquals(0, pocao.getQuantidade());

    pocao.usar(chiko);                       // sem estoque: nada acontece
    assertEquals(21, chiko.getHpAtual());

    SuperPotion superPocao = new SuperPotion();
    superPocao.setQuantidade(1);
    chiko.setHpAtual(1);
    superPocao.usar(chiko);
    assertEquals(chiko.getHpMax(), chiko.getHpAtual());

    MegaPotion megaPocao = new MegaPotion();
    assertEquals("Mega Poção", megaPocao.getNome());
  }

  @Test
  void testItensDeStatusRemovemSomenteStatusCorrespondente() {
    ChikoSal chiko = new ChikoSal();

    Antidote antidoto = new Antidote();
    antidoto.setQuantidade(2);
    chiko.setStatus(StatusEfeito.ENVENENADO);
    antidoto.usar(chiko);
    assertEquals(StatusEfeito.NENHUM, chiko.getStatus());
    chiko.setStatus(StatusEfeito.QUEIMADO);
    antidoto.usar(chiko);                    // consome, mas não era veneno
    assertEquals(StatusEfeito.QUEIMADO, chiko.getStatus());
    antidoto.usar(chiko);                    // sem estoque
    assertEquals(0, antidoto.getQuantidade());

    Spray spray = new Spray();
    spray.setQuantidade(1);
    spray.usar(chiko);
    assertEquals(StatusEfeito.NENHUM, chiko.getStatus());
    spray.usar(chiko);                       // sem estoque

    Cloak manto = new Cloak();
    manto.setQuantidade(2);
    chiko.setStatus(StatusEfeito.RESFRIADO);
    manto.usar(chiko);
    assertEquals(StatusEfeito.NENHUM, chiko.getStatus());
    chiko.setStatus(StatusEfeito.ENVENENADO);
    manto.usar(chiko);                       // não era resfriado
    assertEquals(StatusEfeito.ENVENENADO, chiko.getStatus());
  }

  @Test
  void testSprayNaoRemoveOutrosStatus() {
    ChikoSal chiko = new ChikoSal();
    chiko.setStatus(StatusEfeito.ENVENENADO);
    Spray spray = new Spray();
    spray.setQuantidade(1);

    spray.usar(chiko);

    assertEquals(StatusEfeito.ENVENENADO, chiko.getStatus());
  }

  @Test
  void testPasseDeRetornoUsaMapaDoServicoSoQuandoHaEstoque() {
    ReturnPass passe = new ReturnPass();
    passe.setQuantidade(1);

    passe.usar(servicos);
    passe.usar(servicos);  // sem estoque: não chama o mapa de novo

    verify(servicos).limparMapa();   // times(1) implícito: só a 1ª vez limpou o mapa
    assertEquals(0, passe.getQuantidade());
    assertEquals("Passe de Retorno", passe.getNome());
  }
}
