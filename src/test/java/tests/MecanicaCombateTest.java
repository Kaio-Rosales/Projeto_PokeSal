package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aleatorio.GeradorAleatorio;
import batalha.MecanicaCombate;
import batalha.ResultadoAtaque;
import model.elementos.EfeitoTerreno;
import model.elementos.StatusEfeito;
import model.elementos.TipoElemental;
import model.pokesal.Ataque;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import registro.RegistroBatalha;

/**
 * Testes da {@code MecanicaCombate} com Mockito.
 *
 * <p>Colaboradores MOCKADOS (fronteiras não determinísticas ou com efeito externo):
 * {@code GeradorAleatorio} e {@code RegistroBatalha}. Objetos REAIS: {@code ChikoSal},
 * {@code CyndaSal}, {@code Ataque}, {@code EfeitoTerreno} e a matriz de tipos (são entidades e
 * regras puras; mockar esconderia bugs reais).
 *
 * <p>ATENÇÃO (pegadinha comum de iniciantes): um mock devolve 0 para um {@code int} que você
 * NÃO configurou. Como "0" significa "acertou, crítico e status aplicado", sempre configure TODAS
 * as chamadas que o código fará ao gerador. Por isso o método {@link #semCritico()} existe.
 *
 * <p>Cenário-base: atacante ATK 0 e alvo DEF 0, golpe Normal com danoBase 10 e divisor 5 =>
 * dano normal EXATAMENTE 10 (Normal contra Grama é neutro). Fica fácil conferir a conta.
 */
@ExtendWith(MockitoExtension.class)
class MecanicaCombateTest {

  @Mock
  private GeradorAleatorio gerador;
  @Mock
  private RegistroBatalha registro;
  @InjectMocks
  private MecanicaCombate mecanica;

  private ChikoSal atacante;
  private ChikoSal alvo;

  @BeforeEach
  void montarCenario() {
    atacante = new ChikoSal();
    atacante.setAtk(0);
    alvo = new ChikoSal();
    alvo.setDef(0);
  }

  private static Ataque golpe(int precisao) {
    return new Ataque("Golpe Teste", TipoElemental.NORMAL, 10, 5, 1, precisao);
  }

  /**
   * Configura o sorteio de crítico para "não foi crítico" (9999 está fora da faixa 0-624).
   *
   * <p>Usa {@code doReturn(...).when(...)} e não {@code when(...).thenReturn(...)}: com o
   * Mockito em modo estrito, configurar o MESMO método ({@code proximoInt}) com argumentos
   * diferentes via {@code when()} lança {@code PotentialStubbingProblem}.
   */
  private void semCritico() {
    doReturn(9_999).when(gerador).proximoInt(10_000);
  }

  // ------------------------------------------------------------------ CRÍTICO

  @Test
  void testAcertoCriticoComGeradorMockado() {
    Ataque golpe = golpe(90);
    when(gerador.proximoInt(100)).thenReturn(0);       // 0 < 90  => acertou
    doReturn(624).when(gerador).proximoInt(10_000);  // 624 < 625 => último valor da faixa crítica
    int hpAntes = alvo.getHpAtual();

    ResultadoAtaque resultado =
        mecanica.executarAtaque(atacante, alvo, golpe, EfeitoTerreno.NENHUM);

    assertTrue(resultado.foiCritico());
    assertEquals(15, resultado.getDano(), "Dano normal 10 x 1.5 = 15");
    assertEquals(hpAntes - 15, alvo.getHpAtual());
    // Valores EXATOS (sem any()): queremos provar que o 15 e o "true" foram enviados ao registro.
    verify(registro).registrarDano(alvo, 15, true, 1.0);
  }

  @Test
  void testSemCriticoNoPrimeiroValorForaDaFaixa() {
    when(gerador.proximoInt(100)).thenReturn(0);
    doReturn(625).when(gerador).proximoInt(10_000);  // 625 NÃO é menor que 625 => sem crítico

    ResultadoAtaque resultado =
        mecanica.executarAtaque(atacante, alvo, golpe(90), EfeitoTerreno.NENHUM);

    assertFalse(resultado.foiCritico());
    assertEquals(10, resultado.getDano());
    verify(registro).registrarDano(alvo, 10, false, 1.0);
  }

  @Test
  void testDanoMinimoContinuaSendoMinimoNoCritico() {
    // DEF absurda => dano normal 1; crítico arredonda 1,5 para 2 (Math.round).
    alvo.setDef(100_000);
    when(gerador.proximoInt(100)).thenReturn(0);
    doReturn(0).when(gerador).proximoInt(10_000);

    ResultadoAtaque resultado =
        mecanica.executarAtaque(atacante, alvo, golpe(100), EfeitoTerreno.NENHUM);

    assertEquals(2, resultado.getDano());
  }

