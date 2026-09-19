package servicos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import model.elementos.Terrenos;
import model.itens.Antidote;
import model.itens.Cloak;
import model.itens.Item;
import model.itens.ItemCurativo;
import model.itens.MegaPotion;
import model.itens.Potion;
import model.itens.ReturnPass;
import model.itens.Spray;
import model.itens.SuperPotion;
import model.jogador.Jogador;
import model.pokesal.Ataque;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import model.pokesal.PokeSal;
import model.pokesal.TotoSal;

/** Quarda todos os serviços do sistema.*/
public class Servicos {
  private Jogador jogador;
  private int[][] localizacao = {{0, 0, 0}, {0, 0, 0}, {0}};
  private PokeSal oponente;
  private int escolha = -1;
  private int caminho = 0;

  /** Método que adiciona e muda o nome do jogador.*/
  public void verificarNome() {
    String escolha;
    Scanner scan = new Scanner(System.in);
    System.out.println("Nome atual: " + jogador.getNome());
    System.out.println("Deseja continuar? [S/N]");
    escolha = scan.next();
    if (escolha.equalsIgnoreCase("n")) {
      System.out.println("Cancelando troca...");
      scan.close();
      return;
    } else {
      System.out.print("Novo nome: ");
      jogador.setNome(scan.next());
      scan.close();
    }
  }

  /** Método responsável pela batalha de turnos.*/
  public void batalha(PokeSal pokeSal) {
    PokeSal ativo = proximoPokeSalVivo();
    if (ativo == null) {
      System.out.println("Você não tem nenhum PokeSal em condições de batalhar!");
      return;
    }
    Scanner scan = new Scanner(System.in);
    Random random = new Random();
    int itensUsados = 0;

    System.out.println("Um " + nomePokesal(oponente) + " de nível " + oponente.getNivel()
        + " apareceu!\nVai, " + nomePokesal(ativo) + "!");

    while (true) {
      mostrarStatus(ativo, oponente);
      System.out.println("O que " + nomePokesal(ativo) + " vai fazer?\n1-Atacar\n2-Usar item"
          + "\n3-Fugir");
      int acao = lerOpcao(scan, 1, 3);

      if (acao == 3) {
        System.out.println("Você fugiu da batalha!");
        return;
      }

      Ataque golpe = null;
      ItemCurativo item = null;
      if (acao == 1) {
        golpe = escolherAtaque(scan, ativo);
        if (golpe == null) {
          continue;
        }
      } else {
        item = escolherItem(scan, ativo, itensUsados);
        if (item == null) {
          continue;
        }
      }

      boolean itemUsado = false;
      if (ativo.getSpd() >= oponente.getSpd()) {
        itemUsado = agirTreinador(ativo, oponente, golpe, item);
        if (oponente.getHpAtual() > 0) {
          atacar(oponente, ativo, sortearAtaque(oponente, random));
        }
      } else {
        atacar(oponente, ativo, sortearAtaque(oponente, random));
        if (ativo.getHpAtual() > 0) {
          itemUsado = agirTreinador(ativo, oponente, golpe, item);
        }
      }
      if (itemUsado) {
        itensUsados++;
      }

      Terrenos.curaFimDeTurno(ativo);
      Terrenos.curaFimDeTurno(oponente);

      if (oponente.getHpAtual() <= 0) {
        System.out.println(nomePokesal(oponente) + " foi derrotado!");
        concederXp(ativo, oponente);
        return;
      }
      if (ativo.getHpAtual() <= 0) {
        System.out.println(nomePokesal(ativo) + " desmaiou!");
        ativo = proximoPokeSalVivo();
        if (ativo == null) {
          System.out.println("Todos os seus PokeSals foram derrotados. Você perdeu a batalha!");
          return;
        }
        System.out.println("Vai, " + nomePokesal(ativo) + "!");
      }
    }
  }

  /** Executa a ação do treinador no turno: usar o item ou atacar. Retorna true se usou item.*/
  private boolean agirTreinador(PokeSal ativo, PokeSal oponente, Ataque golpe,
      ItemCurativo item) {
    if (item != null) {
      item.usar(ativo);
      return true;
    }
    atacar(ativo, oponente, golpe);
    return false;
  }

  /** Aplica o golpe considerando vantagem elemental e efeito de terreno.*/
  private void atacar(PokeSal atacante, PokeSal defensor, Ataque golpe) {
    double efetividade = golpe.getTipo().danoElemental(golpe.getTipo(), defensor.getElemento());
    double terreno = Terrenos.multiplicadorDano(golpe.getTipo());
    int dano = (int) Math.round(golpe.calcularDano(atacante.getAtk()) * efetividade * terreno);
    defensor.setHpAtual(Math.max(0, defensor.getHpAtual() - dano));

    System.out.println(nomePokesal(atacante) + " usou " + golpe.getNome() + " e causou " + dano
        + " de dano!");
    if (efetividade > 1) {
      System.out.println("É super efetivo!");
    } else if (efetividade < 1) {
      System.out.println("Não é muito efetivo...");
    }
  }

