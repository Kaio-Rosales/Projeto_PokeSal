package servicos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import model.elementos.StatusEfeito;
import model.itens.Potion;
import model.itens.ReturnPass;
import model.itens.Spray;
import model.jogador.Jogador;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import model.pokesal.TotoSal;
import org.junit.jupiter.api.Test;

/**
 * Testes da batalha selvagem INTERATIVA (menu de console). O teclado é simulado: cada linha do
 * texto é uma escolha do jogador (1 = atacar, 2 = usar item, 3 = fugir).
 */
class ServicosBatalhaTest extends ServicosTestBase {

  @Test
  void testFugirEncerraABatalhaSemDanoNemXp() {
    ChikoSal chiko = new ChikoSal();
    Servicos servicos = servicos(jogador(chiko), "3\n");

    servicos.batalha(new CyndaSal(3));

    assertTrue(saida().contains("fugiu da batalha"));
    assertEquals(chiko.getHpMax(), chiko.getHpAtual());
    assertEquals(0, chiko.getXp());
    verify(registro, never()).registrarAtaque(any(), any(), anyBoolean());
  }

  @Test
  void testVitoriaDoJogadorConcedeXp() {
    CyndaSal cynda = new CyndaSal(10);
    sorteioSemSurpresas();
    Servicos servicos = servicos(jogador(cynda), "1\n1\n1\n1\n");

    servicos.batalha(new ChikoSal(1));  // 40 HP; Arranhão causa 38 => cai no 2º golpe

    assertTrue(saida().contains("Você venceu a batalha!"));
    assertTrue(saida().contains("CyndaSal ganhou 2 de XP!"));
    assertEquals(4, cynda.getXp(), "ganharXp soma quantidade x 2 (nível 10 não sobe mais)");
    assertEquals(StatusEfeito.NENHUM, cynda.getStatus());
  }

  @Test
  void testVitoriaQueFazOPokeSalSubirDeNivel() {
    ChikoSal jogadorChiko = new ChikoSal(1);
    ChikoSal selvagem = new ChikoSal(2);
    selvagem.setHpAtual(1);
    sorteioSemSurpresas();
    Servicos servicos = servicos(jogador(jogadorChiko), "1\n1\n");

    servicos.batalha(selvagem);

    assertTrue(saida().contains("subiu para o nível 2!"));
    assertEquals(2, jogadorChiko.getNivel());
  }

  @Test
  void testDerrotaDoJogador() {
    ChikoSal fraco = new ChikoSal(1);
    fraco.setHpAtual(1);
    sorteioSemSurpresas();
    Servicos servicos = servicos(jogador(fraco), "1\n1\n");

    servicos.batalha(new CyndaSal(10));  // mais rápida: ataca primeiro e derruba o jogador

    assertTrue(saida().contains("ChikoSal desmaiou! Você perdeu a batalha..."));
    assertEquals(0, fraco.getXp());
  }

  @Test
  void testSemPokeSalVivoNaoHaBatalha() {
    ChikoSal desmaiado = new ChikoSal();
    desmaiado.setHpAtual(0);

    servicos(jogador(desmaiado), "").batalha(new ChikoSal());
    servicos(null, "").batalha(new ChikoSal());

    assertEquals(2, saida().split("nenhum PokeSal em condições de batalhar", -1).length - 1);
  }

  @Test
  void testItemNaBatalhaConsomeUmaUnidadeEAvisaORegistro() {
    ChikoSal chiko = new ChikoSal(5);
    chiko.setHpAtual(10);
    Potion pocao = new Potion();
    pocao.setQuantidade(1);
    Jogador ana = jogador(chiko);
    ana.getMochila().add(pocao);
    ChikoSal selvagem = new ChikoSal(1);
    selvagem.setAtk(0);
    sorteioSemSurpresas();
    Servicos servicos = servicos(ana, "2\n1\n3\n");   // usa o item 1 e depois foge

    servicos.batalha(selvagem);

    assertEquals(0, pocao.getQuantidade());
    verify(registro).registrarUsoItem(pocao, chiko);
  }