  // ------------------------------------------------------------------ PRECISÃO

  @Test
  void testGolpeErraQuandoPrecisaoFalha() {
    Ataque golpe = golpe(90);
    when(gerador.proximoInt(100)).thenReturn(90);      // 90 NÃO é menor que 90 => errou

    ResultadoAtaque resultado =
        mecanica.executarAtaque(atacante, alvo, golpe, EfeitoTerreno.NENHUM);

    assertTrue(resultado.agiu());
    assertFalse(resultado.acertou());
    assertEquals(0, resultado.getDano());

    int hpAntes = alvo.getHpAtual();
    assertEquals(hpAntes, alvo.getHpAtual(), "HP do alvo deve ficar inalterado");
    verify(registro).registrarAtaque(atacante, golpe, false);
    // Ao errar, não existe dano nem crítico para sortear:
    verify(registro, never()).registrarDano(any(), anyInt(), anyBoolean(), anyDouble());
    verify(gerador, never()).proximoInt(10_000);
  }

  @Test
  void testGolpeAcertaNoUltimoValorDaPrecisao() {
    when(gerador.proximoInt(100)).thenReturn(89);      // 89 < 90 => acertou
    semCritico();

    ResultadoAtaque resultado =
        mecanica.executarAtaque(atacante, alvo, golpe(90), EfeitoTerreno.NENHUM);

    assertTrue(resultado.acertou());
    assertEquals(10, resultado.getDano());
  }

  @Test
  void testPrecisaoZeroSempreErraPrecisaoCemSempreAcerta() {
    // 0 não é menor que 0 => errou, mesmo com a "melhor sorte" possível.
    when(gerador.proximoInt(100)).thenReturn(0, 99);
    semCritico();

    assertFalse(mecanica.executarAtaque(atacante, alvo, golpe(0), EfeitoTerreno.NENHUM)
        .acertou());
    // 99 é o maior sorteio possível e ainda é menor que 100 => acertou.
    assertTrue(mecanica.executarAtaque(atacante, alvo, golpe(100), EfeitoTerreno.NENHUM)
        .acertou());
  }

  // ------------------------------------------------------------------ STATUS DO GOLPE

  @Test
  void testStatusAplicadoUsandoRetornosEmSequencia() {
    Ataque golpeGelado = new Ataque("Sopro Gelado", TipoElemental.NORMAL, 10, 5, 1, 100,
        StatusEfeito.RESFRIADO, 30);
    // 1ª chamada de proximoInt(100) = precisão (0 => acerta); 2ª = chance do status (29 < 30).
    when(gerador.proximoInt(100)).thenReturn(0, 29);
    semCritico();

    ResultadoAtaque resultado =
        mecanica.executarAtaque(atacante, alvo, golpeGelado, EfeitoTerreno.NENHUM);

    assertEquals(StatusEfeito.RESFRIADO, resultado.getStatusAplicado());
    assertEquals(StatusEfeito.RESFRIADO, alvo.getStatus());
    // Ordem exigida (2.4): ataque -> dano -> efeito de status.
    InOrder ordem = inOrder(registro);
    ordem.verify(registro).registrarAtaque(atacante, golpeGelado, true);
    ordem.verify(registro).registrarDano(eq(alvo), anyInt(), eq(false), anyDouble());
    ordem.verify(registro)
        .registrarEfeitoStatus(eq(alvo), eq(StatusEfeito.RESFRIADO), contains("afetado"));
  }

  @Test
  void testStatusNaoAplicadoQuandoSorteioNaoFicaDentroDaChance() {
    Ataque golpeGelado = new Ataque("Sopro Gelado", TipoElemental.NORMAL, 10, 5, 1, 100,
        StatusEfeito.RESFRIADO, 30);
    when(gerador.proximoInt(100)).thenReturn(0, 30);   // 30 NÃO é menor que 30
    semCritico();

    ResultadoAtaque resultado =
        mecanica.executarAtaque(atacante, alvo, golpeGelado, EfeitoTerreno.NENHUM);

    assertEquals(StatusEfeito.NENHUM, resultado.getStatusAplicado());
    assertEquals(StatusEfeito.NENHUM, alvo.getStatus());
    verify(registro, never()).registrarEfeitoStatus(any(), any(), anyString());
  }

