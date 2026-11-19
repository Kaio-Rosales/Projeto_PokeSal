package model.elementos;

/**
 * Condição do tempo informada por um provedor de clima (ver {@code terreno.ProvedorClima}).
 *
 * <p>O clima sozinho não altera a batalha: quem decide o efeito no terreno é o
 * {@code ServicoTerreno}, combinando o clima com o horário.
 */
public enum Clima {
  ENSOLARADO, CHUVOSO, NUBLADO, VENTOSO
}
