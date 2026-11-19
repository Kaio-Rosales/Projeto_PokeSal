package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import batalha.RepositorioResultados;
import batalha.ResultadoBatalha;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import model.jogador.Jogador;
import model.pokesal.PokeSal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import registro.RegistroBatalha;
import torneio.LinhaRanking;
import torneio.RankingTreinadores;

/**
 * Testes do REQUISITO AUTORAL 1 (RA-01): ranking de treinadores.
 *
 * <p>MOCKADOS: {@code RepositorioResultados} (de onde vêm os dados) e {@code RegistroBatalha}
 * (para onde vão os erros).
 */
@ExtendWith(MockitoExtension.class)
class RankingTreinadoresTest {

  @Mock
  private RepositorioResultados repositorio;
  @Mock
  private RegistroBatalha registro;
  @InjectMocks
  private RankingTreinadores ranking;

  private static Jogador jogador(String nome) {
    return new Jogador(nome, new ArrayList<>(), new ArrayList<>());
  }

  private static ResultadoBatalha venceu(String vencedor, String perdedor, int turnos) {
    return new ResultadoBatalha(jogador(vencedor), jogador(perdedor), turnos,
        Collections.<PokeSal>emptyList(), Collections.<PokeSal>emptyList());
  }

  @Test
  void testRankingSomaPontosComBonusDeVitoriaRelampago() {
    when(repositorio.listarTodos()).thenReturn(Arrays.asList(
        venceu("Ana", "Bruno", 5),     // Ana: 3 + 1 (relâmpago, <= 6 turnos)
        venceu("Ana", "Carla", 10),    // Ana: +3
        venceu("Carla", "Bruno", 8))); // Carla: +3

    List<LinhaRanking> tabela = ranking.gerarRanking();

    assertEquals(3, tabela.size());
    assertLinha(tabela.get(0), 1, "Ana", 2, 0, 7, 15);
    assertLinha(tabela.get(1), 2, "Carla", 1, 1, 3, 18);
    assertLinha(tabela.get(2), 3, "Bruno", 0, 2, 0, 13);
    verify(repositorio, times(1)).listarTodos();
  }

  @Test
  void testBonusRelampagoValeAteSeisTurnosDeixaDeValerNoSetimo() {
    when(repositorio.listarTodos()).thenReturn(Arrays.asList(
        venceu("Ana", "X", 6),
        venceu("Bia", "Y", 7)));

    List<LinhaRanking> tabela = ranking.gerarRanking();

    assertEquals("Ana", tabela.get(0).getTreinador());
    assertEquals(4, tabela.get(0).getPontos(), "6 turnos: 3 + 1");
    assertEquals("Bia", tabela.get(1).getTreinador());
    assertEquals(3, tabela.get(1).getPontos(), "7 turnos: sem bônus");
  }

  @Test
  void testDesempateUsaMenosTurnosDepoisOrdemAlfabetica() {
    // Todos os vencedores fazem 3 pontos e todos os perdedores 0: sobra desempatar.
    when(repositorio.listarTodos()).thenReturn(Arrays.asList(
        venceu("A", "C", 10),
        venceu("B", "D", 8),
        venceu("E", "F", 8)));

    List<LinhaRanking> tabela = ranking.gerarRanking();

    List<String> nomes = new ArrayList<>();
    for (LinhaRanking linha : tabela) {
      nomes.add(linha.getTreinador());
    }
    // 3 pts: B e E (8 turnos, alfabético) antes de A (10 turnos). 0 pts: D e F (8) antes de C.
    assertEquals(Arrays.asList("B", "E", "A", "D", "F", "C"), nomes);
  }

  @Test
  void testMesmaPontuacaoGanhaQuemTemMaisVitorias() {
    // Ana: 4 vitórias lentas = 12 pts. Bia: 3 vitórias relâmpago = 3 x 4 = 12 pts.
    List<ResultadoBatalha> resultados = new ArrayList<>();
    for (int i = 1; i <= 4; i++) {
      resultados.add(venceu("Ana", "Lento" + i, 10));
    }
    for (int i = 1; i <= 3; i++) {
      resultados.add(venceu("Bia", "Rapido" + i, 5));
    }
    when(repositorio.listarTodos()).thenReturn(resultados);

    List<LinhaRanking> tabela = ranking.gerarRanking();

    assertEquals("Ana", tabela.get(0).getTreinador());
    assertEquals("Bia", tabela.get(1).getTreinador());
    assertEquals(tabela.get(0).getPontos(), tabela.get(1).getPontos());
  }

  @Test
  void testFalhaNoRepositorioGeraRankingVazioRegistraErro() {
    // thenThrow: o método NÃO é void, então when(...).thenThrow(...) é a forma natural.
    when(repositorio.listarTodos()).thenThrow(new IllegalStateException("sem conexão"));

    List<LinhaRanking> tabela = ranking.gerarRanking();

    assertTrue(tabela.isEmpty());
    verify(registro).registrarErro(contains("ranking"), any(IllegalStateException.class));
  }

  @Test
  void testSemResultadosNaoGeraLinhasNemErros() {
    when(repositorio.listarTodos()).thenReturn(Collections.<ResultadoBatalha>emptyList());

    assertTrue(ranking.gerarRanking().isEmpty());

    verify(registro, never()).registrarErro(any(), any());
    verifyNoMoreInteractions(registro);
  }

  @Test
  void testConstrutorExigeColaboradores() {
    assertThrows(NullPointerException.class, () -> new RankingTreinadores(null, registro));
    assertThrows(NullPointerException.class, () -> new RankingTreinadores(repositorio, null));
  }

  private static void assertLinha(LinhaRanking linha, int posicao, String nome, int vitorias,
      int derrotas, int pontos, int turnos) {
    assertEquals(posicao, linha.getPosicao());
    assertEquals(nome, linha.getTreinador());
    assertEquals(vitorias, linha.getVitorias());
    assertEquals(derrotas, linha.getDerrotas());
    assertEquals(pontos, linha.getPontos());
    assertEquals(turnos, linha.getTurnosTotais());
  }
}
