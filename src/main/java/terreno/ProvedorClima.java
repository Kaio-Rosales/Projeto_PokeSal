package terreno;

import model.elementos.Clima;

/**
 * Serviço EXTERNO de clima. Em produção poderia ser uma API de previsão do tempo; aqui existe uma
 * simulação ({@link ProvedorClimaAleatorio}). Por ser uma interface, os testes podem mockar o clima.
 */
public interface ProvedorClima {

  /** Consulta o clima neste momento. */
  Clima obterClima();
}
