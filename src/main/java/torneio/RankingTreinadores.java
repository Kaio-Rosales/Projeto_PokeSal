package torneio;

import batalha.RepositorioResultados;
import batalha.ResultadoBatalha;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import registro.RegistroBatalha;

/**
 * REQUISITO AUTORAL 1 (RA-01): classificação geral dos treinadores.
 *
 * <p>Lê os resultados guardados no {@link RepositorioResultados} e calcula pontos:
 * <ul>
 *   <li>vitória = 3 pontos;</li>
 *   <li>vitória relâmpago (até 6 turnos) = +1 ponto de bônus;</li>
 *   <li>derrota = 0 ponto.</li>
 * </ul>
 * Desempate: mais vitórias, depois MENOS turnos somados, depois ordem alfabética do nome.
 * Se o repositório falhar, o erro vai para o {@link RegistroBatalha} e o ranking sai vazio (o
 * jogo não pode quebrar por causa de uma consulta).
 *
 * <p>Colaboradores externos (mockáveis): {@code RepositorioResultados} e {@code RegistroBatalha}.
 * Os treinadores são identificados pelo NOME (a classe {@code Jogador} não define igualdade).
 */
public class RankingTreinadores {

  static final int PONTOS_POR_VITORIA = 3;
  static final int BONUS_VITORIA_RELAMPAGO = 1;
  static final int LIMITE_VITORIA_RELAMPAGO = 6;

  /** Acumulador interno, mutável, usado só durante o cálculo. */
  private static final class Acumulado {
    private int vitorias;
    private int derrotas;
    private int pontos;
    private int turnos;
  }

  private final RepositorioResultados repositorio;
  private final RegistroBatalha registro;

  /** Recebe o repositório de onde vêm os resultados e o registro para avisar de falhas. */
  public RankingTreinadores(RepositorioResultados repositorio, RegistroBatalha registro) {
    this.repositorio = Objects.requireNonNull(repositorio, "repositorio");
    this.registro = Objects.requireNonNull(registro, "registro");
  }

  /** Gera a classificação, da 1ª posição para a última (lista vazia se não houver dados). */
  public List<LinhaRanking> gerarRanking() {
    List<ResultadoBatalha> resultados;
    try {
      resultados = repositorio.listarTodos();
    } catch (RuntimeException falha) {
      registro.registrarErro("Não foi possível consultar os resultados para o ranking", falha);
      return new ArrayList<>();
    }

    Map<String, Acumulado> porTreinador = new LinkedHashMap<>();
    for (ResultadoBatalha resultado : resultados) {
      Acumulado vencedor = porTreinador.computeIfAbsent(
          resultado.getVencedor().getNome(), nome -> new Acumulado());
      vencedor.vitorias++;
      vencedor.turnos += resultado.getTurnos();
      vencedor.pontos += PONTOS_POR_VITORIA;
      if (resultado.getTurnos() <= LIMITE_VITORIA_RELAMPAGO) {
        vencedor.pontos += BONUS_VITORIA_RELAMPAGO;
      }
      Acumulado perdedor = porTreinador.computeIfAbsent(
          resultado.getPerdedor().getNome(), nome -> new Acumulado());
      perdedor.derrotas++;
      perdedor.turnos += resultado.getTurnos();
    }
    return ordenar(porTreinador);
  }

  private static List<LinhaRanking> ordenar(Map<String, Acumulado> porTreinador) {
    List<Map.Entry<String, Acumulado>> entradas = new ArrayList<>(porTreinador.entrySet());
    entradas.sort(Comparator
        .<Map.Entry<String, Acumulado>>comparingInt(e -> -e.getValue().pontos)
        .thenComparingInt(e -> -e.getValue().vitorias)
        .thenComparingInt(e -> e.getValue().turnos)
        .thenComparing(Map.Entry::getKey));

    List<LinhaRanking> ranking = new ArrayList<>();
    int posicao = 1;
    for (Map.Entry<String, Acumulado> entrada : entradas) {
      Acumulado a = entrada.getValue();
      ranking.add(new LinhaRanking(posicao++, entrada.getKey(), a.vitorias, a.derrotas,
          a.pontos, a.turnos));
    }
    return ranking;
  }
}
