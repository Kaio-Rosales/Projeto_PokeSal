package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aleatorio.GeradorAleatorio;
import batalha.Batalha;
import batalha.MecanicaCombate;
import batalha.RepositorioResultados;
import batalha.ResultadoBatalha;
import java.util.ArrayList;
import java.util.Arrays;
import model.elementos.EfeitoTerreno;
import model.elementos.StatusEfeito;
import model.itens.Item;
import model.itens.Potion;
import model.itens.Spray;
import model.jogador.Jogador;
import model.pokesal.Ataque;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import model.pokesal.PokeSal;
import model.pokesal.TotoSal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import registro.RegistroBatalha;
import terreno.ServicoTerreno;

/**
 * Testes da {@code Batalha} (luta automática entre dois treinadores) com Mockito.
 *
 * <p>MOCKADOS: {@code GeradorAleatorio}, {@code ServicoTerreno}, {@code RepositorioResultados} e
 * {@code RegistroBatalha} (as quatro fronteiras do sistema). REAIS: {@code MecanicaCombate}
 * (queremos exercitar a integração Batalha + regras de combate), {@code Jogador} e os PokeSals.
 *
 * <p>Por que não há {@code @InjectMocks} aqui? O construtor de {@code Batalha} recebe também os
 * dois {@code Jogador}es (objetos reais), e o Mockito só injeta mocks. Montamos a batalha com
 * {@link #novaBatalha}. Nos demais arquivos de teste {@code @InjectMocks} é usado normalmente.
 */
@ExtendWith(MockitoExtension.class)
class BatalhaTest {

  @Mock
  private GeradorAleatorio gerador;
  @Mock
  private ServicoTerreno servicoTerreno;
  @Mock
  private RepositorioResultados repositorio;
  @Mock
  private RegistroBatalha registro;

  private MecanicaCombate mecanica;

  @BeforeEach
  void montarMecanica() {
    mecanica = new MecanicaCombate(gerador, registro);
  }

  // ------------------------------------------------------------------ apoio

  private static Jogador treinador(String nome, PokeSal... time) {
    return new Jogador(nome, new ArrayList<>(Arrays.asList(time)), new ArrayList<>());
  }

  private Batalha novaBatalha(Jogador a, Jogador b) {
    return new Batalha(a, b, mecanica, servicoTerreno, repositorio, registro);
  }

  /**
   * Sorteio "sem surpresas": todo sorteio devolve 0 (IA escolhe o 1º golpe; golpes acertam;
   * chance de status sempre passa), EXCETO o crítico, que nunca acontece (9999).
   * A 2ª linha usa doReturn() porque já existe um stub genérico para o mesmo método.
   */
  private void sorteioSemSurpresas() {
    doReturn(0).when(gerador).proximoInt(anyInt());
    doReturn(9_999).when(gerador).proximoInt(10_000);
  }

  private void terrenoNormal() {
    when(servicoTerreno.obterEfeitoAtual()).thenReturn(EfeitoTerreno.NENHUM);
  }

