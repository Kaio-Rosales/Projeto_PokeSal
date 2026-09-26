package model.pokesal;

import java.util.List;
import model.elementos.StatusEfeito;
import model.elementos.TipoElemental;

/**Interface modelo dos pokeSals.*/
public interface PokeSal {
  /**Metodo para retorna o hp maximo.*/
  int getHpMax();

  /**Metodo para retorna o hp atual.*/
  int getHpAtual();

  /**Metodo para modificar o hp atual.*/
  void setHpAtual(int hpAtual);

  /**Metodo para retorna o ataque do pokeSal.*/
  int getAtk();

  /**Metodo para modificar o ataque do pokeSal.*/
  void setAtk(int atk);

  /**Metodo para retorna a defesa do pokeSal.*/
  int getDef();

  /**Metodo para retorna a velocidade do pokeSal.*/
  int getSpd();

  /**Metodo para retorna o nível do pokeSal.*/
  int getNivel();

  /**Metodo para subir o pokeSal em um nível e recalcular seus atributos.*/
  void setNivel();

  /**Metodo para retorna o tipo elemental do pokeSal.*/
  TipoElemental getElemento();

  /**Metodo para retorna o xp acumulado no nível atual.*/
  int getXp();

  /**Metodo para modificar o xp acumulado.*/
  void setXp(int xp);

  /**Metodo que retorna os ataques que o pokeSal pode usar no nível atual.*/
  List<Ataque> ataques();

  /**Metodo para retorna o efeito de status atual do pokeSal (NENHUM se saudável).*/
  StatusEfeito getStatus();

  /**Metodo para modificar o efeito de status do pokeSal.*/
  void setStatus(StatusEfeito status);

  /**Verifica se o pokeSal ainda está apto a batalhar.*/
  default boolean estaVivo() {
    return getHpAtual() > 0;
  }

  /**Ataque efetivo, considerando a redução de 5% causada pelo status Queimado.*/
  default int getAtkEfetivo() {
    if (getStatus() == StatusEfeito.QUEIMADO) {
      return (int) Math.round(getAtk() * 0.95);
    }
    return getAtk();
  }

  /**Velocidade efetiva, considerando a redução de 30% causada pelo status Paralisado.*/
  default int getSpdEfetivo() {
    if (getStatus() == StatusEfeito.PARALISADO) {
      return (int) Math.round(getSpd() * 0.70);
    }
    return getSpd();
  }

  /**Xp necessário para subir do nível atual para o próximo (5 * nível).*/
  default int xpParaProximoNivel() {
    return 5 * getNivel();
  }

  /**Soma o xp recebido e sobe de nível enquanto houver xp suficiente. Retorna os níveis ganhos.*/
  default int ganharXp(int quantidade) {
    int niveisGanhos = 0;
    int xp = getXp() + (quantidade * 2);
    while (getNivel() < 10 && xp >= xpParaProximoNivel()) {
      xp -= xpParaProximoNivel();
      setNivel();
      niveisGanhos++;
    }
    setXp(xp);
    return niveisGanhos;
  }
}