  /** Sorteia um dos ataques que o pokeSal pode usar no seu nível.*/
  private Ataque sortearAtaque(PokeSal pokeSal, Random random) {
    List<Ataque> disponiveis = pokeSal.ataques();
    return disponiveis.get(random.nextInt(disponiveis.size()));
  }

  /** Mostra o menu de ataques disponíveis. Retorna o ataque escolhido ou null se voltar.*/
  private Ataque escolherAtaque(Scanner scan, PokeSal ativo) {
    List<Ataque> disponiveis = ativo.ataques();
    System.out.println("Escolha o ataque:");
    for (int i = 0; i < disponiveis.size(); i++) {
      Ataque ataque = disponiveis.get(i);
      System.out.println((i + 1) + "-" + ataque.getNome() + " (" + ataque.getTipo() + ", dano "
          + ataque.calcularDano(ativo.getAtk()) + ")");
    }
    System.out.println("0-Voltar");
    int opcao = lerOpcao(scan, 0, disponiveis.size());
    return opcao == 0 ? null : disponiveis.get(opcao - 1);
  }

  /** Mostra o menu de itens de cura da mochila. Retorna o item escolhido ou null.*/
  private ItemCurativo escolherItem(Scanner scan, PokeSal ativo, int itensUsados) {
    if (itensUsados >= 2) {
      System.out.println("Você já usou o máximo de " + 2
          + " itens nesta batalha!");
      return null;
    }
    if (ativo.getHpAtual() >= ativo.getHpMax()) {
      System.out.println(nomePokesal(ativo) + " já está com o HP cheio!");
      return null;
    }

    List<ItemCurativo> curativos = new ArrayList<>();
    for (Item item : jogador.getMochila()) {
      if (item instanceof ItemCurativo && item.getQuantidade() > 0) {
        curativos.add((ItemCurativo) item);
      }
    }
    if (curativos.isEmpty()) {
      System.out.println("Você não tem itens de cura na mochila.");
      return null;
    }

    System.out.println("Escolha o item (" + (2 - itensUsados)
        + " uso(s) restante(s) nesta batalha):");
    for (int i = 0; i < curativos.size(); i++) {
      System.out.println((i + 1) + "-" + curativos.get(i).getNome() + " x"
          + curativos.get(i).getQuantidade());
    }
    System.out.println("0-Voltar");
    int opcao = lerOpcao(scan, 0, curativos.size());
    return opcao == 0 ? null : curativos.get(opcao - 1);
  }

  /** Lê uma opção numérica entre min e max, repetindo até receber uma entrada válida.*/
  private int lerOpcao(Scanner scan, int min, int max) {
    while (true) {
      System.out.print("> ");
      String linha = scan.nextLine().trim();
      try {
        int opcao = Integer.parseInt(linha);
        if (opcao >= min && opcao <= max) {
          return opcao;
        }
      } catch (NumberFormatException e) {
        // entrada não numérica: cai na mensagem de opção inválida abaixo
      }
      System.out.println("Opção inválida. Digite um número entre " + min + " e " + max + ".");
    }
  }

  /** Primeiro pokeSal do time do jogador que ainda pode lutar, ou null se não houver.*/
  private PokeSal proximoPokeSalVivo() {
    for (PokeSal pokeSal : jogador.getTime()) {
      if (pokeSal != null && pokeSal.getHpAtual() > 0) {
        return pokeSal;
      }
    }
    return null;
  }

  /** Concede ao vencedor 2 * nível do oponente de xp e sobe de nível se possível.*/
  private void concederXp(PokeSal vencedor, PokeSal derrotado) {
    int xp = 2 * derrotado.getNivel();
    System.out.println(nomePokesal(vencedor) + " ganhou " + xp + " de XP!");
    if (vencedor.ganharXp(xp) > 0) {
      System.out.println(nomePokesal(vencedor) + " subiu para o nível " + vencedor.getNivel());
    }
    System.out.println("XP: " + vencedor.getXp() + "/" + vencedor.xpParaProximoNivel());
  }

  /** Imprime nível e HP dos dois pokeSals em campo.*/
  private void mostrarStatus(PokeSal ativo, PokeSal oponente) {
    System.out.println("\n" + nomePokesal(oponente) + " nv " + oponente.getNivel() + " - HP "
        + oponente.getHpAtual() + "/" + oponente.getHpMax());
    System.out.println(nomePokesal(ativo) + " nv " + ativo.getNivel() + " - HP "
        + ativo.getHpAtual() + "/" + ativo.getHpMax());
  }

  /** Nome de exibição do pokeSal (nome da classe).*/
  private String nomePokesal(PokeSal pokeSal) {
    return pokeSal.getClass().getSimpleName();
  }

