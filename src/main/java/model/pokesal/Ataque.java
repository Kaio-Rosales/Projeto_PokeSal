package model.pokesal;

import model.elementos.StatusEfeito;
import model.elementos.TipoElemental;

/** Classe que determina um golpe que pode usar em batalha, liberado a partir de um nível mínimo.*/
public class Ataque {
  /** Precisão usada pelos construtores antigos (Fases 01 e 02): o golpe nunca erra. */
  public static final int PRECISAO_PADRAO = 100;

  private final String nome;
  private final TipoElemental tipo;
  private final int danoBase;
  private final int divisorAtk;
  private final int nivelMinimo;
  private final int precisao;
  private final StatusEfeito efeito;
  private final int chanceEfeito;

  /**
   * Cria um ataque com dano = danoBase + atk / divisorAtk, liberado no nível mínimo,
   * sem chance de aplicar efeito de status e com precisão 100.*/
  public Ataque(String nome, TipoElemental tipo, int danoBase, int divisorAtk,
      int nivelMinimo) {
    this(nome, tipo, danoBase, divisorAtk, nivelMinimo, PRECISAO_PADRAO,
        StatusEfeito.NENHUM, 0);
  }

  /** Cria um ataque que também possui uma chance (0-100) de aplicar um efeito de status no alvo.*/
  public Ataque(String nome, TipoElemental tipo, int danoBase, int divisorAtk,
      int nivelMinimo, StatusEfeito efeito, int chanceEfeito) {
    this(nome, tipo, danoBase, divisorAtk, nivelMinimo, PRECISAO_PADRAO, efeito, chanceEfeito);
  }

  /** FASE 03: cria um ataque sem efeito de status, informando a precisão (0 a 100).*/
  public Ataque(String nome, TipoElemental tipo, int danoBase, int divisorAtk,
      int nivelMinimo, int precisao) {
    this(nome, tipo, danoBase, divisorAtk, nivelMinimo, precisao, StatusEfeito.NENHUM, 0);
  }

  /**
   * FASE 03: construtor completo. A precisão (0 a 100) é a chance, em %, de o golpe acertar o alvo.
   *
   * @throws IllegalArgumentException se a precisão estiver fora de 0..100
   */
  public Ataque(String nome, TipoElemental tipo, int danoBase, int divisorAtk,
      int nivelMinimo, int precisao, StatusEfeito efeito, int chanceEfeito) {
    if (precisao < 0 || precisao > PRECISAO_PADRAO) {
      throw new IllegalArgumentException("Precisão deve estar entre 0 e 100, mas foi " + precisao);
    }
    this.nome = nome;
    this.tipo = tipo;
    this.danoBase = danoBase;
    this.divisorAtk = divisorAtk;
    this.nivelMinimo = nivelMinimo;
    this.precisao = precisao;
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

  public int getPrecisao() {
    return precisao;
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
