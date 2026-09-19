package model.elementos;

import model.pokesal.PokeSal;

/** Classe responsável pelo efeitos de terrenos.*/
public class Terrenos {
  static boolean quente = false;
  static boolean molhado = false;
  static boolean florido = false;

  /**Método para validar se um evento está ocorrendo ou não.*/
  public static void validarTerrenos(int evento) {
    if (evento == 0) {
      System.out.println("O sol castica aqueles que permanecem desprotegidos!\nO dia está quente!");
      quente = true;
    }
    if (evento == 1) {
      System.out.println("A chuva deixou sua marca!\nO chão está molhado!");
      molhado = true;
    }
    if (evento == 2) {
      System.out.println("A vida desabroxa, com ou sem o homem!\n As plantas tomam o terreno!");
      florido = true;
    }
  }

  /** Desativa todos os terrenos, para que apenas o evento mais recente tenha efeito.*/
  public static void limparTerrenos() {
    quente = false;
    molhado = false;
    florido = false;
  }

  /** Multiplicador de dano do golpe: Quente +15% para fogo, Molhado +10% para água.*/
  public static double multiplicadorDano(TipoElemental tipoGolpe) {
    double multiplicador = 1.0;
    if (quente && tipoGolpe == TipoElemental.FOGO) {
      multiplicador *= 1.50;
    }
    if (molhado && tipoGolpe == TipoElemental.AGUA) {
      multiplicador *= 2.0;
    }
    return multiplicador;
  }

  /** Florido: pokeSal do tipo planta recupera 5% do hp máximo ao final do turno.*/
  public static void curaFimDeTurno(PokeSal pokeSal) {
    if (!florido || pokeSal.getElemento() != TipoElemental.GRAMA || pokeSal.getHpAtual() <= 0) {
      return;
    }
    int cura = Math.max(1, pokeSal.getHpMax() * 5 / 100);
    int novoHp = Math.min(pokeSal.getHpMax(), pokeSal.getHpAtual() + cura);
    if (novoHp > pokeSal.getHpAtual()) {
      pokeSal.setHpAtual(novoHp);
      System.out.println(pokeSal.getClass().getSimpleName() + " recuperou " + cura
          + " de HP graças ao terreno florido!");
    }
  }
}