  // ------------------------------------------------------------------ ordem dos eventos
  @Test
  void testSequenciaDeEventosNoTurno() {
    // Cenário montado para que os CINCO tipos de evento aconteçam no mesmo turno:
    //  - Cynda (nv 5) é mais rápida e ataca primeiro com Arranhão;
    //  - Chiko (nv 5) responde com Folha Mágica, que ENVENENA a Cynda;
    //  - terreno florido cura a Chiko (Grama) no fim do turno;
    //  - Chiko termina o turno com pouco HP e usa uma Poção.
    ChikoSal chiko = new ChikoSal(5);                  // hpMax 56, ATK 55, SPD 52
    chiko.setHpAtual(30);
    Potion pocao = new Potion();
    pocao.setQuantidade(1);
    Jogador treinadorChiko = treinador("Ana", chiko);
    treinadorChiko.getMochila().add(pocao);

    sorteioSemSurpresas();
    doReturn(2).when(gerador).proximoInt(3);      // Chiko tem 3 golpes: o índice 2 é Folha Mágica
    when(servicoTerreno.obterEfeitoAtual()).thenReturn(EfeitoTerreno.TERRENO_FLORIDO);

    CyndaSal cynda = new CyndaSal(5);                  // hpMax 42, ATK 71, SPD 70
    Ataque arranhao = cynda.ataques().get(0);
    Jogador treinadorCynda = treinador("Bruno", cynda);
    Batalha batalha = novaBatalha(treinadorChiko, treinadorCynda);

    batalha.executarTurno();
    assertFalse(batalha.terminou(), "Os dois PokeSals sobrevivem ao turno");
    Ataque folhaMagica = chiko.ataques().get(2);
    // InOrder: as chamadas ao registro devem ocorrer NESTA ordem (valores exatos de propósito:
    // se a fórmula do dano mudar sem querer, este teste avisa).
    //   Arranhão: 10 + 71/3 = 33 - DEF(80)/10 = 25.   Folha Mágica: round((15 + 55/3) x 0,5) = 17
    //   - DEF(55)/10 = 12.   Veneno: 5% de 42 = 2.   Terreno: 5% de 56 = 2.
    InOrder ordem = inOrder(registro);
    ordem.verify(registro).registrarAtaque(cynda, arranhao, true);
    ordem.verify(registro).registrarDano(chiko, 25, false, 1.0);
    ordem.verify(registro).registrarAtaque(chiko, folhaMagica, true);
    ordem.verify(registro).registrarDano(cynda, 12, false, 0.5);
    ordem.verify(registro)
        .registrarEfeitoStatus(eq(cynda), eq(StatusEfeito.ENVENENADO), contains("afetado"));
    ordem.verify(registro)
        .registrarEfeitoStatus(eq(cynda), eq(StatusEfeito.ENVENENADO), contains("veneno"));
    ordem.verify(registro)
        .registrarEfeitoTerreno(eq(chiko), eq(EfeitoTerreno.TERRENO_FLORIDO), contains("2 de HP"));
    ordem.verify(registro).registrarUsoItem(pocao, chiko);
  }

  @Test
  void testOrdemEntreTiposDeEventoComAtLeastOnce() {
    // Mesmo cenário, mas verificando só a ORDEM ENTRE TIPOS com matchers genéricos: any() é
    // adequado quando o valor exato não importa e queremos apenas "aconteceu depois de".
    ChikoSal chiko = new ChikoSal(5);
    chiko.setHpAtual(30);
    Potion pocao = new Potion();
    pocao.setQuantidade(1);
    Jogador treinadorChiko = treinador("Ana", chiko);
    treinadorChiko.getMochila().add(pocao);
    sorteioSemSurpresas();
    doReturn(2).when(gerador).proximoInt(3);
    when(servicoTerreno.obterEfeitoAtual()).thenReturn(EfeitoTerreno.TERRENO_FLORIDO);
    CyndaSal cynda = new CyndaSal(5);

    novaBatalha(treinadorChiko, treinador("Bruno", cynda)).executarTurno();

    InOrder ordem = inOrder(registro);
    ordem.verify(registro, atLeastOnce())
        .registrarAtaque(any(), any(), anyBoolean());
    ordem.verify(registro, atLeastOnce())
        .registrarDano(any(), anyInt(), anyBoolean(), anyDouble());
    ordem.verify(registro, atLeastOnce())
        .registrarEfeitoStatus(any(), any(), anyString());
    ordem.verify(registro, atLeastOnce())
        .registrarEfeitoTerreno(any(), any(), anyString());
    ordem.verify(registro, atLeastOnce())
        .registrarUsoItem(any(), any());
  }

  // ------------------------------------------------------------------ resultado e repositório

