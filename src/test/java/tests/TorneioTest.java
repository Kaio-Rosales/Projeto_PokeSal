package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import batalha.ExecutorBatalha;
import batalha.ResultadoBatalha;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import model.jogador.Jogador;
import model.pokesal.ChikoSal;
import model.pokesal.PokeSal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import torneio.Torneio;

/**
 * Testes do chaveamento do {@code Torneio}.
 *
 * <p>MOCKADO: {@code ExecutorBatalha}. Não queremos simular golpe por golpe aqui: o que
 * importa é "dado quem venceu cada luta, quem avança para a próxima fase". Os {@code Jogador}es
 * são objetos reais (entidades simples).
 */
@ExtendWith(MockitoExtension.class)
class TorneioTest {

  @Mock
  private ExecutorBatalha executor;
  @InjectMocks
  private Torneio torneio;

  private static Jogador jogador(String nome) {
    return new Jogador(nome, new ArrayList<>(), new ArrayList<>());
  }

  private static ResultadoBatalha resultado(Jogador vencedor, Jogador perdedor, int turnos) {
    return new ResultadoBatalha(vencedor, perdedor, turnos, Collections.<PokeSal>emptyList(),
        Collections.<PokeSal>emptyList());
  }

  @Test
  void testChaveamentoTorneioAvancaVencedores() {
    Jogador p1 = jogador("P1");
    Jogador p2 = jogador("P2");
    Jogador p3 = jogador("P3");
    Jogador p4 = jogador("P4");
    Jogador p5 = jogador("P5");
    Jogador p6 = jogador("P6");
    Jogador p7 = jogador("P7");
    Jogador p8 = jogador("P8");

    // Valores EXATOS nos stubs (e não any()): assim o teste também prova QUEM enfrenta QUEM.
    // Se o chaveamento montasse o par errado, o Mockito acusaria o argumento inesperado.
    // Usamos doReturn().when() porque o MESMO método é configurado várias vezes com argumentos
    // diferentes (com when().thenReturn() o modo estrito lança PotentialStubbingProblem).
    // Rodada 1 (quartas): 1x2, 3x4, 5x6, 7x8
    doReturn(resultado(p1, p2, 5)).when(executor).lutar(p1, p2);
    doReturn(resultado(p4, p3, 6)).when(executor).lutar(p3, p4);
    doReturn(resultado(p5, p6, 7)).when(executor).lutar(p5, p6);
    doReturn(resultado(p8, p7, 8)).when(executor).lutar(p7, p8);
    // Rodada 2 (semifinais): vencedores em ordem => P1 x P4 e P5 x P8
    doReturn(resultado(p4, p1, 9)).when(executor).lutar(p1, p4);
    doReturn(resultado(p5, p8, 10)).when(executor).lutar(p5, p8);
    // Final: P4 x P5
    doReturn(resultado(p5, p4, 11)).when(executor).lutar(p4, p5);

    Jogador campeao = torneio.disputar(Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8));

