package model.pokesal;

import model.elementos.StatusEfeito;
import model.elementos.TipoElemental;

/** Classe que determina um golpe que pode usar em batalha, liberado a partir de um nível mínimo.*/
public class Ataque {
  private final String nome;
  private final TipoElemental tipo;
  private final int danoBase;
  private final int divisorAtk;
  private final int nivelMinimo;
  private final StatusEfeito efeito;
  private final int chanceEfeito;

  /**
   * Cria um ataque com dano = danoBase + atk / divisorAtk, liberado no nível mínimo,
   * sem chance de aplicar efeito de status.*/
  public Ataque(String nome, TipoElemental tipo, int danoBase, int divisorAtk,
      int nivelMinimo) {
    this(nome, tipo, danoBase, divisorAtk, nivelMinimo, StatusEfeito.NENHUM, 0);
  }

  /** Cria um ataque que também possui uma chance (0-100) de aplicar um efeito de status no alvo.*/
  public Ataque(String nome, TipoElemental tipo, int danoBase, int divisorAtk,
      int nivelMinimo, StatusEfeito efeito, int chanceEfeito) {
    this.nome = nome;
    this.tipo = tipo;
    this.danoBase = danoBase;
    this.divisorAtk = divisorAtk;
    this.nivelMinimo = nivelMinimo;
    this.efeito = efeito;
    this.chanceEfeito = chanceEfeito;
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

  public StatusEfeito getEfeito() {
    return efeito;
  }

  public int getChanceEfeito() {
    return chanceEfeito;
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