  @Test
  void testResultadoSalvoAoFimDaBatalha() {
    // Cynda nv 10 (ATK 101, SPD 95) derruba a Chiko nv 1 (40 HP) em 2 turnos: 38 de dano por golpe.
    sorteioSemSurpresas();
    terrenoNormal();

    // ArgumentCaptor: "pega" o objeto que a batalha enviou ao repositório para inspecioná-lo.
    ArgumentCaptor<ResultadoBatalha> captor = ArgumentCaptor.forClass(ResultadoBatalha.class);
    verify(repositorio, times(1)).salvar(captor.capture());
    ResultadoBatalha salvo = captor.getValue();

    CyndaSal cynda = new CyndaSal(10);
    ChikoSal chiko = new ChikoSal(1);

    Jogador vencedorEsperado = treinador("Ana", cynda);
    Jogador perdedorEsperado = treinador("Bruno", chiko);
    assertSame(vencedorEsperado, salvo.getVencedor());
    assertSame(perdedorEsperado, salvo.getPerdedor());
    assertEquals(2, salvo.getTurnos());
    ResultadoBatalha devolvido = novaBatalha(vencedorEsperado, perdedorEsperado).executarAteFim();
    assertEquals(Arrays.<PokeSal>asList(cynda), salvo.getPokesalUtilizadosVencedor());
    assertEquals(Arrays.<PokeSal>asList(chiko), salvo.getPokesalUtilizadosPerdedor());
    assertSame(salvo, devolvido, "O resultado salvo é o mesmo que a batalha devolve");
  }

  @Test
  void testFalhaNoRepositorioNaoInvalidaBatalha() {
    sorteioSemSurpresas();
    terrenoNormal();
    // doThrow() é a forma de fazer um método VOID lançar exceção (thenThrow só serve p/ não-void).
    doThrow(new IllegalStateException("banco fora do ar"))
        .when(repositorio).salvar(any(ResultadoBatalha.class));

    Jogador vencedorEsperado = treinador("Ana", new CyndaSal(10));
    Jogador perdedorEsperado = treinador("Bruno", new ChikoSal(1));

    Batalha batalha = novaBatalha(vencedorEsperado, perdedorEsperado);
    ResultadoBatalha resultado = batalha.executarAteFim();   // não pode lançar exceção

    assertTrue(batalha.terminou());
    assertSame(vencedorEsperado, resultado.getVencedor(), "O vencedor continua definido");
    assertSame(resultado, batalha.getResultado());
    ArgumentCaptor<Throwable> causa = ArgumentCaptor.forClass(Throwable.class);
    verify(registro).registrarErro(contains("Não foi possível salvar"), causa.capture());
    assertEquals("banco fora do ar", causa.getValue().getMessage());
  }

  @Test
  void testRepositorioNaoChamadoSeBatalhaNaoTerminou() {
    // Chiko nv 10 x Toto nv 10: depois de UM turno os dois seguem de pé (65 e 28 de HP).
    Batalha batalha = novaBatalha(treinador("Ana", new ChikoSal(10)),
        treinador("Bruno", new TotoSal(10)));
    sorteioSemSurpresas();
    terrenoNormal();

    batalha.executarTurno();

    assertFalse(batalha.terminou());
    assertNull(batalha.getResultado());
    assertEquals(1, batalha.getTurnos());
    verify(repositorio, never()).salvar(any(ResultadoBatalha.class));
    verify(registro, never()).registrarErro(any(), any());
  }

  @Test
  void testInicioDaBatalhaAnunciadoUmaUnicaVezComOsNomesDosTreinadores() {
    sorteioSemSurpresas();
    terrenoNormal();
    Batalha batalha = novaBatalha(treinador("Ana", new CyndaSal(10)),
        treinador("Bruno", new ChikoSal(1)));

    batalha.executarAteFim();    // 2 turnos

    verify(registro, times(1)).registrarInicioBatalha("Ana", "Bruno");
  }

