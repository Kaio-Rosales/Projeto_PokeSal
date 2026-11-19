package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import model.elementos.StatusEfeito;
import model.itens.Antidote;
import model.itens.Item;
import model.itens.Potion;
import model.jogador.Jogador;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import model.pokesal.PokeSal;
import org.junit.jupiter.api.Test;

/** Testes das regras que passaram a viver em {@code Jogador} na Fase 03. */
class JogadorTest {

  private static Jogador jogador(PokeSal... time) {
    return new Jogador("Ana", new ArrayList<>(Arrays.asList(time)), new ArrayList<Item>());
  }

  private static Potion pocao(int quantidade) {
    Potion pocao = new Potion();
    pocao.setQuantidade(quantidade);
    return pocao;
  }

  @Test
  void testItensComMesmoNomeSaoEmpilhadosNaMochila() {
    // REGRESSÃO do BUG-01: a versão da Fase 02 comparava objetos com equals() e, como duas
    // instâncias de Potion nunca são "iguais", cada item achado virava uma linha nova.
    Jogador ana = jogador();
    Potion primeira = pocao(1);
    ana.adicionarItem(primeira);
    ana.adicionarItem(pocao(2));

    assertEquals(1, ana.getMochila().size(), "Mesma poção não pode ocupar duas linhas");
    assertSame(primeira, ana.getMochila().get(0));
    assertEquals(3, primeira.getQuantidade());
  }

  @Test
  void testItensDiferentesFicamEmLinhasSeparadas() {
    Jogador ana = jogador();
    ana.adicionarItem(pocao(1));
    Antidote antidoto = new Antidote();
    antidoto.setQuantidade(1);
    ana.adicionarItem(antidoto);

    assertEquals(2, ana.getMochila().size());
  }

  @Test
  void testPrimeiroPokeSalVivoPulaDesmaiadosNulos() {
    ChikoSal desmaiado = new ChikoSal();
    desmaiado.setHpAtual(0);
    CyndaSal vivo = new CyndaSal();
    Jogador ana = jogador(desmaiado, null, vivo);

    assertSame(vivo, ana.getPrimeiroPokeSalVivo());
    assertTrue(ana.possuiPokeSalVivo());
  }

  @Test
  void testSemPokeSalVivoDevolveNulo() {
    ChikoSal desmaiado = new ChikoSal();
    desmaiado.setHpAtual(0);

    assertNull(jogador(desmaiado).getPrimeiroPokeSalVivo());
    assertFalse(jogador(desmaiado).possuiPokeSalVivo());
    assertFalse(jogador().possuiPokeSalVivo());
  }

  @Test
  void testCurarTimeRestauraHpRemoveStatusIgnorandoNulos() {
    ChikoSal ferido = new ChikoSal();
    ferido.setHpAtual(0);
    ferido.setStatus(StatusEfeito.QUEIMADO);
    Jogador ana = jogador(ferido, null);

    ana.curarTime();

    assertEquals(ferido.getHpMax(), ferido.getHpAtual());
    assertEquals(StatusEfeito.NENHUM, ferido.getStatus());
  }

  @Test
  void testAcessoresBasicos() {
    Jogador ana = jogador();
    ArrayList<PokeSal> novoTime = new ArrayList<>();
    LinkedList<Item> novaMochila = new LinkedList<>();

    ana.setNome("Bia");
    ana.setTime(novoTime);
    ana.setMochila(novaMochila);

    assertEquals("Bia", ana.getNome());
    assertSame(novoTime, ana.getTime());
    assertSame(novaMochila, ana.getMochila());
  }
}
