package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import model.elementos.EfeitoTerreno;
import model.elementos.StatusEfeito;
import model.itens.Potion;
import model.pokesal.Ataque;
import model.pokesal.ChikoSal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import registro.RegistroBatalhaConsole;

/**
 * Teste do adaptador de console. Sem Mockito: capturamos a saída em memória, porque o
 * {@code PrintStream} é recebido pelo construtor (esse é o benefício da injeção).
 */
class RegistroBatalhaConsoleTest {

  private ByteArrayOutputStream saida;
  private ByteArrayOutputStream saidaErro;
  private RegistroBatalhaConsole registro;
  private ChikoSal chiko;

  @BeforeEach
  void montar() {
    saida = new ByteArrayOutputStream();
    saidaErro = new ByteArrayOutputStream();
    registro = new RegistroBatalhaConsole(
        new PrintStream(saida, true, StandardCharsets.UTF_8),
        new PrintStream(saidaErro, true, StandardCharsets.UTF_8));
    chiko = new ChikoSal();
  }

  private String texto() {
    return new String(saida.toByteArray(), StandardCharsets.UTF_8);
  }

  @Test
  void testInicioDeBatalhaIdentificaOsDoisTreinadores() {
    registro.registrarInicioBatalha("Ana", "Bruno");

    assertTrue(texto().contains("=== Batalha: Ana x Bruno ==="));
  }

  @Test
  void testAtaqueQueAcertaNaoMencionaErro() {
    registro.registrarAtaque(chiko, chiko.ataques().get(1), true);

    assertTrue(texto().contains("ChikoSal usou Cabeçada!"));
    assertFalse(texto().contains("errou"));
  }

  @Test
  void testAtaqueQueErraAvisaQueErrouAlvo() {
    registro.registrarAtaque(chiko, chiko.ataques().get(0), false);

    assertTrue(texto().contains("ChikoSal usou Lâmina de Navalha!"));
    assertTrue(texto().contains("errou o alvo"));
  }

  @Test
  void testDanoCriticoSuperEfetivoQueDerrubaAlvo() {
    chiko.setHpAtual(0);

    registro.registrarDano(chiko, 15, true, 2.0);

    String saidaTexto = texto();
    assertTrue(saidaTexto.contains("Acerto crítico!"));
    assertTrue(saidaTexto.contains("É super efetivo!"));
    assertTrue(saidaTexto.contains("ChikoSal sofreu 15 de dano! (HP: 0/40)"));
    assertTrue(saidaTexto.contains("ChikoSal desmaiou!"));
  }

  @Test
  void testDanoPoucoEfetivoNaoMencionaCriticoNemDesmaio() {
    registro.registrarDano(chiko, 3, false, 0.5);

    assertTrue(texto().contains("Não é muito efetivo..."));
    assertFalse(texto().contains("crítico"));
    assertFalse(texto().contains("desmaiou"));
  }

  @Test
  void testDanoNeutroNaoMencionaEfetividade() {
    registro.registrarDano(chiko, 3, false, 1.0);

    assertFalse(texto().contains("efetivo"));
    assertTrue(texto().contains("sofreu 3 de dano"));
  }

  @Test
  void testStatusTerrenoItemSaoImpressos() {
    registro.registrarEfeitoStatus(chiko, StatusEfeito.QUEIMADO, "descrição do status");
    registro.registrarEfeitoTerreno(chiko, EfeitoTerreno.TERRENO_FLORIDO, "descrição do terreno");
    registro.registrarUsoItem(new Potion(), chiko);

    String saidaTexto = texto();
    assertTrue(saidaTexto.contains("descrição do status"));
    assertTrue(saidaTexto.contains("descrição do terreno"));
    assertTrue(saidaTexto.contains("Poção foi usado em ChikoSal."));
  }

  @Test
  void testErroVaiParaSaidaDeErroComPelaCausa() {
    registro.registrarErro("Falha ao salvar", new IllegalStateException("disco cheio"));
    registro.registrarErro("Sem causa", null);

    String erros = new String(saidaErro.toByteArray(), StandardCharsets.UTF_8);
    assertTrue(erros.contains("[ERRO] Falha ao salvar: disco cheio"));
    assertTrue(erros.contains("[ERRO] Sem causa"));
    assertEquals("", texto(), "Erros não devem poluir a saída normal");
  }

  @Test
  void testSaidasSaoObrigatorias() {
    PrintStream qualquer = new PrintStream(new ByteArrayOutputStream());
    assertThrows(NullPointerException.class, () -> new RegistroBatalhaConsole(null, qualquer));
    assertThrows(NullPointerException.class, () -> new RegistroBatalhaConsole(qualquer, null));
    Ataque ataque = chiko.ataques().get(0);
    assertEquals("Lâmina de Navalha", ataque.getNome());
  }
}
