package model.pokesal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.elementos.TipoElemental;

/** Que determina os atributos do TotoSal. */
public class TotoSal implements PokeSal {

  private int nivel = 1;
  private int hpMax = 45 + (nivel * 4);
  private int hpAtual = 45 + (nivel * 4);
  private int atk = 52 + (nivel * 3);
  private int def = 42 + (nivel * 5);
  private int spd = 38 + (nivel * 2);
  private int xp = 0;
  private final TipoElemental elemento = TipoElemental.AGUA;
  private static final List<Ataque> ATAQUES = Arrays.asList(
      new Ataque("Pistola d'Água", TipoElemental.AGUA, 3, 2, 1),
      new Ataque("Mordida", TipoElemental.NORMAL, 5, 2, 5),
      new Ataque("Bomba Hidráulica", TipoElemental.AGUA, 15, 2, 8));

  /** Construtor vazio de TotoSal.*/
  public TotoSal() {
  }

  /** Construtor com atributo nível.*/
  public TotoSal(int nivel) {
    this.nivel = nivel;
    atualizarAtributos();
    this.hpAtual = hpMax;
  }

  public int getHpMax() {
    return hpMax;
  }

  public void setHpMax(int hpMax) {
    this.hpMax = hpMax;
  }

  public int getHpAtual() {
    return hpAtual;
  }

  public void setHpAtual(int hpAtual) {
    this.hpAtual = hpAtual;
  }

  public int getAtk() {
    return atk;
  }

  public void setAtk(int atk) {
    this.atk = atk;
  }

  public int getDef() {
    return def;
  }

  public void setDef(int def) {
    this.def = def;
  }

  public int getSpd() {
    return spd;
  }

  public void setSpd(int spd) {
    this.spd = spd;
  }

  public TipoElemental getElemento() {
    return elemento;
  }

  public int getNivel() {
    return nivel;
  }

  public int getXp() {
    return xp;
  }

  public void setXp(int xp) {
    this.xp = xp;
  }

  /** Acrescenta mais 1 ao nível.*/
  public void setNivel() {
    if (nivel < 10) {
      nivel++;
      int hpMaxAnterior = hpMax;
      atualizarAtributos();
      hpAtual += hpMax - hpMaxAnterior;
    } else {
      System.out.println("Nível máximo excedido");
    }
  }

  /** Recalcula os atributos de acordo com o nível atual.*/
  private void atualizarAtributos() {
    hpMax = 45 + (nivel * 4);
    atk = 52 + (nivel * 3);
    def = 42 + (nivel * 5);
    spd = 38 + (nivel * 2);
  }

  /** Guarda os ataques do TotoSal e devolve apenas os que o nível atual permite usar.*/
  public List<Ataque> ataques() {
    List<Ataque> disponiveis = new ArrayList<>();
    for (Ataque ataque : ATAQUES) {
      if (ataque.podeUsar(nivel)) {
        disponiveis.add(ataque);
      }
    }
    return disponiveis;
  }
}
