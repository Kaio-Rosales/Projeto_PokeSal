package model.itens;

import model.pokesal.PokeSal;

/**Classe abstrata para que guarda os metodos e atributos dos itens de cura.*/
public abstract class ItemCurativo implements Item {
  private final String nome;
  private int quantidade;
  private final int valorCura;

  /** Construtor do item de cura.*/
  protected ItemCurativo(String nome, int valorCura) {
    this.nome = nome;
    this.valorCura = valorCura;
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

  /** Cura o pokesal alvo, sem passar do hpMax e consome uma unidade do item.*/
  public void usar(PokeSal alvo) {
    if (quantidade <= 0) {
      System.out.println("Você não possui mais " + nome);
      return;
    }
    int novoHp = alvo.getHpAtual() + valorCura;
    if (novoHp > alvo.getHpMax()) {
      novoHp = alvo.getHpMax();
    }
    alvo.setHpAtual(novoHp);
    quantidade--;
    System.out.println(nome + " usada. HP atual: " + novoHp);
  }
}
