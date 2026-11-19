package tests;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aleatorio.GeradorAleatorio;
import batalha.ExecutorBatalhaPadrao;
import batalha.MecanicaCombate;
import batalha.RepositorioResultados;
import batalha.ResultadoBatalha;
import java.util.ArrayList;
import java.util.Arrays;
import model.elementos.EfeitoTerreno;
import model.jogador.Jogador;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import model.pokesal.PokeSal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import registro.RegistroBatalha;
import terreno.ServicoTerreno;

/** A fábrica de batalhas cria uma {@code Batalha} nova por confronto e a leva até o fim. */
@ExtendWith(MockitoExtension.class)
class ExecutorBatalhaPadraoTest {

  @Mock
  private GeradorAleatorio gerador;
  @Mock
  private ServicoTerreno servicoTerreno;
  @Mock
  private RepositorioResultados repositorio;
  @Mock
  private RegistroBatalha registro;

  private Jogador treinador(String nome, PokeSal pokeSal) {
    return new Jogador(nome, new ArrayList<>(Arrays.asList(pokeSal)), new ArrayList<>());
  }

  @Test
  void testLutarDisputaBatalhaInteiraSalvaResultado() {
    doReturn(0).when(gerador).proximoInt(anyInt());
    doReturn(9_999).when(gerador).proximoInt(10_000);
    when(servicoTerreno.obterEfeitoAtual()).thenReturn(EfeitoTerreno.NENHUM);
    ExecutorBatalhaPadrao executor = new ExecutorBatalhaPadrao(
        new MecanicaCombate(gerador, registro), servicoTerreno, repositorio, registro);
    Jogador forte = treinador("Forte", new CyndaSal(10));
    Jogador fraco = treinador("Fraco", new ChikoSal(1));

    ResultadoBatalha resultado = executor.lutar(forte, fraco);

    assertSame(forte, resultado.getVencedor());
    assertSame(fraco, resultado.getPerdedor());
    verify(repositorio).salvar(resultado);
  }

  @Test
  void testColaboradoresSaoObrigatorios() {
    MecanicaCombate mecanica = new MecanicaCombate(gerador, registro);
    assertThrows(NullPointerException.class,
        () -> new ExecutorBatalhaPadrao(null, servicoTerreno, repositorio, registro));
    assertThrows(NullPointerException.class,
        () -> new ExecutorBatalhaPadrao(mecanica, null, repositorio, registro));
    assertThrows(NullPointerException.class,
        () -> new ExecutorBatalhaPadrao(mecanica, servicoTerreno, null, registro));
    assertThrows(NullPointerException.class,
        () -> new ExecutorBatalhaPadrao(mecanica, servicoTerreno, repositorio, null));
  }
}
