package servicos;

import aleatorio.GeradorAleatorio;
import batalha.MecanicaCombate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import model.elementos.EfeitoTerreno;
import model.elementos.StatusEfeito;
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
import registro.RegistroBatalha;
import terreno.ServicoTerreno;

/**
 * Serviços do jogo que dependem do jogador: menu de batalha selvagem, itens, mapa e eventos.
 *
 * <p>FASE 03: esta classe continua sendo a "casca" de console do jogo (lê o teclado e imprime
 * menus), mas deixou de decidir regras de combate e de criar suas próprias dependências. Todos os
 * colaboradores chegam pelo construtor (Inversão de Dependência):
 * <ul>
 *   <li>{@code MecanicaCombate}: ataque, dano, status e terreno (antes estavam aqui dentro);</li>
 *   <li>{@code ServicoTerreno}: efeito de terreno por clima e horário (antes: {@code Terrenos}
 *       estático);</li>
 *   <li>{@code GeradorAleatorio}: toda a sorte do mapa (antes: um Random criado aqui dentro);</li>
 *   <li>{@code RegistroBatalha}: eventos de uso de item na batalha.</li>
 * </ul>
 */
public class Servicos {
  private Jogador jogador;
  private int[][] localizacao = {{0, 0, 0}, {0, 0, 0}, {0}};
  private PokeSal oponente;
  private int escolha = -1;
  private int caminho = 0;
  private EfeitoTerreno efeitoTerrenoAtual = EfeitoTerreno.NENHUM;
  private final Scanner scanner;
  private final MecanicaCombate mecanica;
  private final ServicoTerreno servicoTerreno;
  private final GeradorAleatorio gerador;
  private final RegistroBatalha registro;
  private static final int LIMITE_ITENS_POR_BATALHA = 2;

  /**
   * Recebe o jogador, um Scanner compartilhado com a Main (evita fechar o System.in mais de uma
   * vez) e os colaboradores externos. Substitui os construtores antigos, que criavam o próprio
   * Scanner e o próprio Random.*/
  public Servicos(Jogador jogador, Scanner scanner, MecanicaCombate mecanica,
      ServicoTerreno servicoTerreno, GeradorAleatorio gerador, RegistroBatalha registro) {
    this.jogador = jogador;
    this.scanner = Objects.requireNonNull(scanner, "scanner");
    this.mecanica = Objects.requireNonNull(mecanica, "mecanica");
    this.servicoTerreno = Objects.requireNonNull(servicoTerreno, "servicoTerreno");
    this.gerador = Objects.requireNonNull(gerador, "gerador");
    this.registro = Objects.requireNonNull(registro, "registro");
  }

  public Jogador getJogador() {
    return jogador;
  }

  public void setJogador(Jogador jogador) {
    this.jogador = jogador;
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

  /** Efeito de terreno sorteado no último evento do mapa. Pacote-privado: usado em testes.*/
  EfeitoTerreno getEfeitoTerrenoAtual() {
    return efeitoTerrenoAtual;
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
    EfeitoTerreno terreno = efeitoTerrenoAtual;

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
          executarAtaqueIa(oponente, meuPokeSal, terreno);
        }
      } else if (mecanica.atacaPrimeiro(meuPokeSal, oponente)) {
        mecanica.executarAtaque(meuPokeSal, oponente, ataqueEscolhido, terreno);
        if (oponente.estaVivo()) {
          executarAtaqueIa(oponente, meuPokeSal, terreno);
        }
      } else {
        executarAtaqueIa(oponente, meuPokeSal, terreno);
        if (meuPokeSal.estaVivo()) {
          mecanica.executarAtaque(meuPokeSal, oponente, ataqueEscolhido, terreno);
        }
      }

      if (meuPokeSal.estaVivo()) {
        mecanica.aplicarStatusFimDeTurno(meuPokeSal);
      }
      if (oponente.estaVivo()) {
        mecanica.aplicarStatusFimDeTurno(oponente);
      }
      if (meuPokeSal.estaVivo()) {
        mecanica.aplicarTerrenoFimDeTurno(meuPokeSal, terreno);
      }
      if (oponente.estaVivo()) {
        mecanica.aplicarTerrenoFimDeTurno(oponente, terreno);
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

  /** Escolhe o primeiro PokeSal do time do jogador que ainda esteja apto a batalhar.*/
  private PokeSal escolherPokeSalParaBatalha() {
    if (jogador == null) {
      return null;
    }
    return jogador.getPrimeiroPokeSalVivo();
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

  /** O oponente escolhe um golpe aleatório (sorteio da mecânica) e o executa.*/
  private void executarAtaqueIa(PokeSal atacante, PokeSal alvo, EfeitoTerreno terreno) {
    mecanica.executarAtaque(atacante, alvo, mecanica.escolherAtaqueIa(atacante), terreno);
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
    registro.registrarUsoItem(escolhido, alvo);
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

  /**
   * Método que determina os eventos aleatórios: primeiro consulta o terreno (clima e horário) e
   * depois sorteia entre achar um item ou enfrentar um PokeSal selvagem.*/
  public void eventosAleatorios() {
    efeitoTerrenoAtual = servicoTerreno.obterEfeitoAtual();
    if (!efeitoTerrenoAtual.getMensagem().isEmpty()) {
      System.out.println(efeitoTerrenoAtual.getMensagem());
    }
    int evento = gerador.proximoInt(2);

    if (evento == 0) {
      Item itemEncontrado = sortearItem(gerador.proximoInt(7));
      itemEncontrado.setQuantidade(1);
      jogador.adicionarItem(itemEncontrado);
      System.out.println("Você encontrou: " + itemEncontrado.getNome());
    } else {
      int especie = gerador.proximoInt(3);
      int nivel = gerador.proximoInt(10) + 1;
      if (especie == 0) {
        oponente = new ChikoSal(nivel);
      } else if (especie == 1) {
        oponente = new TotoSal(nivel);
      } else {
        oponente = new CyndaSal(nivel);
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
        escolha = -1; // BUG-02: sem isto, o próximo "Avançar" estourava ArrayIndexOutOfBounds
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
