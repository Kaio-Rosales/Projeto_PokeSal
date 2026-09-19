package model.itens;

/** Interface comum a todo item que pode ser guardado na mochila do jogador. */
public interface Item {

  /**Metodo para retorna o nome do item.*/
  String getNome();

  /**Metodo para retorna a quantidade daquele item.*/
  int getQuantidade();

  /**Metodo para midificar a quantidade de itens.*/
  void setQuantidade(int quantidade);
}
