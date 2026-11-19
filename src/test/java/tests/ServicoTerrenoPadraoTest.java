package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import model.elementos.Clima;
import model.elementos.EfeitoTerreno;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import terreno.ProvedorClima;
import terreno.ServicoTerrenoPadrao;

/**
 * Testes do {@code ServicoTerrenoPadrao}: o efeito depende do CLIMA e do HORÁRIO.
 *
 * <p>MOCKADOS: {@code ProvedorClima} (serviço externo) e, no primeiro teste, o {@code Clock}.
 * Nos testes de fronteira usamos {@code Clock.fixed(...)} REAL: é um "dublê" determinístico da
 * própria biblioteca e dispensa configurar {@code instant()} e {@code getZone()} à mão.
 */
@ExtendWith(MockitoExtension.class)
class ServicoTerrenoPadraoTest {

  @Mock
  private ProvedorClima provedorClima;
  @Mock
  private Clock relogio;
  @InjectMocks
  private ServicoTerrenoPadrao servico;

  @Test
  void testAsfaltoQuenteApenasDuranteDia() {
    when(provedorClima.obterClima()).thenReturn(Clima.ENSOLARADO);
    when(relogio.getZone()).thenReturn(ZoneOffset.UTC);
    // Retornos em sequência: a 1ª leitura do relógio é meio-dia; a 2ª, meia-noite.
    when(relogio.instant()).thenReturn(
        Instant.parse("2026-11-17T12:00:00Z"),
        Instant.parse("2026-11-17T23:59:00Z"));

    assertEquals(EfeitoTerreno.ASFALTO_QUENTE, servico.obterEfeitoAtual(), "Dia ensolarado");
    assertEquals(EfeitoTerreno.NENHUM, servico.obterEfeitoAtual(), "Noite ensolarada");

    verify(relogio, times(2)).instant();
    verify(provedorClima, times(2)).obterClima();
  }

  @ParameterizedTest(name = "{0} => {1}")
  @CsvSource({
    "05:59:59, NENHUM",
    "06:00:00, ASFALTO_QUENTE",
    "12:00:00, ASFALTO_QUENTE",
    "17:59:59, ASFALTO_QUENTE",
    "18:00:00, NENHUM",
    "00:00:00, NENHUM"
  })
  void testFronteirasDoHorarioDeDia(String horario, EfeitoTerreno esperado) {
    when(provedorClima.obterClima()).thenReturn(Clima.ENSOLARADO);
    Clock relogioFixo = Clock.fixed(Instant.parse("2026-11-17T" + horario + "Z"), ZoneOffset.UTC);

    ServicoTerrenoPadrao servicoComRelogioFixo =
        new ServicoTerrenoPadrao(provedorClima, relogioFixo);

    assertEquals(esperado, servicoComRelogioFixo.obterEfeitoAtual());
  }

  @Test
  void testChuvaMolhaChaoEmQualquerHorarioSemConsultarRelogio() {
    when(provedorClima.obterClima()).thenReturn(Clima.CHUVOSO);

    assertEquals(EfeitoTerreno.CHAO_MOLHADO, servico.obterEfeitoAtual());

    verify(relogio, never()).instant(); // chuva não depende da hora
  }

  @Test
  void testNubladoFloresceVentosoNaoTemEfeito() {
    when(provedorClima.obterClima()).thenReturn(Clima.NUBLADO, Clima.VENTOSO);

    assertEquals(EfeitoTerreno.TERRENO_FLORIDO, servico.obterEfeitoAtual());
    assertEquals(EfeitoTerreno.NENHUM, servico.obterEfeitoAtual());
  }

  @Test
  void testConstrutorExigeColaboradores() {
    assertThrows(NullPointerException.class, () -> new ServicoTerrenoPadrao(null, relogio));
    assertThrows(NullPointerException.class, () -> new ServicoTerrenoPadrao(provedorClima, null));
  }
}
