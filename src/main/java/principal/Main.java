package principal;

import aleatorio.GeradorAleatorio;
import aleatorio.GeradorAleatorioPadrao;
import batalha.ExecutorBatalha;
import batalha.ExecutorBatalhaPadrao;
import batalha.MecanicaCombate;
import batalha.RepositorioResultados;
import batalha.RepositorioResultadosEmMemoria;
import batalha.ResultadoBatalha;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import model.itens.Item;
import model.itens.MegaPotion;
import model.itens.Potion;
import model.itens.ReturnPass;
import model.itens.SuperPotion;
import model.jogador.Jogador;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import model.pokesal.PokeSal;
import model.pokesal.TotoSal;
import registro.RegistroBatalha;
import registro.RegistroBatalhaConsole;
import servicos.Servicos;
import terreno.ProvedorClimaAleatorio;
import terreno.ServicoTerreno;
import terreno.ServicoTerrenoPadrao;
import torneio.GeradorTreinadores;
import torneio.LinhaRanking;
import torneio.NotificadorTorneio;
import torneio.NotificadorTorneioConsole;
import torneio.PremiacaoCampeao;
import torneio.RankingTreinadores;
import torneio.Torneio;

/**
 * Classe com método main para rodar a aplicação.
 *
 * <p>FASE 03: a {@code Main} é a RAIZ DE COMPOSIÇÃO ("composition root"): o único lugar do
 * sistema que escolhe as implementações concretas ({@code Random}, {@code Clock}, console) e as
 * entrega, pelo construtor, às classes de regra de negócio. Trocar uma implementação (por
 * exemplo, um repositório em arquivo) exige mudar apenas este método.*/
public class Main {

  /** Método main.*/
  public static void main(String[] args) {
    Scanner scan = new Scanner(System.in);

    System.out.println("=======================================");
    System.out.println("      Bem-vindo ao mundo de PokeSal!");
    System.out.println("=======================================");
    System.out.print("Qual é o seu nome, treinador(a)? ");
    String nome = scan.nextLine();

    Jogador jogador = new Jogador(nome, new ArrayList<>(), new ArrayList<>());

    PokeSal inicial = escolherPokeSalInicial(scan);
    jogador.getTime().add(inicial);
    entregarItensIniciais(jogador);

    // --- Montagem das dependências (injeção manual) ---
    GeradorAleatorio gerador = new GeradorAleatorioPadrao(new Random());
    RegistroBatalha registro = new RegistroBatalhaConsole(System.out, System.err);
    ServicoTerreno servicoTerreno =
        new ServicoTerrenoPadrao(new ProvedorClimaAleatorio(gerador), Clock.systemDefaultZone());
    MecanicaCombate mecanica = new MecanicaCombate(gerador, registro);
    RepositorioResultados repositorio = new RepositorioResultadosEmMemoria();
    NotificadorTorneio notificador = new NotificadorTorneioConsole(System.out);

    Servicos servicos = new Servicos(jogador, scan, mecanica, servicoTerreno, gerador, registro);
    ModuloTorneio moduloTorneio = new ModuloTorneio(
        new GeradorTreinadores(gerador),
        new ExecutorBatalhaPadrao(mecanica, servicoTerreno, repositorio, registro),
        new PremiacaoCampeao(notificador, registro),
        new RankingTreinadores(repositorio, registro));

    System.out.println("\nBem-vindo(a), " + jogador.getNome() + "! Sua jornada começa agora, com "
        + inicial.getClass().getSimpleName() + " ao seu lado!");

    boolean sair = false;
    while (!sair) {
      exibirMenuPrincipal();
      int opcao = lerOpcao(scan);
      switch (opcao) {
        case 1:
          servicos.imprimirMapa();
          break;
        case 2:
          servicos.avancarMovimento();
          break;
        case 3:
          servicos.retornarMovimento();
          break;
        case 4:
          servicos.usarItem();
          break;
        case 5:
          mostrarTime(jogador);
          break;
        case 6:
          menuConfiguracoes(scan, servicos);
          break;
        case 7:
          moduloTorneio.executar(scan, jogador);
          break;
        case 8:
          sair = true;
          System.out.println("Até a próxima, " + jogador.getNome() + "!");
          break;
        default:
          System.out.println("Opção inválida. Tente novamente.");
      }
    }
    servicos.encerrar();
  }

  /** Exibe as opções do menu principal.*/
  private static void exibirMenuPrincipal() {
    System.out.println("\n============ MENU ============");
    System.out.println("1 - Ver mapa");
    System.out.println("2 - Avançar");
    System.out.println("3 - Retornar");
    System.out.println("4 - Usar Item");
    System.out.println("5 - Ver time PokeSal");
    System.out.println("6 - Configurações");
    System.out.println("7 - Torneio");
    System.out.println("8 - Sair");
    System.out.print("Escolha uma opção: ");
  }

  /** Lê uma opção inteira do menu, tratando entradas inválidas sem travar o programa.*/
  private static int lerOpcao(Scanner scan) {
    while (!scan.hasNextInt()) {
      System.out.print("Digite um número válido: ");
      scan.next();
    }
    return scan.nextInt();
  }

