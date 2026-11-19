package torneio;

/** Uma linha da classificação geral: o desempenho acumulado de um treinador. */
public class LinhaRanking {

  private final int posicao;
  private final String treinador;
  private final int vitorias;
  private final int derrotas;
  private final int pontos;
  private final int turnosTotais;

  LinhaRanking(int posicao, String treinador, int vitorias, int derrotas, int pontos,
      int turnosTotais) {
    this.posicao = posicao;
    this.treinador = treinador;
    this.vitorias = vitorias;
    this.derrotas = derrotas;
    this.pontos = pontos;
    this.turnosTotais = turnosTotais;
  }

  public int getPosicao() {
    return posicao;
  }

  public String getTreinador() {
    return treinador;
  }

  public int getVitorias() {
    return vitorias;
  }

  public int getDerrotas() {
    return derrotas;
  }

  public int getPontos() {
    return pontos;
  }

  public int getTurnosTotais() {
    return turnosTotais;
  }
}
