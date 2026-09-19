package model.itens;

import model.pokesal.PokeSal;

/**Classe abstrata para que guarda os metodos e atributos dos itens quem curam status.*/
public abstract class ItemStatus implements Item {
  private final String nome;
  private int quantidade;

  /** Construtor do item de status.*/
  protected ItemStatus(String nome) {
    this.nome = nome;
  }

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

  /** Remove o efeito de status correspondente do pokesal alvo, se houver unidades do item.*/
  public void usar(PokeSal alvo) {
    if (quantidade <= 0) {
      System.out.println("Você não possui mais " + nome);
      return;
    }
    removerEfeito(alvo);
    quantidade--;
    System.out.println(nome + " usado.");
  }

  /** Aplica a remoção do status específico. Implementado por cada subclasse(Não continuado).*/
  protected abstract void removerEfeito(PokeSal alvo);
}
