package servicos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import batalha.MecanicaCombate;
import java.util.Scanner;
import model.elementos.EfeitoTerreno;
import model.itens.Item;
import model.jogador.Jogador;
import model.pokesal.ChikoSal;
import model.pokesal.PokeSal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Testes dos eventos aleatórios do mapa: o sorteio vem do {@code GeradorAleatorio} mockado e o
 * terreno do {@code ServicoTerreno} mockado, então cada cenário é reproduzível.
 */
class ServicosEventosTest extends ServicosTestBase {

  @ParameterizedTest(name = "sorteio {0} => {1}")
  @CsvSource({
    "0, Poção",
    "1, Super Poção",
    "2, Mega Poção",
    "3, Antidoto",
    "4, Spray",
    "5, Manto",
    "6, Passe de Retorno"
  })
  void testSorteioDeItemEncontradoVaiParaMochila(int sorteio, String nomeEsperado) {
    when(servicoTerreno.obterEfeitoAtual()).thenReturn(EfeitoTerreno.NENHUM);
    when(gerador.proximoInt(2)).thenReturn(0);              // evento 0 = achar item
    doReturn(sorteio).when(gerador).proximoInt(7);
    Jogador ana = jogador();

    servicos(ana, "").eventosAleatorios();

    assertEquals(1, ana.getMochila().size());
    assertEquals(nomeEsperado, ana.getMochila().get(0).getNome());
    assertEquals(1, ana.getMochila().get(0).getQuantidade());
    assertTrue(saida().contains("Você encontrou: " + nomeEsperado));
  }

  @Test
  void testItemRepetidoEmpilhadoEmVezDeDuplicado() {
    // REGRESSÃO do BUG-01 no fluxo real do jogo: achar duas poções gera UMA linha com x2.
    when(servicoTerreno.obterEfeitoAtual()).thenReturn(EfeitoTerreno.NENHUM);
    when(gerador.proximoInt(2)).thenReturn(0);
    doReturn(0).when(gerador).proximoInt(7);
    Jogador ana = jogador();
    Servicos servicos = servicos(ana, "");

    servicos.eventosAleatorios();
    servicos.eventosAleatorios();

    assertEquals(1, ana.getMochila().size());
    Item pocao = ana.getMochila().get(0);
    assertEquals(2, pocao.getQuantidade());
  }

  @ParameterizedTest(name = "espécie {0} => {1}")
  @CsvSource({"0, ChikoSal", "1, TotoSal", "2, CyndaSal"})
  void testSorteioDeBatalhaCriaOponenteDaEspecieNivelSorteados(int especie, String nome) {
    when(servicoTerreno.obterEfeitoAtual()).thenReturn(EfeitoTerreno.NENHUM);
    when(gerador.proximoInt(2)).thenReturn(1);              // evento 1 = batalha
    doReturn(especie).when(gerador).proximoInt(3);
    doReturn(4).when(gerador).proximoInt(10);               // nível = 4 + 1 = 5
    Servicos servicos = servicos(jogador(new ChikoSal()), "3\n");   // entra na batalha e foge

    servicos.eventosAleatorios();

    PokeSal oponente = servicos.getOponente();
    assertNotNull(oponente);
    assertEquals(nome, oponente.getClass().getSimpleName());
    assertEquals(5, oponente.getNivel());
    assertTrue(saida().contains("selvagem de nível 5 apareceu"));
  }

  @Test
  void testTerrenoConsultadoAnunciadoAoJogador() {
    when(servicoTerreno.obterEfeitoAtual()).thenReturn(EfeitoTerreno.CHAO_MOLHADO);
    when(gerador.proximoInt(2)).thenReturn(0);
    doReturn(0).when(gerador).proximoInt(7);
    Servicos servicos = servicos(jogador(), "");

    servicos.eventosAleatorios();

    assertEquals(EfeitoTerreno.CHAO_MOLHADO, servicos.getEfeitoTerrenoAtual());
    assertTrue(saida().contains("O chão está molhado!"));
    verify(servicoTerreno, times(1)).obterEfeitoAtual();
  }

  @Test
  void testSemEfeitoDeTerrenoNadaAnunciado() {
    when(servicoTerreno.obterEfeitoAtual()).thenReturn(EfeitoTerreno.NENHUM);
    when(gerador.proximoInt(2)).thenReturn(0);
    doReturn(0).when(gerador).proximoInt(7);

    servicos(jogador(), "").eventosAleatorios();

    assertFalse(saida().contains("O dia está quente"));
    assertFalse(saida().contains("molhado"));
    assertFalse(saida().contains("plantas"));
  }

  @Test
  void testTerrenoFloridoCuraNoFimDoTurnoDaBatalhaSelvagem() {
    when(servicoTerreno.obterEfeitoAtual()).thenReturn(EfeitoTerreno.TERRENO_FLORIDO);
    sorteioSemSurpresas();
    doReturn(1).when(gerador).proximoInt(2);                // evento = batalha; IA usa golpe 2
    ChikoSal chiko = new ChikoSal(5);
    Servicos servicos = servicos(jogador(chiko), "1\n1\n3\n");  // ataca, ataca, foge

    servicos.eventosAleatorios();

    verify(registro, atLeastOnce()).registrarEfeitoTerreno(eq(chiko),
        eq(EfeitoTerreno.TERRENO_FLORIDO), contains("terreno florido"));
  }

  @Test
  void testVerificarLimiteDeItens() {
    Servicos servicos = servicos(jogador(), "");

    assertTrue(servicos.verificarLimiteDeItens(0));
    assertTrue(servicos.verificarLimiteDeItens(1));
    assertFalse(servicos.verificarLimiteDeItens(2));
  }

  @Test
  void testColaboradoresDoConstrutorSaoObrigatorios() {
    Scanner teclado = new Scanner("");
    MecanicaCombate mecanica = new MecanicaCombate(gerador, registro);

    assertThrows(NullPointerException.class,
        () -> new Servicos(jogador(), null, mecanica, servicoTerreno, gerador, registro));
    assertThrows(NullPointerException.class,
        () -> new Servicos(jogador(), teclado, null, servicoTerreno, gerador, registro));
    assertThrows(NullPointerException.class,
        () -> new Servicos(jogador(), teclado, mecanica, null, gerador, registro));
    assertThrows(NullPointerException.class,
        () -> new Servicos(jogador(), teclado, mecanica, servicoTerreno, null, registro));
    assertThrows(NullPointerException.class,
        () -> new Servicos(jogador(), teclado, mecanica, servicoTerreno, gerador, null));
  }
}
