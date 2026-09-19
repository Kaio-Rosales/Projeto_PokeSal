package model.itens;

import model.pokesal.PokeSal;

/** Determina a função e quantidade do item.*/
public class Antidote extends ItemStatus {

  /**Construtor da classe super.*/
  public Antidote() {
    super("Antidoto");
  }

  @Override
  protected void removerEfeito(PokeSal alvo) {
  }
}
