package aleatorio;

/**
 * Fonte de aleatoriedade do jogo.
 *
 * <p>FASE 03: toda sorte (acertar golpe, crítico, efeito de status, escolha de golpe da IA,
 * sorteio de itens e oponentes) passa por esta interface. Assim os testes trocam o sorteio real
 * por um MOCK e controlam exatamente o que "aconteceu". Princípio da Inversão de Dependência
 * (o "D" do SOLID): as regras dependem desta abstração, não de {@code java.util.Random}.
 */
public interface GeradorAleatorio {

  /**
   * Sorteia um inteiro de 0 (inclusive) até {@code limiteExclusivo} (exclusive).
   *
   * @param limiteExclusivo deve ser maior que zero
   */
  int proximoInt(int limiteExclusivo);
}
