package servicos;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;

import aleatorio.GeradorAleatorio;
import batalha.MecanicaCombate;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import model.itens.Item;
import model.jogador.Jogador;
import model.pokesal.PokeSal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import registro.RegistroBatalha;
import terreno.ServicoTerreno;

/**
 * Base dos testes de {@code Servicos}: monta os colaboradores mockados, simula o TECLADO com um
 * {@link Scanner} sobre um texto e captura o CONSOLE para conferir mensagens.
 *
 * <p>Estes testes ficam no pacote {@code servicos} (e não em {@code tests}) porque
 * {@code getOponente()}, {@code getLocalizacao()} e {@code getEfeitoTerrenoAtual()} são
 * pacote-privados: expô-los como públicos só para testar pioraria o encapsulamento.
 *
 * <p>MOCKADOS: {@code GeradorAleatorio}, {@code ServicoTerreno}, {@code RegistroBatalha}.
 * REAIS: {@code MecanicaCombate}, {@code Jogador} e PokeSals.
 */
@ExtendWith(MockitoExtension.class)
abstract class ServicosTestBase {

  @Mock
  protected GeradorAleatorio gerador;
  @Mock
  protected ServicoTerreno servicoTerreno;
  @Mock
  protected RegistroBatalha registro;

  private PrintStream saidaOriginal;
  private ByteArrayOutputStream buffer;

  @BeforeEach
  void capturarConsole() {
    saidaOriginal = System.out;
    buffer = new ByteArrayOutputStream();
    System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
  }

  @AfterEach
  void restaurarConsole() {
    System.setOut(saidaOriginal);
  }

  /** Tudo o que o jogo imprimiu até agora. */
  protected String saida() {
    return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
  }

  /** Cria o serviço lendo {@code teclado} como se o jogador tivesse digitado esse texto. */
  protected Servicos servicos(Jogador jogador, String teclado) {
    return new Servicos(jogador, new Scanner(teclado), new MecanicaCombate(gerador, registro),
        servicoTerreno, gerador, registro);
  }

  protected static Jogador jogador(PokeSal... time) {
    return new Jogador("Ana", new ArrayList<>(Arrays.asList(time)), new ArrayList<Item>());
  }

  /**
   * Sorteio "sem surpresas": tudo 0 (golpes acertam, IA escolhe o 1º golpe, status passa),
   * exceto o crítico, que nunca ocorre. Veja o comentário equivalente em {@code BatalhaTest}.
   */
  protected void sorteioSemSurpresas() {
    doReturn(0).when(gerador).proximoInt(anyInt());
    doReturn(9_999).when(gerador).proximoInt(10_000);
  }
}
