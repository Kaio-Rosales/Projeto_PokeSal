package batalha;

import java.util.Objects;
import model.jogador.Jogador;
import registro.RegistroBatalha;
import terreno.ServicoTerreno;

/**
 * Fábrica de batalhas: guarda os colaboradores compartilhados e cria um objeto {@link Batalha}
 * novo para cada confronto (pois {@code Batalha} guarda estado de UMA luta).
 */
public class ExecutorBatalhaPadrao implements ExecutorBatalha {

  private final MecanicaCombate mecanica;
  private final ServicoTerreno servicoTerreno;
  private final RepositorioResultados repositorio;
  private final RegistroBatalha registro;

  /** Recebe os colaboradores que serão repassados a cada batalha. */
  public ExecutorBatalhaPadrao(MecanicaCombate mecanica, ServicoTerreno servicoTerreno,
      RepositorioResultados repositorio, RegistroBatalha registro) {
    this.mecanica = Objects.requireNonNull(mecanica, "mecanica");
    this.servicoTerreno = Objects.requireNonNull(servicoTerreno, "servicoTerreno");
    this.repositorio = Objects.requireNonNull(repositorio, "repositorio");
    this.registro = Objects.requireNonNull(registro, "registro");
  }

  @Override
  public ResultadoBatalha lutar(Jogador treinadorA, Jogador treinadorB) {
    return new Batalha(treinadorA, treinadorB, mecanica, servicoTerreno, repositorio, registro)
        .executarAteFim();
  }
}
