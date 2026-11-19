package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import batalha.RepositorioResultadosEmMemoria;
import batalha.ResultadoAtaque;
import batalha.ResultadoBatalha;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import model.elementos.StatusEfeito;
import model.jogador.Jogador;
import model.pokesal.ChikoSal;
import model.pokesal.PokeSal;
import org.junit.jupiter.api.Test;

/** Testes de objetos simples (sem colaboradores): repositório em memória e os resultados. */
class RepositorioResultadosEmMemoriaTest {

  private static Jogador jogador(String nome) {
    return new Jogador(nome, new ArrayList<>(), new ArrayList<>());
  }

  private static ResultadoBatalha resultado(String vencedor, String perdedor) {
    return new ResultadoBatalha(jogador(vencedor), jogador(perdedor), 3,
        Collections.<PokeSal>emptyList(), Collections.<PokeSal>emptyList());
  }

  @Test
  void testGuardaOsResultadosNaOrdemDeChegada() {
    RepositorioResultadosEmMemoria repositorio = new RepositorioResultadosEmMemoria();
    ResultadoBatalha primeiro = resultado("Ana", "Bruno");
    ResultadoBatalha segundo = resultado("Carla", "Diego");

    repositorio.salvar(primeiro);
    repositorio.salvar(segundo);

    assertEquals(2, repositorio.listarTodos().size());
    assertSame(primeiro, repositorio.listarTodos().get(0));
    assertSame(segundo, repositorio.listarTodos().get(1));
  }

  @Test
  void testListaDevolvidaNaoAlteraEstadoInterno() {
    RepositorioResultadosEmMemoria repositorio = new RepositorioResultadosEmMemoria();
    repositorio.salvar(resultado("Ana", "Bruno"));

    assertThrows(UnsupportedOperationException.class,
        () -> repositorio.listarTodos().add(resultado("X", "Y")));
    assertEquals(1, repositorio.listarTodos().size());
  }

  @Test
  void testResultadoNuloEhRejeitado() {
    assertThrows(NullPointerException.class,
        () -> new RepositorioResultadosEmMemoria().salvar(null));
  }

  @Test
  void testResultadoDeBatalhaCopiaAsListasRecebidas() {
    ChikoSal chiko = new ChikoSal();
    ArrayList<PokeSal> usadosOriginal = new ArrayList<>(Arrays.<PokeSal>asList(chiko));
    ResultadoBatalha resultado = new ResultadoBatalha(jogador("Ana"), jogador("Bruno"), 7,
        usadosOriginal, new ArrayList<PokeSal>());

    usadosOriginal.clear(); // mexer na lista original não pode alterar o resultado

    assertEquals(1, resultado.getPokesalUtilizadosVencedor().size());
    assertTrue(resultado.getPokesalUtilizadosPerdedor().isEmpty());
    assertEquals(7, resultado.getTurnos());
    assertThrows(UnsupportedOperationException.class,
        () -> resultado.getPokesalUtilizadosVencedor().clear());
  }

  @Test
  void testFabricasDeResultadoDeAtaque() {
    ResultadoAtaque semAcao = ResultadoAtaque.semAcao();
    assertFalse(semAcao.agiu());
    assertFalse(semAcao.acertou());

    ResultadoAtaque errou = ResultadoAtaque.errou();
    assertTrue(errou.agiu());
    assertFalse(errou.acertou());
    assertEquals(0, errou.getDano());

    ResultadoAtaque acertou = ResultadoAtaque.acertou(12, true, StatusEfeito.QUEIMADO);
    assertTrue(acertou.acertou());
    assertTrue(acertou.foiCritico());
    assertEquals(12, acertou.getDano());
    assertEquals(StatusEfeito.QUEIMADO, acertou.getStatusAplicado());
  }
}
