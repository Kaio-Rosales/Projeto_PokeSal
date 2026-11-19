package servicos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import model.elementos.EfeitoTerreno;
import model.elementos.StatusEfeito;
import model.itens.Potion;
import model.itens.ReturnPass;
import model.itens.Spray;
import model.jogador.Jogador;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import org.junit.jupiter.api.Test;

/** Testes do mapa, dos itens fora de batalha, da cura e do nome do jogador. */
class ServicosMapaEItensTest extends ServicosTestBase {

  /** Para os testes de mapa, todo evento aleatório vira "achar uma poção" (inofensivo). */
  private void eventosSempreAcharPocao() {
    when(servicoTerreno.obterEfeitoAtual()).thenReturn(EfeitoTerreno.NENHUM);
    doReturn(0).when(gerador).proximoInt(anyInt());
  }

  private static int posicoesMarcadas(int[][] mapa) {
    int total = 0;
    for (int[] linha : mapa) {
      for (int celula : linha) {
        total += celula;
      }
    }
    return total;
  }

  // ------------------------------------------------------------------ mapa

  @Test
  void testEscolhaInvalidaNoMapaNaoCorrompeOEstado() {
    // REGRESSÃO do BUG-02: antes, digitar 9 deixava escolha = 8 e o PRÓXIMO "avançar" lançava
    // ArrayIndexOutOfBoundsException. Agora o estado volta a -1 e o jogo segue normalmente.
    eventosSempreAcharPocao();
    Servicos servicos = servicos(jogador(new ChikoSal()), "9\n-5\n1\n");

    servicos.avancarMovimento();   // 9  -> inválido
    servicos.avancarMovimento();   // -5 -> inválido
    servicos.avancarMovimento();   // 1  -> Entrada da Ucsal

    assertEquals(2, saida().split("Escolha inválida", -1).length - 1);
    assertEquals(1, servicos.getLocalizacao()[0][0]);
  }

  @Test
  void testPrimeiroAvancoMarcaAEntradaEConsultaOTerreno() {
    eventosSempreAcharPocao();
    Servicos servicos = servicos(jogador(new ChikoSal()), "1\n");

    servicos.avancarMovimento();

    assertEquals(1, servicos.getLocalizacao()[0][0]);
    assertTrue(saida().contains("Mapa Atual:"));
    verify(servicoTerreno, times(1)).obterEfeitoAtual();
  }

  @Test
  void testAvancarAndaPassoAPassoAteOFimDoCaminho() {
    eventosSempreAcharPocao();
    Servicos servicos = servicos(jogador(new ChikoSal()), "2\n");

    servicos.avancarMovimento();  // escolhe o Estacionamento (índice 1), posição 0
    servicos.avancarMovimento();  // passo 1
    servicos.avancarMovimento();  // passo 2
    servicos.avancarMovimento();  // não há passo 3

    assertEquals(1, servicos.getLocalizacao()[1][2]);
    assertTrue(saida().contains("Avançou um passo"));
    assertTrue(saida().contains("Você chegou ao fim do caminho"));
  }

  @Test
  void testVilaUniversitariaCuraOTimeELimpaOMapa() {
    ChikoSal ferido = new ChikoSal();
    ferido.setHpAtual(1);
    ferido.setStatus(StatusEfeito.QUEIMADO);
    Servicos servicos = servicos(jogador(ferido), "3\n");

    servicos.avancarMovimento();

    assertEquals(ferido.getHpMax(), ferido.getHpAtual());
    assertEquals(StatusEfeito.NENHUM, ferido.getStatus());
    assertEquals(0, posicoesMarcadas(servicos.getLocalizacao()));
    assertTrue(saida().contains("1 PokeSal(s) curado(s) com sucesso!"));
  }

  @Test
  void testRetornarSemTerAvancadoEOperacaoInvalida() {
    servicos(jogador(), "").retornarMovimento();

    assertTrue(saida().contains("Operação inválida"));
  }

  @Test
  void testRetornarRecuaUmaPosicaoEDepoisVoltaAoPontoDePartida() {
    eventosSempreAcharPocao();
    Servicos servicos = servicos(jogador(new ChikoSal()), "1\n");
    servicos.avancarMovimento();   // [0][0]
    servicos.avancarMovimento();   // [0][1]

    servicos.retornarMovimento();  // desmarca [0][1] e ainda há trilha
    assertTrue(saida().contains("Você recuou uma posição"));
    assertEquals(0, servicos.getLocalizacao()[0][1]);

    servicos.retornarMovimento();  // desmarca [0][0]
    assertTrue(saida().contains("retornou ao ponto de partida"));
    assertEquals(0, posicoesMarcadas(servicos.getLocalizacao()));
  }

