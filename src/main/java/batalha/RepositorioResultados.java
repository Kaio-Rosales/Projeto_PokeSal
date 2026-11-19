package batalha;

import java.util.List;

/**
 * Onde os resultados das batalhas são guardados. Poderia ser um arquivo ou um banco de dados;
 * por ser interface, a batalha não sabe (nem precisa saber) e os testes usam um mock.
 */
public interface RepositorioResultados {

  /**
   * Guarda o resultado de uma batalha.
   *
   * @throws RuntimeException se o armazenamento estiver indisponível (a batalha trata a falha)
   */
  void salvar(ResultadoBatalha resultado);

  /** Todos os resultados já guardados, do mais antigo para o mais recente. */
  List<ResultadoBatalha> listarTodos();
}
