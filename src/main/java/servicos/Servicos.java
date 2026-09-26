package servicos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import model.elementos.StatusEfeito;
import model.elementos.Terrenos;
import model.elementos.TipoElemental;
import model.itens.Antidote;
import model.itens.Cloak;
import model.itens.Item;
import model.itens.ItemCurativo;
import model.itens.ItemStatus;
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
  private final Scanner scanner;
  private Random random = new Random();
  private static final int LIMITE_ITENS_POR_BATALHA = 2;

  /** Construtor padrão, cria seu próprio Scanner de leitura do console.*/
  public Servicos() {
    this.scanner = new Scanner(System.in);
  }

  /**
   * Construtor que recebe o jogador já instanciado e um Scanner compartilhado com a Main,
   * evitando fechar o System.in mais de uma vez durante a execução.*/
  public Servicos(Jogador jogador, Scanner scanner) {
    this.jogador = jogador;
    this.scanner = scanner;
  }

  public Jogador getJogador() {
    return jogador;
  }

  public void setJogador(Jogador jogador) {
    this.jogador = jogador;
  }

  /**
   * Permite injetar um gerador de números aleatórios determinístico (ex.: em testes) no lugar do
   * Random padrão. Pacote-privado: não faz parte da API pública do jogo.*/
  void setRandom(Random random) {
    this.random = random;
  }

  /**
   * Expõe a matriz de localização para inspeção (ex.: em testes). Pacote-privado: não faz parte
   * da API pública do jogo.*/
  int[][] getLocalizacao() {
    return localizacao;
  }

  /**
   * Expõe o oponente atual para inspeção (ex.: em testes). Pacote-privado: não faz parte da
   * API pública do jogo.*/
  PokeSal getOponente() {
    return oponente;
  }

  /** Fecha o Scanner utilizado pelos serviços. Deve ser chamado apenas ao encerrar o sistema.*/
  public void encerrar() {
    scanner.close();
  }

  /** Método que adiciona e muda o nome do jogador.*/
  public void verificarNome() {
    String escolhaNome;
    System.out.println("Nome atual: " + jogador.getNome());
    System.out.println("Deseja trocar o nome? [S/N]");
    escolhaNome = scanner.next();
    scanner.nextLine();
    if (escolhaNome.equalsIgnoreCase("n")) {
      System.out.println("Cancelando troca...");
      return;
    } else {
      System.out.print("Novo nome: ");
      jogador.setNome(scanner.nextLine());
    }
  }

  /** Método responsável pela batalha de turnos.*/
  public void batalha(PokeSal pokeSal) {
    oponente = pokeSal;
    PokeSal meuPokeSal = escolherPokeSalParaBatalha();

    if (meuPokeSal == null) {
      System.out.println("Você não possui nenhum PokeSal em condições de batalhar!");
      return;
    }

    System.out.println("\nUm " + oponente.getClass().getSimpleName()
        + " selvagem de nível " + oponente.getNivel() + " apareceu!");

    int itensUsados = 0;
    boolean fugiu = false;

    while (meuPokeSal.estaVivo() && oponente.estaVivo() && !fugiu) {
      exibirStatusBatalha(meuPokeSal, oponente);
      System.out.println("1 - Atacar");
      System.out.println("2 - Usar Item (" + (LIMITE_ITENS_POR_BATALHA - itensUsados)
          + " restante(s))");
      System.out.println("3 - Fugir");
      System.out.print("Escolha: ");
      int opcao = lerInteiro();

      Ataque ataqueEscolhido = null;
      boolean usouItem = false;

      if (opcao == 3) {
        System.out.println(jogador.getNome() + " fugiu da batalha!");
        fugiu = true;
        continue;
      } else if (opcao == 2) {
        if (!verificarLimiteDeItens(itensUsados)) {
          System.out.println("Você já usou o número máximo de itens permitido nesta batalha");
          continue;
        }
        usouItem = usarItemBatalha(meuPokeSal);
        if (!usouItem) {
          continue;
        }
        itensUsados++;
      } else if (opcao == 1) {
        ataqueEscolhido = escolherAtaque(meuPokeSal);
      } else {
        System.out.println("Opção inválida.");
        continue;
      }

      if (usouItem) {
        // Usar item dá prioridade de turno ao treinador; o oponente ataca em seguida.
        if (oponente.estaVivo()) {
          executarAtaqueIa(oponente, meuPokeSal);
        }
      } else if (atacaPrimeiro(meuPokeSal, oponente)) {
        executarAtaque(meuPokeSal, oponente, ataqueEscolhido);
        if (oponente.estaVivo()) {
          executarAtaqueIa(oponente, meuPokeSal);
        }
      } else {
        executarAtaqueIa(oponente, meuPokeSal);
        if (meuPokeSal.estaVivo()) {
          executarAtaque(meuPokeSal, oponente, ataqueEscolhido);
        }
      }

      if (meuPokeSal.estaVivo()) {
        aplicarStatusFimDeTurno(meuPokeSal);
      }
      if (oponente.estaVivo()) {
        aplicarStatusFimDeTurno(oponente);
      }
      if (meuPokeSal.estaVivo()) {
        Terrenos.curaFimDeTurno(meuPokeSal);
      }
      if (oponente.estaVivo()) {
        Terrenos.curaFimDeTurno(oponente);
      }
    }

    if (!fugiu) {
      if (!meuPokeSal.estaVivo()) {
        System.out.println("\n" + meuPokeSal.getClass().getSimpleName()
            + " desmaiou! Você perdeu a batalha...");
      } else {
        System.out.println("\nVocê venceu a batalha!");
        int xpGanho = oponente.getNivel() * 2;
        int niveisGanhos = meuPokeSal.ganharXp(xpGanho);
        System.out.println(meuPokeSal.getClass().getSimpleName() + " ganhou " + xpGanho
            + " de XP!");
        if (niveisGanhos > 0) {
          System.out.println(meuPokeSal.getClass().getSimpleName() + " subiu para o nível "
              + meuPokeSal.getNivel() + "!");
        }
      }
    }

    meuPokeSal.setStatus(StatusEfeito.NENHUM);
    oponente.setStatus(StatusEfeito.NENHUM);
  }

  /**
   * Determina se o PokeSal do jogador age antes do oponente, com base na velocidade efetiva
   * (já considerando a penalidade de Paralisado). Em caso de empate, o jogador age primeiro.*/
  public boolean atacaPrimeiro(PokeSal meuPokeSal, PokeSal oponentePokeSal) {
    return meuPokeSal.getSpdEfetivo() >= oponentePokeSal.getSpdEfetivo();
  }

  /** Escolhe o primeiro PokeSal do time do jogador que ainda esteja apto a batalhar.*/
  private PokeSal escolherPokeSalParaBatalha() {
    if (jogador == null) {
      return null;
    }
    for (PokeSal candidato : jogador.getTime()) {
      if (candidato.estaVivo()) {
        return candidato;
      }
    }
    return null;
  }

  /** Imprime o HP e o status atual dos dois PokeSals em batalha.*/
  private void exibirStatusBatalha(PokeSal meuPokeSal, PokeSal oponentePokeSal) {
    String statusMeu = formatarStatus(meuPokeSal);
    String statusOponente = formatarStatus(oponentePokeSal);
    System.out.println("\n-------------------------------------------");
    System.out.println("Seu " + meuPokeSal.getClass().getSimpleName() + " (Nv. "
        + meuPokeSal.getNivel() + ") - HP: " + meuPokeSal.getHpAtual() + "/"
        + meuPokeSal.getHpMax() + statusMeu);
    System.out.println(oponentePokeSal.getClass().getSimpleName() + " selvagem (Nv. "
        + oponentePokeSal.getNivel() + ") - HP: " + oponentePokeSal.getHpAtual() + "/"
        + oponentePokeSal.getHpMax() + statusOponente);
    System.out.println("-------------------------------------------");
  }

  /** Retorna " [STATUS]" se o pokeSal tiver algum efeito ativo, ou string vazia caso contrário.*/
  private String formatarStatus(PokeSal pokeSal) {
    if (pokeSal.getStatus() == StatusEfeito.NENHUM) {
      return "";
    }
    return " [" + pokeSal.getStatus() + "]";
  }

  /** Pede ao jogador que escolha um dos ataques disponíveis do PokeSal informado.*/
  private Ataque escolherAtaque(PokeSal meuPokeSal) {
    List<Ataque> disponiveis = meuPokeSal.ataques();
    System.out.println("\nEscolha um ataque:");
    for (int i = 0; i < disponiveis.size(); i++) {
      System.out.println((i + 1) + " - " + disponiveis.get(i).getNome()
          + " (" + disponiveis.get(i).getTipo() + ")");
    }
    int opcao = -1;
    while (opcao < 1 || opcao > disponiveis.size()) {
      System.out.print("Opção: ");
      opcao = lerInteiro();
      if (opcao < 1 || opcao > disponiveis.size()) {
        System.out.println("Opção inválida.");
      }
    }
    return disponiveis.get(opcao - 1);
  }

  /**
   * Executa um ataque de um PokeSal contra outro, aplicando vantagem elemental e terreno.
   * Aplica o dano, verifica desmaio e sorteia a chance de aplicar o status do golpe.*/
  private void executarAtaque(PokeSal atacante, PokeSal alvo, Ataque ataque) {
    if (atacante.getStatus() == StatusEfeito.RESFRIADO) {
      System.out.println(atacante.getClass().getSimpleName()
          + " está resfriado e não conseguiu agir!");
      return;
    }

    TipoElemental tipoGolpe = ataque.getTipo();
    double efetividade = tipoGolpe.danoElemental(tipoGolpe, alvo.getElemento());
    int dano = calcularDano(atacante, alvo, ataque);

    int hpRestante = Math.max(0, alvo.getHpAtual() - dano);
    alvo.setHpAtual(hpRestante);

    System.out.println(atacante.getClass().getSimpleName() + " usou " + ataque.getNome() + "!");
    if (efetividade > 1.0) {
      System.out.println("É super efetivo!");
    } else if (efetividade < 1.0) {
      System.out.println("Não é muito efetivo...");
    }
    System.out.println(alvo.getClass().getSimpleName() + " sofreu " + dano + " de dano! (HP: "
        + hpRestante + "/" + alvo.getHpMax() + ")");

    if (hpRestante <= 0) {
      System.out.println(alvo.getClass().getSimpleName() + " desmaiou!");
      return;
    }

    if (ataque.getEfeito() != StatusEfeito.NENHUM && alvo.getStatus() == StatusEfeito.NENHUM) {
      int sorteio = random.nextInt(100);
      if (sorteio < ataque.getChanceEfeito()) {
        alvo.setStatus(ataque.getEfeito());
        System.out.println(alvo.getClass().getSimpleName() + " foi afetado por "
            + ataque.getEfeito() + "!");
      }
    }
  }

  /**
   * Calcula o dano final de um ataque.*/
  public int calcularDano(PokeSal atacante, PokeSal alvo, Ataque ataque) {
    TipoElemental tipoGolpe = ataque.getTipo();
    double efetividade = tipoGolpe.danoElemental(tipoGolpe, alvo.getElemento());
    double multiplicadorTerreno = Terrenos.multiplicadorDano(tipoGolpe);
    int danoBruto = ataque.calcularDano(atacante.getAtkEfetivo());
    int danoComBonus = (int) Math.round(danoBruto * efetividade * multiplicadorTerreno);
    return Math.max(1, danoComBonus - (alvo.getDef() / 10));
  }

  /** Escolhe um ataque aleatório entre os disponíveis e o executa. Usado pelo oponente.*/
  private void executarAtaqueIa(PokeSal atacante, PokeSal alvo) {
    List<Ataque> disponiveis = atacante.ataques();
    Ataque escolhido = disponiveis.get(random.nextInt(disponiveis.size()));
    executarAtaque(atacante, alvo, escolhido);
  }

  /**
   * Aplica dano ou cura de status ao final do turno (Queimado, Envenenado, Resfriado).
   * Paralisado apenas reduz a velocidade efetiva e não causa dano por turno.*/
  private void aplicarStatusFimDeTurno(PokeSal pokeSal) {
    switch (pokeSal.getStatus()) {
      case QUEIMADO:
        int danoQueimado = Math.max(1, pokeSal.getHpMax() * 2 / 100);
        pokeSal.setHpAtual(Math.max(0, pokeSal.getHpAtual() - danoQueimado));
        System.out.println(pokeSal.getClass().getSimpleName() + " sofreu " + danoQueimado
            + " de dano pela queimadura!");
        break;
      case ENVENENADO:
        int danoVeneno = Math.max(1, pokeSal.getHpMax() * 5 / 100);
        pokeSal.setHpAtual(Math.max(0, pokeSal.getHpAtual() - danoVeneno));
        System.out.println(pokeSal.getClass().getSimpleName() + " sofreu " + danoVeneno
            + " de dano pelo veneno!");
        break;
      case RESFRIADO:
        if (random.nextInt(100) < 50) {
          pokeSal.setStatus(StatusEfeito.NENHUM);
          System.out.println(pokeSal.getClass().getSimpleName() + " não está mais resfriado!");
        }
        break;
      case PARALISADO:
      case NENHUM:
      default:
        break;
    }
  }

  /**
   * Permite ao jogador usar um item (de cura ou de status) no PokeSal ativo durante a batalha.
   * Retorna true se um item foi de fato utilizado (consumindo o turno do treinador).*/
  private boolean usarItemBatalha(PokeSal alvo) {
    List<Item> disponiveis = itensDisponiveis(item -> true);
    if (disponiveis.isEmpty()) {
      System.out.println("Sua mochila está vazia!");
      return false;
    }

    System.out.println("\nItens disponíveis:");
    for (int i = 0; i < disponiveis.size(); i++) {
      System.out.println((i + 1) + " - " + disponiveis.get(i).getNome()
          + " (x" + disponiveis.get(i).getQuantidade() + ")");
    }
    System.out.println("0 - Cancelar");
    System.out.print("Opção: ");
    int opcao = lerInteiro();
    if (opcao == 0) {
      return false;
    }
    if (opcao < 1 || opcao > disponiveis.size()) {
      System.out.println("Opção inválida.");
      return false;
    }

    Item escolhido = disponiveis.get(opcao - 1);
    if (escolhido instanceof ReturnPass) {
      System.out.println("Você não pode usar o Passe de Retorno durante uma batalha!");
      return false;
    } else if (escolhido instanceof ItemCurativo) {
      ((ItemCurativo) escolhido).usar(alvo);
    } else if (escolhido instanceof ItemStatus) {
      ((ItemStatus) escolhido).usar(alvo);
    }
    return true;
  }

  /** Permite usar itens de cura ou o Passe de Retorno fora de batalha.*/
  public void usarItem() {
    List<Item> disponiveis = itensDisponiveis(
        item -> item instanceof ItemCurativo || item instanceof ReturnPass);
    if (disponiveis.isEmpty()) {
      System.out.println("Você não possui itens de cura ou Passe de Retorno.");
      return;
    }

    System.out.println("\nItens disponíveis:");
    for (int i = 0; i < disponiveis.size(); i++) {
      System.out.println((i + 1) + " - " + disponiveis.get(i).getNome()
          + " (x" + disponiveis.get(i).getQuantidade() + ")");
    }
    System.out.println("0 - Cancelar");
    System.out.print("Opção: ");
    int opcao = lerInteiro();
    if (opcao == 0 || opcao < 1 || opcao > disponiveis.size()) {
      System.out.println("Operação cancelada.");
      return;
    }

    Item escolhido = disponiveis.get(opcao - 1);
    if (escolhido instanceof ReturnPass) {
      ((ReturnPass) escolhido).usar(this);
    } else if (escolhido instanceof ItemCurativo) {
      PokeSal alvo = escolherPokeSalDoTime();
      if (alvo != null) {
        ((ItemCurativo) escolhido).usar(alvo);
      }
    }
  }

  /** Interface funcional simples para filtrar itens da mochila com quantidade disponível.*/
  private interface FiltroItem {
    boolean aceita(Item item);
  }

  /** Retorna os itens da mochila com quantidade > 0 que atendem ao filtro informado.*/
  private List<Item> itensDisponiveis(FiltroItem filtro) {
    List<Item> disponiveis = new ArrayList<>();
    for (Item item : jogador.getMochila()) {
      if (item.getQuantidade() > 0 && filtro.aceita(item)) {
        disponiveis.add(item);
      }
    }
    return disponiveis;
  }

  /** Pede ao jogador que escolha um PokeSal do time para receber um item de cura.*/
  private PokeSal escolherPokeSalDoTime() {
    List<PokeSal> time = jogador.getTime();
    if (time.isEmpty()) {
      System.out.println("Seu time está vazio.");
      return null;
    }
    System.out.println("Escolha o PokeSal:");
    for (int i = 0; i < time.size(); i++) {
      PokeSal pokeSal = time.get(i);
      System.out.println((i + 1) + " - " + pokeSal.getClass().getSimpleName()
          + " (HP: " + pokeSal.getHpAtual() + "/" + pokeSal.getHpMax() + ")");
    }
    System.out.print("Opção: ");
    int opcao = lerInteiro();
    if (opcao < 1 || opcao > time.size()) {
      System.out.println("Opção inválida.");
      return null;
    }
    return time.get(opcao - 1);
  }

  /** Lê um inteiro do scanner compartilhado, ignorando entradas inválidas.*/
  private int lerInteiro() {
    while (!scanner.hasNextInt()) {
      System.out.print("Digite um número válido: ");
      scanner.next();
    }
    return scanner.nextInt();
  }

  /** Método que determina os eventos aleatórios.*/
  public void eventosAleatorios() {
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

  /** Verifica se o limite de itens usáveis em uma batalha.*/
  boolean verificarLimiteDeItens(int itensUsados) {
    return itensUsados < LIMITE_ITENS_POR_BATALHA;
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
      escolha = lerInteiro() - 1;
      if (escolha == 2) {
        curaTotal();
        localizacao[2][0] = 1;
        imprimirMapa();
        limparMapa();
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
  }

  /** Metodo para o personagem retornar pelo mapa.*/
  public void retornarMovimento() {
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
    caminho = 0;
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
        pokeSal.setStatus(StatusEfeito.NENHUM);
        curados++;
      }
    }
    System.out.println(curados + " PokeSal(s) curado(s) com sucesso!");
  }
}
