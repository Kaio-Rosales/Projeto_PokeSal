package model.itens;

import model.elementos.StatusEfeito;
import model.pokesal.PokeSal;

/** Determina a função e quantidade do item.*/
public class Cloak extends ItemStatus {

  /**Construtor da classe super.*/
  public Cloak() {
    super("Manto");
  }

  @Override
  protected void removerEfeito(PokeSal alvo) {
    if (alvo.getStatus() == StatusEfeito.RESFRIADO) {
      alvo.setStatus(StatusEfeito.NENHUM);
      System.out.println(alvo.getClass().getSimpleName() + " não está mais resfriado!");
    } else {
      System.out.println(alvo.getClass().getSimpleName() + " não estava resfriado.");
    }
  }
}
