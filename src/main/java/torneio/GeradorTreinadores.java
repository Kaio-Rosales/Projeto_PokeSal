package torneio;

import aleatorio.GeradorAleatorio;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import model.itens.Item;
import model.itens.Potion;
import model.jogador.Jogador;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import model.pokesal.PokeSal;
import model.pokesal.TotoSal;

/**
 * Monta os adversários do torneio: treinadores controlados pelo computador, com times do mesmo
 * tamanho do time do jogador e níveis próximos ao nível médio dele (-1, 0 ou +1).
 */
public class GeradorTreinadores {

  /** São 8 nomes para que, mesmo se o jogador usar um deles, ainda sobrem 7 adversários. */
  private static final String[] NOMES = {
    "Ana", "Bruno", "Carla", "Diego", "Elisa", "Fábio", "Gabriela", "Henrique"
  };
  private static final int NIVEL_MINIMO = 1;
  private static final int NIVEL_MAXIMO = 10;
  private static final int ESPECIES = 3;
  private static final int VARIACAO_NIVEL = 3;

  private final GeradorAleatorio gerador;

  /** Recebe o gerador usado para sortear espécies e níveis. */
  public GeradorTreinadores(GeradorAleatorio gerador) {
    this.gerador = Objects.requireNonNull(gerador, "gerador");
  }

  /**
   * Devolve a lista de participantes: o jogador humano primeiro e depois os adversários.
   *
   * @throws IllegalArgumentException se {@code total} não for 4 nem 8
   */
  public List<Jogador> montarParticipantes(Jogador humano, int total) {
    if (!Torneio.tamanhoValido(total)) {
      throw new IllegalArgumentException("O torneio exige 4 ou 8 treinadores");
    }
    int tamanhoTime = Math.max(1, humano.getTime().size());
    int nivelBase = nivelMedio(humano);

    List<Jogador> participantes = new ArrayList<>();
    participantes.add(humano);
    for (String nome : NOMES) {
      if (participantes.size() == total) {
        break;
      }
      // Nomes iguais quebrariam o ranking (a chave é o nome): pulamos o nome do jogador.
      if (!nome.equalsIgnoreCase(humano.getNome())) {
        participantes.add(criarAdversario(nome, tamanhoTime, nivelBase));
      }
    }
    return participantes;
  }

  private Jogador criarAdversario(String nome, int tamanhoTime, int nivelBase) {
    List<PokeSal> time = new ArrayList<>();
    for (int i = 0; i < tamanhoTime; i++) {
      int nivel = nivelBase + gerador.proximoInt(VARIACAO_NIVEL) - 1;
      nivel = Math.max(NIVEL_MINIMO, Math.min(NIVEL_MAXIMO, nivel));
      time.add(criarPokeSal(gerador.proximoInt(ESPECIES), nivel));
    }
    List<Item> mochila = new ArrayList<>();
    Potion pocao = new Potion();
    pocao.setQuantidade(1);
    mochila.add(pocao);
    return new Jogador(nome, time, mochila);
  }

  private static PokeSal criarPokeSal(int especie, int nivel) {
    if (especie == 0) {
      return new ChikoSal(nivel);
    }
    if (especie == 1) {
      return new TotoSal(nivel);
    }
    return new CyndaSal(nivel);
  }

  private static int nivelMedio(Jogador jogador) {
    if (jogador.getTime().isEmpty()) {
      return NIVEL_MINIMO;
    }
    int soma = 0;
    for (PokeSal pokeSal : jogador.getTime()) {
      soma += pokeSal.getNivel();
    }
    return soma / jogador.getTime().size();
  }
}