    assertSame(p5, campeao);
    InOrder ordem = inOrder(executor);
    ordem.verify(executor).lutar(p1, p2);
    ordem.verify(executor).lutar(p3, p4);
    ordem.verify(executor).lutar(p5, p6);
    ordem.verify(executor).lutar(p7, p8);
    ordem.verify(executor).lutar(p1, p4);
    ordem.verify(executor).lutar(p5, p8);
    ordem.verify(executor).lutar(p4, p5);
    verify(executor, times(7)).lutar(any(Jogador.class), any(Jogador.class)); // n-1 lutas
    assertEquals(7, torneio.getHistorico().size());
    assertEquals(11, torneio.getHistorico().get(6).getTurnos(), "A final é o último resultado");
  }

  @Test
  void testTorneioDeQuatroTreinadoresUsaRetornosEmSequencia() {
    Jogador a = jogador("A");
    Jogador b = jogador("B");
    Jogador c = jogador("C");
    Jogador d = jogador("D");
    // Aqui só importa a ORDEM dos resultados, então any() + retornos em sequência bastam:
    // semifinal 1: A vence B; semifinal 2: D vence C; final: D vence A.
    when(executor.lutar(any(Jogador.class), any(Jogador.class))).thenReturn(
        resultado(a, b, 4), resultado(d, c, 5), resultado(d, a, 6));

    Jogador campeao = torneio.disputar(Arrays.asList(a, b, c, d));

    assertSame(d, campeao);
    verify(executor).lutar(a, d);   // final entre os dois vencedores das semifinais
    assertEquals(3, torneio.getHistorico().size());
  }

  @Test
  void testTimesSaoCuradosAntesDeCadaLuta() {
    ChikoSal ferida = new ChikoSal();
    ferida.setHpAtual(1);
    Jogador a =
        new Jogador("A", new ArrayList<>(Arrays.<PokeSal>asList(ferida)), new ArrayList<>());
    Jogador b = jogador("B");
    Jogador c = jogador("C");
    Jogador d = jogador("D");
    when(executor.lutar(any(Jogador.class), any(Jogador.class))).thenReturn(
        resultado(a, b, 1), resultado(c, d, 1), resultado(a, c, 1));

    torneio.disputar(Arrays.asList(a, b, c, d));

    assertEquals(ferida.getHpMax(), ferida.getHpAtual(), "O Centro PokeSal cura antes da luta");
  }

  @ParameterizedTest(name = "{0} participantes são rejeitados")
  @ValueSource(ints = {0, 1, 2, 3, 5, 6, 7, 9, 16})
  void testTamanhoInvalidoDoTorneioLancadoAntesDeQualquerLuta(int quantidade) {
    List<Jogador> participantes = new ArrayList<>();
    for (int i = 0; i < quantidade; i++) {
      participantes.add(jogador("P" + i));
    }

    assertThrows(IllegalArgumentException.class, () -> torneio.disputar(participantes));

    verify(executor, never()).lutar(any(), any());
  }

  @Test
  void testListaNulaOuComTreinadorNuloEhRejeitada() {
    assertThrows(IllegalArgumentException.class, () -> torneio.disputar(null));
    assertThrows(IllegalArgumentException.class,
        () -> torneio.disputar(Arrays.asList(jogador("A"), jogador("B"), jogador("C"), null)));
    verify(executor, never()).lutar(any(), any());
  }

  @Test
  void testNomesRepetidosSaoRejeitadosPoisRankingUsaNome() {
    assertThrows(IllegalArgumentException.class,
        () -> torneio.disputar(Arrays.asList(jogador("Ana"), jogador("Bruno"), jogador("Carla"),
            jogador("ANA"))));
    verify(executor, never()).lutar(any(), any());
  }

  @Test
  void testVencedorQueNaoParticipouDaLutaEhRejeitado() {
    Jogador a = jogador("A");
    Jogador b = jogador("B");
    Jogador intruso = jogador("Intruso");
    doReturn(resultado(intruso, b, 3)).when(executor).lutar(a, b);

    assertThrows(IllegalStateException.class,
        () -> torneio.disputar(Arrays.asList(a, b, jogador("C"), jogador("D"))));
  }

  @Test
  void testFalhaDoExecutorInterrompeTorneio() {
    when(executor.lutar(any(Jogador.class), any(Jogador.class)))
        .thenThrow(new IllegalStateException("arena fechada"));

    assertThrows(IllegalStateException.class,
        () -> torneio.disputar(Arrays.asList(jogador("A"), jogador("B"), jogador("C"),
            jogador("D"))));
    assertTrue(torneio.getHistorico().isEmpty());
  }

  @Test
  void testHistoricoNaoPodeSerAlteradoDeFora() {
    assertThrows(UnsupportedOperationException.class,
        () -> torneio.getHistorico().add(resultado(jogador("A"), jogador("B"), 1)));
  }

  @Test
  void testExecutorEhObrigatorio() {
    assertThrows(NullPointerException.class, () -> new Torneio(null));
  }

  @Test
  void testTamanhosValidos() {
    assertTrue(Torneio.tamanhoValido(4));
    assertTrue(Torneio.tamanhoValido(8));
    assertEquals(false, Torneio.tamanhoValido(6));
  }
}
