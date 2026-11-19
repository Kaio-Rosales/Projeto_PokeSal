package registro;

import java.io.PrintStream;
import java.util.Objects;
import model.elementos.EfeitoTerreno;
import model.elementos.StatusEfeito;
import model.itens.Item;
import model.pokesal.Ataque;
import model.pokesal.PokeSal;

/**
 * Implementação de produção do {@link RegistroBatalha}: escreve no console.
 *
 * <p>Os textos são os mesmos que antes estavam espalhados dentro de {@code Servicos}. A saída é
 * recebida no construtor ({@code System.out} em produção), o que permite capturá-la em testes.
 */
public class RegistroBatalhaConsole implements RegistroBatalha {

  private final PrintStream saida;
  private final PrintStream saidaErro;

  /** Recebe os fluxos de saída normal e de erro. */
  public RegistroBatalhaConsole(PrintStream saida, PrintStream saidaErro) {
    this.saida = Objects.requireNonNull(saida, "saida");
    this.saidaErro = Objects.requireNonNull(saidaErro, "saidaErro");
  }

  @Override
  public void registrarInicioBatalha(String nomeTreinadorA, String nomeTreinadorB) {
    saida.println("\n=== Batalha: " + nomeTreinadorA + " x " + nomeTreinadorB + " ===");
  }

  @Override
  public void registrarAtaque(PokeSal atacante, Ataque ataque, boolean acertou) {
    saida.println(atacante.getClass().getSimpleName() + " usou " + ataque.getNome() + "!");
    if (!acertou) {
      saida.println("Mas o golpe errou o alvo!");
    }
  }

  @Override
  public void registrarDano(PokeSal alvo, int dano, boolean critico, double efetividade) {
    if (critico) {
      saida.println("Acerto crítico!");
    }
    if (efetividade > 1.0) {
      saida.println("É super efetivo!");
    } else if (efetividade < 1.0) {
      saida.println("Não é muito efetivo...");
    }
    saida.println(alvo.getClass().getSimpleName() + " sofreu " + dano + " de dano! (HP: "
        + alvo.getHpAtual() + "/" + alvo.getHpMax() + ")");
    if (!alvo.estaVivo()) {
      saida.println(alvo.getClass().getSimpleName() + " desmaiou!");
    }
  }

  @Override
  public void registrarEfeitoStatus(PokeSal alvo, StatusEfeito status, String descricao) {
    saida.println(descricao);
  }

  @Override
  public void registrarEfeitoTerreno(PokeSal alvo, EfeitoTerreno efeito, String descricao) {
    saida.println(descricao);
  }

  @Override
  public void registrarUsoItem(Item item, PokeSal alvo) {
    saida.println(item.getNome() + " foi usado em " + alvo.getClass().getSimpleName() + ".");
  }

  @Override
  public void registrarErro(String mensagem, Throwable causa) {
    saidaErro.println("[ERRO] " + mensagem + (causa == null ? "" : ": " + causa.getMessage()));
  }
}
