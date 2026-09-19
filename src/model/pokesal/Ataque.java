package model.pokesal;

import model.elementos.TipoElemental;

/** Classe que determina um golpe que pode usar em batalha, liberado a partir de um nível mínimo.*/
public class Ataque {
  private final String nome;
  private final TipoElemental tipo;
  private final int danoBase;
  private final int divisorAtk;
  private final int nivelMinimo;

  /** Cria um ataque com dano = danoBase + atk / divisorAtk, liberado no nível mínimo.*/
  public Ataque(String nome, TipoElemental tipo, int danoBase, int divisorAtk,
      int nivelMinimo) {
    this.nome = nome;
    this.tipo = tipo;
    this.danoBase = danoBase;
    this.divisorAtk = divisorAtk;
    this.nivelMinimo = nivelMinimo;
  }

  public String getNome() {
    return nome;
  }

  public TipoElemental getTipo() {
    return tipo;
  }

  public int getNivelMinimo() {
    return nivelMinimo;
  }

  /** Verifica se um pokeSal do nível informado já pode usar este ataque.*/
  public boolean podeUsar(int nivel) {
    return nivel >= nivelMinimo;
  }

  /** Dano bruto do ataque.*/
  public int calcularDano(int atk) {
    return danoBase + atk / divisorAtk;
  }
}
