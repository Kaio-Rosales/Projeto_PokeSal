package model.pokesal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.elementos.TipoElemental;

/** Que determina os atributos do CyndaSal.*/
public class CyndaSal implements PokeSal {

  private int nivel = 1;
  private int hpMax = 32 + (nivel * 2);
  private int hpAtual = 32 + (nivel * 2);
  private int atk = 41 + (nivel * 6);
  private int def = 40 + (nivel * 3);
  private int spd = 45 + (nivel * 5);
  private int xp = 0;
  private final TipoElemental elemento = TipoElemental.FOGO;
  private static final List<Ataque> ATAQUES = Arrays.asList(
      new Ataque("Arranhão", TipoElemental.NORMAL, 10, 3, 1),
      new Ataque("Brasa", TipoElemental.FOGO, 10, 2, 3),
      new Ataque("Lança-Chamas", TipoElemental.FOGO, 0, 1, 8));

  /** Construtor vazio de CyndaSal.*/
  public CyndaSal() {
  }

  /** Construtor com atributo nível.*/
  public CyndaSal(int nivel) {
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

  public int getXp() {
    return xp;
  }

  public void setXp(int xp) {
    this.xp = xp;
  }

  /** Recalcula os atributos de acordo com o nível atual.*/
  private void atualizarAtributos() {
    hpMax = 32 + (nivel * 2);
    atk = 41 + (nivel * 6);
    def = 40 + (nivel * 3);
    spd = 45 + (nivel * 5);
  }

  /** Guarda os ataques do CyndaSal e devolve apenas os que o nível atual permite usar.*/
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
