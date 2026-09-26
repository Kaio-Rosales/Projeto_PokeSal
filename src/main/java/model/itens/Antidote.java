package model.itens;

import model.elementos.StatusEfeito;
import model.pokesal.PokeSal;

/** Determina a função e quantidade do item.*/
public class Antidote extends ItemStatus {

  /**Construtor da classe super.*/
  public Antidote() {
    super("Antidoto");
  }

  @Override
  protected void removerEfeito(PokeSal alvo) {
    if (alvo.getStatus() == StatusEfeito.ENVENENADO) {
      alvo.setStatus(StatusEfeito.NENHUM);
      System.out.println(alvo.getClass().getSimpleName() + " não está mais envenenado!");
    } else {
      System.out.println(alvo.getClass().getSimpleName() + " não estava envenenado.");
    }
  }
}