  @Test
  void testTerrenoConsultadoUmaUnicaVezPorBatalha() {
    CyndaSal cynda = new CyndaSal(10);
    sorteioSemSurpresas();
    terrenoNormal();

    novaBatalha(treinador("Ana", cynda), treinador("Bruno", new ChikoSal(1))).executarAteFim();

    verify(servicoTerreno, times(1)).obterEfeitoAtual(); // 2 turnos, 1 consulta: terreno estável
  }

  // ------------------------------------------------------------------ troca de PokeSal

  @Test
  void testQuandoPokeSalDesmaiaProximoDoTimeEntraEmCampo() {
    CyndaSal cynda = new CyndaSal(10);
    ChikoSal primeiro = new ChikoSal(1);
    ChikoSal segundo = new ChikoSal(1);
    sorteioSemSurpresas();
    terrenoNormal();

    ResultadoBatalha resultado = novaBatalha(treinador("Ana", cynda),
        treinador("Bruno", primeiro, segundo)).executarAteFim();

    assertEquals(4, resultado.getTurnos(), "2 golpes para cada Chiko");
    assertEquals(Arrays.<PokeSal>asList(primeiro, segundo),
        resultado.getPokesalUtilizadosPerdedor());
  }

  // ------------------------------------------------------------------ casos de borda

  @Test
  void testTempoEsgotadoVenceQuemTemMaisHpNoTime() {
    sorteioSemSurpresas();
    terrenoNormal();
    Jogador ana = treinador("Ana", new ChikoSal(10));      // termina o turno 1 com 65 HP
    Jogador bruno = treinador("Bruno", new TotoSal(10));   // termina o turno 1 com 28 HP

    ResultadoBatalha resultado = new Batalha(ana, bruno, mecanica, servicoTerreno, repositorio,
        registro, 1).executarAteFim();

    assertSame(ana, resultado.getVencedor());
    assertEquals(1, resultado.getTurnos());
  }

  @Test
  void testTempoEsgotadoComVantagemDoSegundoTreinadorFazEleVencer() {
    sorteioSemSurpresas();
    terrenoNormal();
    Jogador bruno = treinador("Bruno", new TotoSal(10));
    Jogador ana = treinador("Ana", new ChikoSal(10));

    ResultadoBatalha resultado = new Batalha(bruno, ana, mecanica, servicoTerreno, repositorio,
        registro, 1).executarAteFim();

    assertSame(ana, resultado.getVencedor(), "Ana (2º argumento) termina com mais HP");
  }

  @Test
  void testAmbosDesmaiamNoMesmoTurnoVencePrimeiroTreinador() {
    // Ana ataca primeiro e derruba o Toto, mas morre de veneno no fim do mesmo turno.
    ChikoSal envenenada = new ChikoSal(10);
    envenenada.setHpAtual(1);
    envenenada.setStatus(StatusEfeito.ENVENENADO);
    TotoSal frageil = new TotoSal(10);
    frageil.setHpAtual(1);
    Jogador ana = treinador("Ana", envenenada);
    Jogador bruno = treinador("Bruno", frageil);
    sorteioSemSurpresas();
    terrenoNormal();

    ResultadoBatalha resultado = novaBatalha(ana, bruno).executarAteFim();

    assertFalse(envenenada.estaVivo());
    assertFalse(frageil.estaVivo());
    assertSame(ana, resultado.getVencedor(), "Regra de desempate: cabeça de chave (1º treinador)");
    assertEquals(1, resultado.getTurnos());
  }

  // ------------------------------------------------------------------ itens da IA

  /** Cenário "cada golpe causa 1 de dano": ATK 0 e DEF enorme nos dois lados. */
  private static ChikoSal chikoDeDanoMinimo() {
    ChikoSal chiko = new ChikoSal(10);
    chiko.setAtk(0);
    chiko.setDef(100_000);
    return chiko;
  }