  /** Método que determina os eventos aleatórios.*/
  public void eventosAleatorios() {
    Random random = new Random();
    int terreno = random.nextInt(4);
    Terrenos.limparTerrenos();
    Terrenos.validarTerrenos(terreno);
    int evento = random.nextInt(2);

    if (evento == 0) {
      boolean existe = false;
      Item itemEncontrado = sortearItem(random.nextInt(7));
      List<Item> mochila = jogador.getMochila();

      for (Item item : mochila) {
        if (item.equals(itemEncontrado)) {
          item.setQuantidade(item.getQuantidade() + 1);
          existe = true;
          break;
        }
      }
      if (existe == false) {
        itemEncontrado.setQuantidade(1);
        mochila.add(itemEncontrado);
      }
      System.out.println("Você encontrou: " + itemEncontrado.getNome());
    } else if (evento == 1) {
      int pokesal = random.nextInt(3);
      int nivel = random.nextInt(10);
      if (pokesal == 0) {
        oponente = new ChikoSal(nivel + 1);
      } else if (pokesal == 1) {
        oponente = new TotoSal(nivel + 1);
      } else {
        oponente = new CyndaSal(nivel + 1);
      }
      batalha(oponente);
    }
  }

  /** Instancia um item de acordo com o índice sorteado.*/
  private Item sortearItem(int tipo) {
    switch (tipo) {
      case 0:
        return new Potion();
      case 1:
        return new SuperPotion();
      case 2:
        return new MegaPotion();
      case 3:
        return new Antidote();
      case 4:
        return new Spray();
      case 5:
        return new Cloak();
      default:
        return new ReturnPass();
    }
  }

  /** Metodo para o personagem avançar pelo mapa.*/
  public void avancarMovimento() {
    Scanner scan = new Scanner(System.in);

    for (int i = 0; i < localizacao.length; i++) {
      for (int o = 0; o < localizacao[i].length; o++) {
        if (localizacao[i][o] == 1) {
          escolha = i;
          caminho = o;
        }
      }
    }

    if (escolha == -1) {
      System.out.println("Para onde quer ir?\n1-Entrada da Ucsal\n2-Estacionamento da Ucsal"
          + "\n3-Vila Universitária");
      escolha = scan.nextInt() - 1;
      if (escolha == 2) {
        curaTotal();
        localizacao[2][0] = 1;
        imprimirMapa();
        limparMapa();
        scan.close();
        return;
      }

      if (escolha >= 0 && escolha < localizacao.length) {
        localizacao[escolha][caminho] = 1;
        imprimirMapa();
        eventosAleatorios();
      } else {
        System.out.println("Escolha inválida");
      }
    } else {
      caminho++;
      if (caminho < localizacao[escolha].length) {
        localizacao[escolha][caminho] = 1;
        System.out.println("Avançou um passo");
        imprimirMapa();
        eventosAleatorios();
      } else {
        System.out.println("Você chegou ao fim do caminho");
        imprimirMapa();
      }
    }
    scan.close();
  }

  /** Metodo para o personagem retornar pelo mapa.*/
  public void retornarMovimento() {
    Scanner scan = new Scanner(System.in);

    for (int i = 0; i < localizacao.length; i++) {
      for (int o = 0; o < localizacao[i].length; o++) {
        if (localizacao[i][o] == 1) {
          escolha = i;
          caminho = o;
        }
      }
    }

    if (escolha == -1) {
      System.out.println("Operação inválida. Nenhuma ação de avanço foi realizada anteriormente");
    } else {
      localizacao[escolha][caminho] = 0;
      if (localizacao[escolha][0] == 0) {
        System.out.println("Voçê retornou ao ponto de partida");
        imprimirMapa();
        escolha = -1;
      } else {
        System.out.println("Você recuou uma posição");
        imprimirMapa();
        eventosAleatorios();
      }
    }
    scan.close();
  }

  /**Método para tornar todas a posições do mapa = 0.*/
  public void limparMapa() {
    for (int i = 0; i < localizacao.length; i++) {
      for (int o = 0; o < localizacao[i].length; o++) {
        if (localizacao[i][o] == 1) {
          localizacao[i][o] = 0;
        }
      }
    }
    escolha = -1;
    System.out.println("Você voltou ao início");
  }

  /**Método que imprime o mapa.*/
  public void imprimirMapa() {
    System.out.println("Mapa Atual:");
    for (int i = 0; i < localizacao.length; i++) {
      if (i == 0) {
        System.out.println("Entrada da Ucsal");
      }
      if (i == 1) {
        System.out.println("Estacionamento da Ucsal");
      }
      if (i == 2) {
        System.out.println("Vila Universitária");
      }
      for (int o = 0; o < localizacao[i].length; o++) {
        System.out.print(localizacao[i][o] + " ");
      }
      System.out.println();
    }
    System.out.println("-----------");
  }

  /**Método que cura todos os pokesals do time.*/
  public void curaTotal() {
    List<PokeSal> time = jogador.getTime();
    if (time.isEmpty()) {
      System.out.println("O time está vazio. Não há nada para curar.");
      return;
    }

    int curados = 0;
    for (PokeSal pokeSal : time) {
      if (pokeSal != null) {
        pokeSal.setHpAtual(pokeSal.getHpMax());
        curados++;
      }
      System.out.println(curados + " PokeSal(s) curado(s) com sucesso!");
    }
  }
}