package batalha;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Implementação simples do repositório: guarda os resultados numa lista em memória. */
public class RepositorioResultadosEmMemoria implements RepositorioResultados {

  private final List<ResultadoBatalha> resultados = new ArrayList<>();

  @Override
  public void salvar(ResultadoBatalha resultado) {
    resultados.add(Objects.requireNonNull(resultado, "resultado"));
  }

  @Override
  public List<ResultadoBatalha> listarTodos() {
    return Collections.unmodifiableList(new ArrayList<>(resultados));
  }
}
