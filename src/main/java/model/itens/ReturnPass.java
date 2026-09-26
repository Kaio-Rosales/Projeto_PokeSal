package model.itens;

import servicos.Servicos;

/**Determina a função e quantidade do item.*/
public class ReturnPass implements Item {

  private final String nome = "Passe de Retorno";
  private int quantidade;

  @Override
  public String getNome() {
    return nome;
  }

  @Override
  public int getQuantidade() {
    return quantidade;
  }

  @Override
  public void setQuantidade(int quantidade) {
    this.quantidade = quantidade;
  }

  /**Retorna o jogador ao ponto de partida, limpa o mapa e consumindo uma unidade do pass.*/
  public void usar(Servicos servicos) {
    if (quantidade <= 0) {
      System.out.println("Você não possui mais " + nome);
      return;
    }
    servicos.limparMapa();
    quantidade--;
    System.out.println(nome + " usado.");
  }
}