  @Test
  void testLimiteDeDoisItensPorBatalha() {
    ChikoSal chiko = new ChikoSal(5);
    chiko.setHpAtual(5);
    Potion pocoes = new Potion();
    pocoes.setQuantidade(5);
    Jogador ana = jogador(chiko);
    ana.getMochila().add(pocoes);
    ChikoSal selvagem = new ChikoSal(1);
    selvagem.setAtk(0);
    sorteioSemSurpresas();
    // usa, usa, tenta usar de novo (recusado) e foge.
    Servicos servicos = servicos(ana, "2\n1\n2\n1\n2\n3\n");

    servicos.batalha(selvagem);

    assertTrue(saida().contains("número máximo de itens"));
    assertEquals(3, pocoes.getQuantidade());
    verify(registro, times(2)).registrarUsoItem(pocoes, chiko);
  }

  @Test
  void testMochilaVaziaOuEscolhaCanceladaNaoConsomemTurno() {
    Potion pocao = new Potion();
    pocao.setQuantidade(1);
    Jogador semItens = jogador(new ChikoSal());
    Jogador comPocao = jogador(new ChikoSal());
    comPocao.getMochila().add(pocao);

    servicos(semItens, "2\n3\n").batalha(new ChikoSal(1));
    assertTrue(saida().contains("Sua mochila está vazia!"));

    servicos(comPocao, "2\n0\n3\n").batalha(new ChikoSal(1));     // 0 = cancelar
    servicos(comPocao, "2\n9\n3\n").batalha(new ChikoSal(1));     // 9 = inválida
    assertEquals(1, pocao.getQuantidade());
    assertTrue(saida().contains("Opção inválida."));
    verify(registro, never()).registrarUsoItem(any(), any());
  }

  @Test
  void testPasseDeRetornoNaoPodeSerUsadoEmBatalha() {
    ReturnPass passe = new ReturnPass();
    passe.setQuantidade(1);
    Jogador ana = jogador(new ChikoSal());
    ana.getMochila().add(passe);

    servicos(ana, "2\n1\n3\n").batalha(new ChikoSal(1));

    assertTrue(saida().contains("não pode usar o Passe de Retorno durante uma batalha"));
    assertEquals(1, passe.getQuantidade());
  }

  @Test
  void testItemDeStatusRemoveOEfeitoDoPokeSal() {
    ChikoSal chiko = new ChikoSal(5);
    chiko.setStatus(StatusEfeito.QUEIMADO);
    Spray spray = new Spray();
    spray.setQuantidade(1);
    Jogador ana = jogador(chiko);
    ana.getMochila().add(spray);
    ChikoSal selvagem = new ChikoSal(1);
    selvagem.setAtk(0);
    sorteioSemSurpresas();

    servicos(ana, "2\n1\n3\n").batalha(selvagem);

    assertEquals(0, spray.getQuantidade());
    verify(registro).registrarUsoItem(spray, chiko);
  }

  @Test
  void testOpcoesInvalidasNoMenuESaoReaskedAteUmaValida() {
    Servicos servicos = servicos(jogador(new ChikoSal()), "7\nabc\n3\n");

    servicos.batalha(new ChikoSal(1));

    assertTrue(saida().contains("Opção inválida."));
    assertTrue(saida().contains("Digite um número válido"));
    assertTrue(saida().contains("fugiu da batalha"));
  }

  @Test
  void testEscolhaDeAtaqueInvalidaEPedidaDeNovo() {
    ChikoSal chiko = new ChikoSal(5);
    ChikoSal selvagem = new ChikoSal(1);
    selvagem.setAtk(0);
    sorteioSemSurpresas();
    // Atacar -> ataque 9 (inválido) -> ataque 1 (válido) -> foge no turno seguinte.
    Servicos servicos = servicos(jogador(chiko), "1\n9\n1\n3\n");

    servicos.batalha(selvagem);

    assertTrue(saida().contains("Opção inválida."));
    assertTrue(saida().contains("1 - Lâmina de Navalha (GRAMA)"));
    verify(registro, times(2)).registrarAtaque(any(), any(), anyBoolean());
  }

  @Test
  void testStatusDoOponenteTambemApareceNoPainel() {
    ChikoSal chiko = new ChikoSal();
    TotoSal selvagem = new TotoSal(1);
    selvagem.setStatus(StatusEfeito.ENVENENADO);

    servicos(jogador(chiko), "3\n").batalha(selvagem);

    assertTrue(saida().contains("[ENVENENADO]"));
    assertEquals(StatusEfeito.NENHUM, selvagem.getStatus(), "Status é limpo ao fim da batalha");
  }
}
