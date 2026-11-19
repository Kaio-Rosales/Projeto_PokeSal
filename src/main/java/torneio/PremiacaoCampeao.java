package torneio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import model.itens.Antidote;
import model.itens.Item;
import model.itens.MegaPotion;
import model.itens.ReturnPass;
import model.itens.SuperPotion;
import model.jogador.Jogador;
import registro.RegistroBatalha;

/**
 * REQUISITO AUTORAL 2 (RA-02): premiação do campeão com notificação.
 *
 * <p>Ao final do torneio o campeão recebe itens na mochila, conforme o tamanho do torneio:
 * <ul>
 *   <li>4 treinadores: 1 Super Poção + 1 Antídoto;</li>
 *   <li>8 treinadores: 2 Mega Poções + 1 Super Poção + 1 Passe de Retorno.</li>
 * </ul>
 * Depois, o {@link NotificadorTorneio} anuncia o campeão. Se a notificação FALHAR, os prêmios
 * já entregues são mantidos e o erro é registrado (o prêmio é do jogador, a notificação é só um
 * aviso).
 *
 * <p>Colaboradores externos (mockáveis): {@code NotificadorTorneio} e {@code RegistroBatalha}.
 */
public class PremiacaoCampeao {

  private final NotificadorTorneio notificador;
  private final RegistroBatalha registro;

  /** Recebe o canal de notificação e o registro para avisar de falhas. */
  public PremiacaoCampeao(NotificadorTorneio notificador, RegistroBatalha registro) {
    this.notificador = Objects.requireNonNull(notificador, "notificador");
    this.registro = Objects.requireNonNull(registro, "registro");
  }

  /**
   * Entrega os prêmios, notifica e devolve a descrição de cada prêmio (ex.: "2x Mega Poção").
   *
   * @throws IllegalArgumentException se o campeão for nulo ou o torneio não tiver 4 ou 8 pessoas
   */
  public List<String> premiar(Jogador campeao, int totalParticipantes) {
    if (campeao == null) {
      throw new IllegalArgumentException("O campeão é obrigatório");
    }
    if (!Torneio.tamanhoValido(totalParticipantes)) {
      throw new IllegalArgumentException("O torneio exige 4 ou 8 treinadores");
    }

    List<String> descricoes = new ArrayList<>();
    for (Item premio : montarPremios(totalParticipantes)) {
      descricoes.add(premio.getQuantidade() + "x " + premio.getNome());
      campeao.adicionarItem(premio);
    }
    notificar(campeao, descricoes);
    return descricoes;
  }

  private void notificar(Jogador campeao, List<String> descricoes) {
    try {
      notificador.notificarCampeao(campeao.getNome(), descricoes);
    } catch (RuntimeException falha) {
      registro.registrarErro("Não foi possível notificar o campeão " + campeao.getNome()
          + "; os prêmios foram entregues mesmo assim", falha);
    }
  }

  private static List<Item> montarPremios(int totalParticipantes) {
    List<Item> premios = new ArrayList<>();
    if (totalParticipantes == 4) {
      premios.add(comQuantidade(new SuperPotion(), 1));
      premios.add(comQuantidade(new Antidote(), 1));
    } else {
      premios.add(comQuantidade(new MegaPotion(), 2));
      premios.add(comQuantidade(new SuperPotion(), 1));
      premios.add(comQuantidade(new ReturnPass(), 1));
    }
    return premios;
  }

  private static Item comQuantidade(Item item, int quantidade) {
    item.setQuantidade(quantidade);
    return item;
  }
}