  @Test
  void testIaUsaNoMaximoDuasPocoesPorBatalha() {
    ChikoSal chikoAna = chikoDeDanoMinimo();
    chikoAna.setHpAtual(3);
    Potion pocoes = new Potion();
    pocoes.setQuantidade(5);
    Jogador ana = treinador("Ana", chikoAna);
    ana.getMochila().add(pocoes);
    Jogador bruno = treinador("Bruno", chikoDeDanoMinimo());
    sorteioSemSurpresas();
    terrenoNormal();

    ResultadoBatalha resultado = novaBatalha(ana, bruno).executarAteFim();

    assertSame(bruno, resultado.getVencedor(), "Sem poções extras, Ana acaba derrotada");
    assertEquals(3, pocoes.getQuantidade(), "Das 5 poções, só 2 podem ser usadas por batalha");
    verify(registro, times(2)).registrarUsoItem(pocoes, chikoAna);
  }

  @Test
  void testIaIgnoraItensQueNaoCuramOuQueEstaoZerados() {
    ChikoSal chikoAna = chikoDeDanoMinimo();
    chikoAna.setHpAtual(3);
    Spray spray = new Spray();
    spray.setQuantidade(1);
    Potion pocaoZerada = new Potion();             // quantidade 0
    Jogador ana = treinador("Ana", chikoAna);
    ana.getMochila().add(spray);
    ana.getMochila().add(pocaoZerada);
    sorteioSemSurpresas();
    terrenoNormal();

    novaBatalha(ana, treinador("Bruno", chikoDeDanoMinimo())).executarAteFim();

    assertEquals(1, spray.getQuantidade());
    verify(registro, never()).registrarUsoItem(any(Item.class), any(PokeSal.class));
  }

  // ------------------------------------------------------------------ validações

  @Test
  void testTurnoAposFimLancaExcecao() {
    sorteioSemSurpresas();
    terrenoNormal();
    Batalha batalha = novaBatalha(treinador("Ana", new CyndaSal(10)),
        treinador("Bruno", new ChikoSal(1)));
    batalha.executarAteFim();

    assertThrows(IllegalStateException.class, batalha::executarTurno);
    assertNotNull(batalha.getResultado());
  }

  @Test
  void testConstrutorRejeitaTreinadoresInvalidos() {
    Jogador ana = treinador("Ana", new ChikoSal());
    Jogador semVivos = treinador("Zé", new ChikoSal());
    semVivos.getTime().get(0).setHpAtual(0);

    assertThrows(IllegalArgumentException.class, () -> novaBatalha(null, ana));
    assertThrows(IllegalArgumentException.class, () -> novaBatalha(ana, null));
    assertThrows(IllegalArgumentException.class, () -> novaBatalha(ana, ana));
    assertThrows(IllegalArgumentException.class, () -> novaBatalha(ana, semVivos));
    Jogador semTime = treinador("Sem Time");
    assertThrows(IllegalArgumentException.class, () -> novaBatalha(semTime, ana));
  }

  @Test
  void testConstrutorRejeitaLimiteDeTurnosInvalidoColaboradoresNulos() {
    Jogador ana = treinador("Ana", new ChikoSal());
    Jogador bruno = treinador("Bruno", new TotoSal());

    assertThrows(IllegalArgumentException.class,
        () -> new Batalha(ana, bruno, mecanica, servicoTerreno, repositorio, registro, 0));
    assertThrows(NullPointerException.class,
        () -> new Batalha(ana, bruno, null, servicoTerreno, repositorio, registro));
    assertThrows(NullPointerException.class,
        () -> new Batalha(ana, bruno, mecanica, null, repositorio, registro));
    assertThrows(NullPointerException.class,
        () -> new Batalha(ana, bruno, mecanica, servicoTerreno, null, registro));
    assertThrows(NullPointerException.class,
        () -> new Batalha(ana, bruno, mecanica, servicoTerreno, repositorio, null));
  }
}
