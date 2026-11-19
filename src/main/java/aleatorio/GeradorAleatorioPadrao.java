package aleatorio;

import java.util.Objects;
import java.util.Random;

/**
 * Implementação de produção do {@link GeradorAleatorio}: apenas repassa para um
 * {@link java.util.Random}.
 *
 * <p>Esta é a ÚNICA classe do sistema que conhece {@code Random}. O {@code Random} é recebido
 * pelo construtor (não é criado aqui dentro), então um teste pode entregar um Random com
 * semente fixa e obter sempre a mesma sequência.
 */
public class GeradorAleatorioPadrao implements GeradorAleatorio {

  private final Random random;

  /** Recebe o Random que será usado nos sorteios. */
  public GeradorAleatorioPadrao(Random random) {
    this.random = Objects.requireNonNull(random, "random");
  }

  @Override
  public int proximoInt(int limiteExclusivo) {
    return random.nextInt(limiteExclusivo);
  }
}
