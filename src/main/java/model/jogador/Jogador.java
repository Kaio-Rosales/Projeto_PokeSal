package model.jogador;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import model.elementos.StatusEfeito;
import model.itens.Item;
import model.pokesal.PokeSal;

/** Classe base para o jogador do jogo.*/
public class Jogador {

  private String nome = "Dummy";
  private List<PokeSal> time = new ArrayList<>(7);
  private List<Item> mochila = new LinkedList<>();

  /** Cronstrutor do jogador.*/
  public Jogador(String nome, List<PokeSal> time, List<Item> mochila) {
    this.nome = nome;
    this.time = time;
    this.mochila = mochila;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public List<PokeSal> getTime() {
    return time;
  }

  public void setTime(List<PokeSal> time) {
    this.time = time;
  }

  public List<Item> getMochila() {
    return mochila;
  }

  public void setMochila(List<Item> mochila) {
    this.mochila = mochila;
  }

  /**
   * FASE 03: primeiro PokeSal do time que ainda pode lutar, ou {@code null} se todos desmaiaram.
   * Antes essa busca existia só dentro de {@code Servicos}; agora a batalha entre treinadores
   * também precisa dela, então ela vive na entidade que possui o time.*/
  public PokeSal getPrimeiroPokeSalVivo() {
    for (PokeSal candidato : time) {
      if (candidato != null && candidato.estaVivo()) {
        return candidato;
      }
    }
    return null;
  }

  /** FASE 03: indica se ainda existe algum PokeSal apto a lutar no time.*/
  public boolean possuiPokeSalVivo() {
    return getPrimeiroPokeSalVivo() != null;
  }

  /** FASE 03: devolve HP máximo e remove status de todo o time (usado entre rodadas do torneio).*/
  public void curarTime() {
    for (PokeSal pokeSal : time) {
      if (pokeSal != null) {
        pokeSal.setHpAtual(pokeSal.getHpMax());
        pokeSal.setStatus(StatusEfeito.NENHUM);
      }
    }
  }

  /**
   * FASE 03: guarda o item na mochila, somando a quantidade se já existir um item com o mesmo
   * nome. CORRIGE o BUG-01 (ver docs): a versão antiga comparava com {@code equals()}, que nunca
   * era verdadeiro entre objetos diferentes, e por isso itens iguais nunca se empilhavam.*/
  public void adicionarItem(Item novo) {
    for (Item existente : mochila) {
      if (existente.getNome().equals(novo.getNome())) {
        existente.setQuantidade(existente.getQuantidade() + novo.getQuantidade());
        return;
      }
    }
    mochila.add(novo);
  }
}
