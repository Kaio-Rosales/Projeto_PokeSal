package model.elementos;

/** Enumeração para quardar os tipos elementais e o método de dano elemental.*/
public enum TipoElemental {
  GRAMA, FOGO, AGUA, NORMAL;

  /** Método que compara os elementos e retorna a efetividade do ataque como um double.*/
  public double danoElemental(TipoElemental x, TipoElemental y) {
    double retorno = 1;
    if (x.equals(FOGO) && y.equals(GRAMA)) {
      retorno = 2.0;
    }
    if (x.equals(FOGO) && y.equals(AGUA)) {
      retorno = 0.5;
    }
    if (x.equals(AGUA) && y.equals(FOGO)) {
      retorno = 2.0;
    }
    if (x.equals(AGUA) && y.equals(GRAMA)) {
      retorno = 0.5;
    }
    if (x.equals(GRAMA) && y.equals(AGUA)) {
      retorno = 2.0;
    }
    if (x.equals(GRAMA) && y.equals(FOGO)) {
      retorno = 0.5;
    }
    return retorno;
  }

}
