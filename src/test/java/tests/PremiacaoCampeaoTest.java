package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.itens.Item;
import model.itens.SuperPotion;
import model.jogador.Jogador;
import model.pokesal.PokeSal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import registro.RegistroBatalha;
import torneio.NotificadorTorneio;
import torneio.PremiacaoCampeao;

/**
 * Testes do REQUISITO AUTORAL 2 (RA-02): premiação do campeão com notificação.
 *
 * <p>MOCKADOS: {@code NotificadorTorneio} (canal externo que pode falhar) e
 * {@code RegistroBatalha}. REAIS: {@code Jogador} e os itens (são entidades).
 */
@ExtendWith(MockitoExtension.class)
class PremiacaoCampeaoTest {

  @Mock
  private NotificadorTorneio notificador;
  @Mock
  private RegistroBatalha registro;
  @InjectMocks
  private PremiacaoCampeao premiacao;
  @Captor
  private ArgumentCaptor<List<String>> premiosCaptor;

  private static Jogador campeao() {
    return new Jogador("Ana", new ArrayList<PokeSal>(), new ArrayList<Item>());
  }

  private static int quantidadeNaMochila(Jogador jogador, String nomeDoItem) {
    for (Item item : jogador.getMochila()) {
      if (item.getNome().equals(nomeDoItem)) {
        return item.getQuantidade();
      }
    }
    return 0;
  }

  @Test
  void testTorneioDeQuatroPremiaSuperPocaoAntidotoNotifica() {
    Jogador ana = campeao();

    List<String> premios = premiacao.premiar(ana, 4);

    assertEquals(Arrays.asList("1x Super Poção", "1x Antidoto"), premios);
    assertEquals(1, quantidadeNaMochila(ana, "Super Poção"));
    assertEquals(1, quantidadeNaMochila(ana, "Antidoto"));
    // ArgumentCaptor: confere exatamente o que foi enviado ao notificador.
    verify(notificador).notificarCampeao(eq("Ana"),
        premiosCaptor.capture());
    assertEquals(premios, premiosCaptor.getValue());
  }

  @Test
  void testTorneioDeOitoPremiaMaisEmpilhaComQueCampeaoJaTinha() {
    Jogador ana = campeao();
    SuperPotion jaTinha = new SuperPotion();
    jaTinha.setQuantidade(2);
    ana.getMochila().add(jaTinha);

    List<String> premios = premiacao.premiar(ana, 8);

    assertEquals(Arrays.asList("2x Mega Poção", "1x Super Poção", "1x Passe de Retorno"), premios);
    assertEquals(2, quantidadeNaMochila(ana, "Mega Poção"));
    assertEquals(3, quantidadeNaMochila(ana, "Super Poção"), "2 que já tinha + 1 de prêmio");
    assertEquals(3, ana.getMochila().size(), "Super Poção NÃO pode aparecer em duas linhas");
    assertEquals(1, quantidadeNaMochila(ana, "Passe de Retorno"));
  }

  @Test
  void testFalhaNaNotificacaoNaoCancelaOsPremios() {
    Jogador ana = campeao();
    // doThrow(): forma usada para métodos void (notificarCampeao não devolve nada).
    doThrow(new IllegalStateException("e-mail indisponível"))
        .when(notificador).notificarCampeao(anyString(), anyList());

    List<String> premios = premiacao.premiar(ana, 4);

    assertEquals(2, premios.size());
    assertEquals(1, quantidadeNaMochila(ana, "Super Poção"), "Prêmio mantido mesmo sem aviso");
    verify(registro).registrarErro(contains("Não foi possível notificar"),
        any(IllegalStateException.class));
  }

  @Test
  void testPremiosJaEstaoNaMochilaQuandoNotificadorChamado() {
    Jogador ana = campeao();
    // doAnswer(): executa código no momento exato da chamada do mock. Aqui provamos a ORDEM
    // "primeiro entrega, depois avisa" olhando a mochila dentro da chamada.
    doAnswer(invocacao -> {
      assertEquals(2, ana.getMochila().size(), "Os prêmios já deveriam estar na mochila");
      return null;
    }).when(notificador).notificarCampeao(anyString(), anyList());

    premiacao.premiar(ana, 4);

    verify(notificador).notificarCampeao(anyString(), anyList());
  }

  @Test
  void testEntradaInvalidaNaoPremiaNemNotifica() {
    Jogador ana = campeao();

    assertThrows(IllegalArgumentException.class, () -> premiacao.premiar(null, 4));
    assertThrows(IllegalArgumentException.class, () -> premiacao.premiar(ana, 6));

    assertTrue(ana.getMochila().isEmpty());
    verify(notificador, never()).notificarCampeao(any(), any());
  }

  @Test
  void testConstrutorExigeColaboradores() {
    assertThrows(NullPointerException.class, () -> new PremiacaoCampeao(null, registro));
    assertThrows(NullPointerException.class, () -> new PremiacaoCampeao(notificador, null));
  }
}