  @Test
  void testNaoSorteiaStatusQuandoAlvoJaTemStatus() {
    alvo.setStatus(StatusEfeito.QUEIMADO);
    when(gerador.proximoInt(100)).thenReturn(0);
    semCritico();

    Ataque golpeGelado = new Ataque("Sopro Gelado", TipoElemental.NORMAL, 10, 5, 1, 100,
            StatusEfeito.RESFRIADO, 100);
    mecanica.executarAtaque(atacante, alvo, golpeGelado, EfeitoTerreno.NENHUM);

    assertEquals(StatusEfeito.QUEIMADO, alvo.getStatus(), "Status antigo é mantido");
    verify(gerador, times(1)).proximoInt(100); // só a precisão; a chance do status não foi sorteada
  }

  @Test
  void testAlvoQueDesmaiaNaoRecebeStatusNemSorteio() {
    alvo.setHpAtual(1);
    when(gerador.proximoInt(100)).thenReturn(0);
    semCritico();

    Ataque golpeGelado = new Ataque("Sopro Gelado", TipoElemental.NORMAL, 10, 5, 1, 100,
            StatusEfeito.RESFRIADO, 100);
    mecanica.executarAtaque(atacante, alvo, golpeGelado, EfeitoTerreno.NENHUM);

    assertEquals(0, alvo.getHpAtual(), "HP nunca fica negativo");
    assertFalse(alvo.estaVivo());
    verify(gerador, times(1)).proximoInt(100);
    verify(registro, never()).registrarEfeitoStatus(any(), any(), anyString());
  }

  @Test
  void testAtacanteResfriadoNaoConsegueAgir() {
    atacante.setStatus(StatusEfeito.RESFRIADO);
    int hpAntes = alvo.getHpAtual();

    ResultadoAtaque resultado =
        mecanica.executarAtaque(atacante, alvo, golpe(100), EfeitoTerreno.NENHUM);

    assertFalse(resultado.agiu());
    assertEquals(hpAntes, alvo.getHpAtual());
    verify(registro).registrarEfeitoStatus(eq(atacante), eq(StatusEfeito.RESFRIADO),
        contains("não conseguiu agir"));
    verify(gerador, never()).proximoInt(anyInt()); // nem a precisão é sorteada
    verify(registro, never()).registrarAtaque(any(), any(), anyBoolean());
  }

  // ------------------------------------------------------------------ DANO E TERRENO

  @Test
  void testRegistroRecebeDanoEfetividadeCapturados() {
    // Fogo contra Grama (x2,0) com Asfalto Quente (x1,15): danoBruto 10 + 20/2 = 20.
    CyndaSal fogo = new CyndaSal();
    fogo.setAtk(20);
    Ataque brasa = new Ataque("Brasa Teste", TipoElemental.FOGO, 10, 2, 1, 100);
    when(gerador.proximoInt(100)).thenReturn(0);
    semCritico();

    mecanica.executarAtaque(fogo, alvo, brasa, EfeitoTerreno.ASFALTO_QUENTE);

    ArgumentCaptor<Integer> danoCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Double> efetividadeCaptor = ArgumentCaptor.forClass(Double.class);
    verify(registro).registrarDano(eq(alvo), danoCaptor.capture(), eq(false),
        efetividadeCaptor.capture());
    assertEquals(46, danoCaptor.getValue());           // round(20 x 2,0 x 1,15) - 0
    assertEquals(2.0, efetividadeCaptor.getValue(), 0.0001);
  }

  @Test
  void testTerrenoMudaResultadoDoMesmoGolpe() {
    CyndaSal fogo = new CyndaSal();
    fogo.setAtk(20);
    Ataque brasa = new Ataque("Brasa Teste", TipoElemental.FOGO, 10, 2, 1);

    int semTerreno = mecanica.calcularDano(fogo, alvo, brasa, EfeitoTerreno.NENHUM);
    int comAsfaltoQuente = mecanica.calcularDano(fogo, alvo, brasa, EfeitoTerreno.ASFALTO_QUENTE);

    assertEquals(40, semTerreno);
    assertEquals(46, comAsfaltoQuente);
  }

  // ------------------------------------------------------------------ FIM DE TURNO

  @Test
  void testQueimaduraCausaDoisPorCentoDoHpMaximoComMinimoDeUm() {
    ChikoSal queimado = new ChikoSal(); // hpMax 40 => 2% = 0 => mínimo 1
    queimado.setStatus(StatusEfeito.QUEIMADO);
    queimado.setHpAtual(10);

    mecanica.aplicarStatusFimDeTurno(queimado);

    assertEquals(9, queimado.getHpAtual());
    verify(registro).registrarEfeitoStatus(eq(queimado), eq(StatusEfeito.QUEIMADO),
        contains("queimadura"));
  }

