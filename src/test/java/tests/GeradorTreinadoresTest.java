package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aleatorio.GeradorAleatorio;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.jogador.Jogador;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import model.pokesal.PokeSal;
import model.pokesal.TotoSal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import torneio.GeradorTreinadores;

/**
 * Testes da montagem de adversários. MOCKADO: {@code GeradorAleatorio}.
 *
 * <p>Detalhe: espécie e variação de nível usam o mesmo limite (3). Para cada membro de time a
 * classe sorteia PRIMEIRO a variação de nível e DEPOIS a espécie; os retornos em sequência abaixo
 * seguem essa ordem.
 */
@ExtendWith(MockitoExtension.class)
class GeradorTreinadoresTest {

  @Mock
  private GeradorAleatorio gerador;
  @InjectMocks
  private GeradorTreinadores geradorTreinadores;

  private static Jogador humano(PokeSal... time) {
    return new Jogador("Você", new ArrayList<>(Arrays.asList(time)), new ArrayList<>());
  }

  @Test
  void testMontaAdversariosComEspeciesNiveisSorteados() {
    Jogador voce = humano(new ChikoSal(5));
    // Ana: variação 1 (nível 5) + espécie 0 (Chiko); Bruno: variação 2 (nível 6) + espécie 1
    // (Toto); Carla: variação 0 (nível 4) + espécie 2 (Cynda).
    when(gerador.proximoInt(3)).thenReturn(1, 0, 2, 1, 0, 2);

    List<Jogador> participantes = geradorTreinadores.montarParticipantes(voce, 4);

    assertEquals(4, participantes.size());
    assertSame(voce, participantes.get(0), "O jogador humano vem sempre em primeiro");
    assertEquals("Ana", participantes.get(1).getNome());
    assertEquals("Bruno", participantes.get(2).getNome());
    assertEquals("Carla", participantes.get(3).getNome());
    PokeSal ana = participantes.get(1).getTime().get(0);
    PokeSal bruno = participantes.get(2).getTime().get(0);
    assertTrue(ana instanceof ChikoSal);
    assertEquals(5, ana.getNivel());
    assertTrue(bruno instanceof TotoSal);
    assertEquals(6, bruno.getNivel());

    PokeSal carla = participantes.get(3).getTime().get(0);
    assertTrue(carla instanceof CyndaSal);
    assertEquals(4, carla.getNivel());
  }

  @Test
  void testTorneioDeOitoTemSetesAdversariosComNomesUnicosUmaPocaoCada() {
    when(gerador.proximoInt(3)).thenReturn(1);

    List<Jogador> participantes =
        geradorTreinadores.montarParticipantes(humano(new ChikoSal(3)), 8);

    assertEquals(8, participantes.size());
    Set<String> nomes = new HashSet<>();
    for (Jogador participante : participantes) {
      nomes.add(participante.getNome());
    }
    assertEquals(8, nomes.size(), "Os nomes precisam ser únicos (o ranking usa o nome)");
    Jogador adversario = participantes.get(7);
    assertEquals(1, adversario.getMochila().size());
    assertEquals(1, adversario.getMochila().get(0).getQuantidade());
  }

  @Test
  void testTimeDosAdversariosTemMesmoTamanhoDoTimeDoJogador() {
    when(gerador.proximoInt(3)).thenReturn(1);

    List<Jogador> participantes = geradorTreinadores.montarParticipantes(
        humano(new ChikoSal(4), new TotoSal(4), new CyndaSal(4)), 4);

    assertEquals(3, participantes.get(1).getTime().size());
    assertEquals(3, participantes.get(3).getTime().size());
  }

  @Test
  void testNivelDosAdversariosFicaEntre1E10() {
    // Variação 2 => nível base + 1. Com base 10 o resultado seria 11 => limitado a 10.
    when(gerador.proximoInt(3)).thenReturn(2, 0);
    List<Jogador> noTeto = geradorTreinadores.montarParticipantes(humano(new ChikoSal(10)), 4);
    assertEquals(10, noTeto.get(1).getTime().get(0).getNivel());
  }

  @Test
  void testNivelNaoFicaAbaixoDeUm() {
    // Variação 0 => nível base - 1. Com base 1 o resultado seria 0 => limitado a 1.
    when(gerador.proximoInt(3)).thenReturn(0);
    List<Jogador> noPiso = geradorTreinadores.montarParticipantes(humano(new ChikoSal(1)), 4);
    assertEquals(1, noPiso.get(1).getTime().get(0).getNivel());
  }

  @Test
  void testJogadorSemTimeGeraAdversariosDeUmPokeSalNivelUm() {
    when(gerador.proximoInt(3)).thenReturn(1);

    List<Jogador> participantes = geradorTreinadores.montarParticipantes(humano(), 4);

    assertEquals(1, participantes.get(1).getTime().size());
    assertEquals(1, participantes.get(1).getTime().get(0).getNivel());
  }

  @Test
  void testAdversarioNuncaTemMesmoNomeDoJogador() {
    // REGRESSÃO (achada no teste de fumaça): jogador "ana" + adversário "Ana" => "Ana venceu Ana"
    // e o ranking misturava os dois. Agora o nome repetido é pulado.
    when(gerador.proximoInt(3)).thenReturn(1);
    Jogador ana = new Jogador("ana", new ArrayList<>(Arrays.<PokeSal>asList(new ChikoSal(3))),
        new ArrayList<>());

    List<Jogador> participantes = geradorTreinadores.montarParticipantes(ana, 8);

    assertEquals(8, participantes.size());
    Set<String> nomes = new HashSet<>();
    for (Jogador participante : participantes) {
      assertTrue(nomes.add(participante.getNome().toLowerCase()),
          "Nome repetido: " + participante.getNome());
    }
    assertTrue(nomes.contains("henrique"), "O 8º nome do conjunto substitui o que foi pulado");
  }

  @Test
  void testTamanhoInvalidoNaoSorteiaNada() {
    assertThrows(IllegalArgumentException.class,
        () -> geradorTreinadores.montarParticipantes(humano(new ChikoSal()), 5));
    verify(gerador, never()).proximoInt(anyInt());
  }

  @Test
  void testGeradorObrigatorio() {
    assertThrows(NullPointerException.class, () -> new GeradorTreinadores(null));
  }
}
