package model.pokesal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.elementos.TipoElemental;

/** Que determina os atributos do ChikoSal. */
public class ChikoSal implements PokeSal {

  private int nivel = 1;
  private int hpMax = 36 + (nivel * 4);
  private int hpAtual = 36 + (nivel * 4);
  private int atk = 40 + (nivel * 3);
  private int def = 50 + (nivel * 6);
  private int spd = 32 + (nivel * 4);
  private int xp = 0;
  private final TipoElemental elemento = TipoElemental.GRAMA;
  private static final List<Ataque> ATAQUES = Arrays.asList(
      new Ataque("Lâmina de Navalha", TipoElemental.GRAMA, 10, 3, 1),
      new Ataque("Folha Mágica", TipoElemental.GRAMA, 15, 3, 5),
      new Ataque("Raio Solar", TipoElemental.GRAMA, 10, 2, 8));

  /** Construtor vazio de ChikoSal. */
  public ChikoSal() {
  }

  /** Construtor com atributo nível.*/
  public ChikoSal(int nivel) {
    this.nivel = nivel;
    atualizarAtributos();
    this.hpAtual = hpMax;
  }

  @Override
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
    hpMax = 36 + (nivel * 4);
    atk = 40 + (nivel * 3);
    def = 50 + (nivel * 6);
    spd = 32 + (nivel * 4);
  }

  /** Guarda os ataques do ChikoSal e devolve apenas os que o nível atual permite usar.*/
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
