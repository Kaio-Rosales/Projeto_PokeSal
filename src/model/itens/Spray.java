package model.itens;

import model.pokesal.PokeSal;

/** Determina a função e quantidade do item.*/
public class Spray extends ItemStatus {

  /**Construtor da classe super.*/
  public Spray() {
    super("Spray");
  }

  @Override
  protected void removerEfeito(PokeSal alvo) {
  }

}
