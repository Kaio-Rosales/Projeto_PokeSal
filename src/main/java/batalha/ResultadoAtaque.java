package batalha;

import model.elementos.StatusEfeito;

/**
 * Resumo imutável do que aconteceu em UMA tentativa de ataque. Serve para o chamador (e para os
 * testes) saber o desfecho sem precisar ler o console.
 */
public class ResultadoAtaque {

  private final boolean agiu;
  private final boolean acertou;
  private final boolean critico;
  private final int dano;
  private final StatusEfeito statusAplicado;

  private ResultadoAtaque(boolean agiu, boolean acertou, boolean critico, int dano,
      StatusEfeito statusAplicado) {
    this.agiu = agiu;
    this.acertou = acertou;
    this.critico = critico;
    this.dano = dano;
    this.statusAplicado = statusAplicado;
  }

  /** O atacante não conseguiu agir (ex.: está resfriado). */
  public static ResultadoAtaque semAcao() {
    return new ResultadoAtaque(false, false, false, 0, StatusEfeito.NENHUM);
  }

  /** O golpe foi usado, mas errou o alvo: dano zero. */
  public static ResultadoAtaque errou() {
    return new ResultadoAtaque(true, false, false, 0, StatusEfeito.NENHUM);
  }

  /** O golpe acertou, causou dano e possivelmente aplicou um status. */
  public static ResultadoAtaque acertou(int dano, boolean critico, StatusEfeito statusAplicado) {
    return new ResultadoAtaque(true, true, critico, dano, statusAplicado);
  }

  /**Retorna se houve um acerto, através de um booleano.*/
  public boolean acertou() {
    return acertou;
  }

  /**Retorna se houve ação ou não, através de um booleano.*/
  public boolean agiu() {
    return agiu;
  }

  /**Retorna se o ataque foi crítico ou não, através de um booleano.*/
  public boolean foiCritico() {
    return critico;
  }

  public int getDano() {
    return dano;
  }

  public StatusEfeito getStatusAplicado() {
    return statusAplicado;
  }
}
