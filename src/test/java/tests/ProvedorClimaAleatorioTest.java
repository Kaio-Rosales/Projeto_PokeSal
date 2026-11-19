package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aleatorio.GeradorAleatorio;
import model.elementos.Clima;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import terreno.ProvedorClimaAleatorio;

/** O provedor simulado sorteia uma das 4 condições do tempo usando o gerador injetado. */
@ExtendWith(MockitoExtension.class)
class ProvedorClimaAleatorioTest {

  @Mock
  private GeradorAleatorio gerador;
  @InjectMocks
  private ProvedorClimaAleatorio provedor;

  @Test
  void testCadaIndiceSorteadoCorrespondeUmClima() {
    when(gerador.proximoInt(4)).thenReturn(0, 1, 2, 3);

    assertEquals(Clima.ENSOLARADO, provedor.obterClima());
    assertEquals(Clima.CHUVOSO, provedor.obterClima());
    assertEquals(Clima.NUBLADO, provedor.obterClima());
    assertEquals(Clima.VENTOSO, provedor.obterClima());
    verify(gerador, times(4)).proximoInt(4);
  }

  @Test
  void testGeradorObrigatorio() {
    assertThrows(NullPointerException.class, () -> new ProvedorClimaAleatorio(null));
  }
}
