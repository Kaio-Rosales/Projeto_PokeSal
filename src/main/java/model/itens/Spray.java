package model.itens;

import model.elementos.StatusEfeito;
import model.pokesal.PokeSal;

/** Determina a função e quantidade do item.*/
public class Spray extends ItemStatus {

  /**Construtor da classe super.*/
  public Spray() {
    super("Spray");
  }

  @Override
  protected void removerEfeito(PokeSal alvo) {
    if (alvo.getStatus() == StatusEfeito.QUEIMADO) {
      alvo.setStatus(StatusEfeito.NENHUM);
      System.out.println(alvo.getClass().getSimpleName() + " não está mais queimado!");
    } else {
      System.out.println(alvo.getClass().getSimpleName() + " não estava queimado.");
    }
  }

}