  @Test
  void testLimparMapaZeraAsPosicoes() {
    eventosSempreAcharPocao();
    Servicos servicos = servicos(jogador(new ChikoSal()), "1\n");
    servicos.avancarMovimento();

    servicos.limparMapa();

    assertEquals(0, posicoesMarcadas(servicos.getLocalizacao()));
    assertTrue(saida().contains("Você voltou ao início"));
  }

  // ------------------------------------------------------------------ cura

  @Test
  void testCuraTotalIgnoraPosicoesNulasEAvisaTimeVazio() {
    ChikoSal ferido = new ChikoSal();
    ferido.setHpAtual(3);
    Jogador comNulo = jogador(ferido, null);
    servicos(comNulo, "").curaTotal();
    assertEquals(ferido.getHpMax(), ferido.getHpAtual());

    servicos(jogador(), "").curaTotal();
    assertTrue(saida().contains("O time está vazio"));
  }

  // ------------------------------------------------------------------ itens fora de batalha

  @Test
  void testSemItensDeCuraOuPasseMostraAviso() {
    Jogador ana = jogador(new ChikoSal());
    Spray spray = new Spray();   // item de status: não aparece fora de batalha
    spray.setQuantidade(1);
    ana.getMochila().add(spray);

    servicos(ana, "").usarItem();

    assertTrue(saida().contains("Você não possui itens de cura ou Passe de Retorno."));
  }

  @Test
  void testPocaoForaDeBatalhaCuraOPokeSalEscolhido() {
    ChikoSal ferido = new ChikoSal();
    ferido.setHpAtual(5);
    Potion pocao = new Potion();
    pocao.setQuantidade(1);
    Jogador ana = jogador(ferido);
    ana.getMochila().add(pocao);

    servicos(ana, "1\n1\n").usarItem();   // item 1, PokeSal 1

    assertEquals(25, ferido.getHpAtual());
    assertEquals(0, pocao.getQuantidade());
  }

  @Test
  void testEscolhasInvalidasOuCanceladasNaoGastamItens() {
    ChikoSal ferido = new ChikoSal();
    ferido.setHpAtual(5);
    Potion pocao = new Potion();
    pocao.setQuantidade(1);
    Jogador ana = jogador(ferido);
    ana.getMochila().add(pocao);

    servicos(ana, "0\n").usarItem();        // cancelar
    servicos(ana, "9\n").usarItem();        // item inexistente
    servicos(ana, "1\n9\n").usarItem();     // PokeSal inexistente

    assertEquals(5, ferido.getHpAtual());
    assertEquals(1, pocao.getQuantidade());
    assertTrue(saida().contains("Operação cancelada."));
    assertTrue(saida().contains("Opção inválida."));
  }

  @Test
  void testPocaoSemTimeAvisaQueOTimeEstaVazio() {
    Potion pocao = new Potion();
    pocao.setQuantidade(1);
    Jogador ana = jogador();
    ana.getMochila().add(pocao);

    servicos(ana, "1\n").usarItem();

    assertTrue(saida().contains("Seu time está vazio."));
    assertEquals(1, pocao.getQuantidade());
  }

  @Test
  void testPasseDeRetornoLimpaOMapa() {
    eventosSempreAcharPocao();
    ReturnPass passe = new ReturnPass();
    passe.setQuantidade(1);
    Jogador ana = jogador(new ChikoSal());
    ana.getMochila().add(passe);
    // Um único teclado simulado: "1" escolhe a Entrada no mapa; o outro "1" escolhe o item 1
    // (o Passe, que já estava na mochila antes da poção achada no evento).
    Servicos servicos = servicos(ana, "1\n1\n");
    servicos.avancarMovimento();
    assertEquals(1, posicoesMarcadas(servicos.getLocalizacao()));

    servicos.usarItem();

    assertEquals(0, passe.getQuantidade());
    assertEquals(0, posicoesMarcadas(servicos.getLocalizacao()));
    assertTrue(saida().contains("Passe de Retorno usado."));
  }

  // ------------------------------------------------------------------ nome e ciclo de vida

  @Test
  void testTrocarONomeOuCancelar() {
    Jogador ana = jogador();
    servicos(ana, "n\n").verificarNome();
    assertEquals("Ana", ana.getNome());

    servicos(ana, "s\nBia Souza\n").verificarNome();
    assertEquals("Bia Souza", ana.getNome());
  }

  @Test
  void testJogadorPodeSerTrocadoEEncerrarFechaOTeclado() {
    Servicos servicos = servicos(jogador(), "");
    Jogador outro = jogador(new CyndaSal());

    servicos.setJogador(outro);
    assertSame(outro, servicos.getJogador());

    servicos.encerrar();
    assertThrows(IllegalStateException.class, () -> servicos.verificarNome());
  }
}
