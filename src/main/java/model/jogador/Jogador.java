package model.jogador;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
}