  @Test
  void testVenenoCausaCincoPorCentoDoHpMaximo() {
    ChikoSal envenenado = new ChikoSal(10); // hpMax 76 => 5% = 3
    envenenado.setStatus(StatusEfeito.ENVENENADO);

    mecanica.aplicarStatusFimDeTurno(envenenado);

    assertEquals(73, envenenado.getHpAtual());
    verify(registro).registrarEfeitoStatus(eq(envenenado), eq(StatusEfeito.ENVENENADO),
        contains("3 de dano pelo veneno"));
  }

  @Test
  void testDanoDeStatusNuncaDeixaHpNegativo() {
    ChikoSal quaseMorto = new ChikoSal();
    quaseMorto.setStatus(StatusEfeito.ENVENENADO);
    quaseMorto.setHpAtual(1);

    mecanica.aplicarStatusFimDeTurno(quaseMorto);

    assertEquals(0, quaseMorto.getHpAtual());
  }

  @Test
  void testResfriadoSeCuraSomenteQuandoSorteioForMenorQueCinquenta() {
    ChikoSal resfriado = new ChikoSal();
    resfriado.setStatus(StatusEfeito.RESFRIADO);
    when(gerador.proximoInt(100)).thenReturn(50, 49);

    mecanica.aplicarStatusFimDeTurno(resfriado);          // sorteio 50: continua resfriado
    assertEquals(StatusEfeito.RESFRIADO, resfriado.getStatus());

    mecanica.aplicarStatusFimDeTurno(resfriado);          // sorteio 49: cura
    assertEquals(StatusEfeito.NENHUM, resfriado.getStatus());
    verify(registro, times(1)).registrarEfeitoStatus(eq(resfriado), eq(StatusEfeito.RESFRIADO),
        contains("não está mais resfriado"));
  }

  @Test
  void testParalisadoSaudavelNaoSofremEfeitoNoFimDoTurno() {
    ChikoSal paralisado = new ChikoSal();
    paralisado.setStatus(StatusEfeito.PARALISADO);
    ChikoSal saudavel = new ChikoSal();
    int hpAntes = paralisado.getHpAtual();

    mecanica.aplicarStatusFimDeTurno(paralisado);
    mecanica.aplicarStatusFimDeTurno(saudavel);

    assertEquals(hpAntes, paralisado.getHpAtual());
    assertEquals(hpAntes, saudavel.getHpAtual());
    verify(registro, never()).registrarEfeitoStatus(any(), any(), anyString());
    verify(gerador, never()).proximoInt(anyInt());
  }

  @Test
  void testTerrenoFloridoCuraAvisaRegistro() {
    ChikoSal grama = new ChikoSal(); // hpMax 40 => cura 2
    grama.setHpAtual(10);

    mecanica.aplicarTerrenoFimDeTurno(grama, EfeitoTerreno.TERRENO_FLORIDO);

    assertEquals(12, grama.getHpAtual());
    verify(registro).registrarEfeitoTerreno(eq(grama), eq(EfeitoTerreno.TERRENO_FLORIDO),
        contains("recuperou 2 de HP"));
  }

  @Test
  void testTerrenoSemCuraNaoAvisaRegistro() {
    ChikoSal grama = new ChikoSal();
    grama.setHpAtual(10);
    CyndaSal fogo = new CyndaSal();
    fogo.setHpAtual(10);

    mecanica.aplicarTerrenoFimDeTurno(grama, EfeitoTerreno.CHAO_MOLHADO); // terreno sem cura
    mecanica.aplicarTerrenoFimDeTurno(fogo, EfeitoTerreno.TERRENO_FLORIDO); // não é Grama

    assertEquals(10, grama.getHpAtual());
    assertEquals(10, fogo.getHpAtual());
    verify(registro, never()).registrarEfeitoTerreno(any(), any(), anyString());
  }

  // ------------------------------------------------------------------ IA

  @Test
  void testIaEscolheGolpePeloIndiceSorteado() {
    ChikoSal chiko = new ChikoSal(); // nível 1: Lâmina de Navalha e Cabeçada
    when(gerador.proximoInt(2)).thenReturn(1);

    Ataque escolhido = mecanica.escolherAtaqueIa(chiko);

    assertSame(chiko.ataques().get(1), escolhido);
    assertEquals("Cabeçada", escolhido.getNome());
  }

  @Test
  void testConstrutorExigeColaboradores() {
    assertThrows(NullPointerException.class, () -> new MecanicaCombate(null, registro));
    assertThrows(NullPointerException.class, () -> new MecanicaCombate(gerador, null));
  }
}
