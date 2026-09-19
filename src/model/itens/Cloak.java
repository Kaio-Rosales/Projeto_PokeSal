package model.itens;

import model.pokesal.PokeSal;

/** Determina a função e quantidade do item.*/
public class Cloak extends ItemStatus {

  /**Construtor da classe super.*/
  public Cloak() {
    super("Manto");
  }

  @Override
  protected void removerEfeito(PokeSal alvo) {
  }
}