  /** Pede ao jogador que escolha um dos três PokeSals iniciais.*/
  private static PokeSal escolherPokeSalInicial(Scanner scan) {
    PokeSal escolhido = null;
    while (escolhido == null) {
      System.out.println("\nEscolha seu PokeSal inicial:");
      System.out.println("1 - ChikoSal (Planta)");
      System.out.println("2 - CyndaSal (Fogo)");
      System.out.println("3 - TotoSal (Água)");
      System.out.print("Opção: ");
      int opcao = lerOpcao(scan);
      switch (opcao) {
        case 1:
          escolhido = new ChikoSal();
          break;
        case 2:
          escolhido = new CyndaSal();
          break;
        case 3:
          escolhido = new TotoSal();
          break;
        default:
          System.out.println("Opção inválida.");
      }
    }
    return escolhido;
  }

  /** Entrega os itens de partida: 1 ReturnPass, 1 Potion, 1 SuperPotion e 1 MegaPotion.*/
  private static void entregarItensIniciais(Jogador jogador) {
    ReturnPass returnPass = new ReturnPass();
    returnPass.setQuantidade(1);
    Potion potion = new Potion();
    potion.setQuantidade(1);
    SuperPotion superPotion = new SuperPotion();
    superPotion.setQuantidade(1);
    MegaPotion megaPotion = new MegaPotion();
    megaPotion.setQuantidade(1);

    List<Item> mochila = jogador.getMochila();

    mochila.add(returnPass);
    mochila.add(potion);
    mochila.add(superPotion);
    mochila.add(megaPotion);
  }

  /** Mostra todos os PokeSals do time do jogador com seus atributos atuais.*/
  private static void mostrarTime(Jogador jogador) {
    System.out.println("\n--- Seu time PokeSal ---");
    List<PokeSal> time = jogador.getTime();
    if (time.isEmpty()) {
      System.out.println("Seu time está vazio.");
      return;
    }
    int i = 1;
    for (PokeSal pokeSal : time) {
      System.out.println(i + " - " + pokeSal.getClass().getSimpleName()
          + " | Nível: " + pokeSal.getNivel()
          + " | HP: " + pokeSal.getHpAtual() + "/" + pokeSal.getHpMax()
          + " | ATK: " + pokeSal.getAtk() + " | DEF: " + pokeSal.getDef()
          + " | SPD: " + pokeSal.getSpd()
          + " | Elemento: " + pokeSal.getElemento()
          + " | Status: " + pokeSal.getStatus());
      i++;
    }
  }

  /** Submenu de configurações: ver/trocar perfil e voltar ao menu principal.*/
  private static void menuConfiguracoes(Scanner scan, Servicos servicos) {
    boolean voltar = false;
    while (!voltar) {
      System.out.println("\n--- Configurações ---");
      System.out.println("1 - Ver perfil");
      System.out.println("2 - Voltar ao menu principal");
      System.out.print("Opção: ");
      int opcao = lerOpcao(scan);
      switch (opcao) {
        case 1:
          servicos.verificarNome();
          break;
        case 2:
          voltar = true;
          break;
        default:
          System.out.println("Opção inválida.");
      }
    }
  }

  /**
   * Parte "console" do torneio: pergunta o tamanho, mostra os resultados, premia o campeão e
   * exibe o ranking. As REGRAS ficam nas classes de {@code batalha} e {@code torneio}.*/
  private static final class ModuloTorneio {
    private final GeradorTreinadores geradorTreinadores;
    private final ExecutorBatalha executor;
    private final PremiacaoCampeao premiacao;
    private final RankingTreinadores ranking;

    private ModuloTorneio(GeradorTreinadores geradorTreinadores, ExecutorBatalha executor,
        PremiacaoCampeao premiacao, RankingTreinadores ranking) {
      this.geradorTreinadores = geradorTreinadores;
      this.executor = executor;
      this.premiacao = premiacao;
      this.ranking = ranking;
    }

    private void executar(Scanner scan, Jogador jogador) {
      System.out.println("\n--- Torneio do Estacionamento da UCSal Pituaçu ---");
      System.out.println("Escolha o tamanho do torneio:");
      System.out.println("1 - 4 treinadores");
      System.out.println("2 - 8 treinadores");
      System.out.println("0 - Cancelar");
      System.out.print("Opção: ");
      int opcao = lerOpcao(scan);
      if (opcao != 1 && opcao != 2) {
        System.out.println("Torneio cancelado.");
        return;
      }
      int total = opcao == 1 ? 4 : 8;

      List<Jogador> participantes = geradorTreinadores.montarParticipantes(jogador, total);
      System.out.println("\nParticipantes:");
      for (Jogador participante : participantes) {
        System.out.println(" - " + participante.getNome());
      }

      Torneio torneio = new Torneio(executor);
      Jogador campeao = torneio.disputar(participantes);

      System.out.println("\n--- Resultados das batalhas ---");
      for (ResultadoBatalha resultado : torneio.getHistorico()) {
        System.out.println(resultado.getVencedor().getNome() + " venceu "
            + resultado.getPerdedor().getNome() + " em " + resultado.getTurnos() + " turno(s).");
      }

      premiacao.premiar(campeao, total);
      mostrarRanking();
    }

    private void mostrarRanking() {
      System.out.println("\n--- Classificação geral ---");
      for (LinhaRanking linha : ranking.gerarRanking()) {
        System.out.println(linha.getPosicao() + "º " + linha.getTreinador()
            + " | pontos: " + linha.getPontos()
            + " | vitórias: " + linha.getVitorias()
            + " | derrotas: " + linha.getDerrotas());
      }
    }
  }
}