package torneio;

import java.util.List;

/**
 * Canal que avisa alguém sobre acontecimentos do torneio (e-mail, push, mural...). Colaborador
 * externo: pode falhar, e por isso é uma interface que os testes mockam.
 */
public interface NotificadorTorneio {

  /** Anuncia o campeão e a lista de prêmios que ele recebeu. */
  void notificarCampeao(String nomeCampeao, List<String> premios);
}
