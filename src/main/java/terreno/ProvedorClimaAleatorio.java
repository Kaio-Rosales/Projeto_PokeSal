package terreno;

import java.util.Objects;
import aleatorio.GeradorAleatorio;
import model.elementos.Clima;

/**
 * Simula o serviço de clima sorteando uma condição. Cada consulta equivale a "olhar o céu agora".
 * Mantém a mesma chance (1 em 4) que o antigo sorteio de terrenos de {@code Servicos}.
 */
public class ProvedorClimaAleatorio implements ProvedorClima {

  private final GeradorAleatorio gerador;

  /** Recebe o gerador de números aleatórios. */
  public ProvedorClimaAleatorio(GeradorAleatorio gerador) {
    this.gerador = Objects.requireNonNull(gerador, "gerador");
  }

  @Override
  public Clima obterClima() {
    Clima[] climas = Clima.values();
    return climas[gerador.proximoInt(climas.length)];
  }
}
