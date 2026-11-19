package terreno;

import java.time.Clock;
import java.time.LocalTime;
import java.util.Objects;
import model.elementos.Clima;
import model.elementos.EfeitoTerreno;

/**
 * Regra de negócio do terreno: combina CLIMA + HORÁRIO.
 *
 * <ul>
 *   <li>ENSOLARADO: Asfalto Quente, mas SÓ durante o dia (06:00 inclusive até 18:00 exclusive);
 *       à noite o asfalto esfria e não há efeito.</li>
 *   <li>CHUVOSO: Chão Molhado a qualquer hora.</li>
 *   <li>NUBLADO: Terreno Florido.</li>
 *   <li>VENTOSO: sem efeito.</li>
 * </ul>
 *
 * <p>O horário vem de um {@link Clock} recebido no construtor (nunca de {@code LocalTime.now()}
 * sem argumento). Em produção usa-se {@code Clock.systemDefaultZone()}; nos testes,
 * {@code Clock.fixed(...)} ou um mock.
 */
public class ServicoTerrenoPadrao implements ServicoTerreno {

  static final LocalTime INICIO_DIA = LocalTime.of(6, 0);
  static final LocalTime FIM_DIA = LocalTime.of(18, 0);

  private final ProvedorClima provedorClima;
  private final Clock relogio;

  /** Recebe o serviço de clima e o relógio. */
  public ServicoTerrenoPadrao(ProvedorClima provedorClima, Clock relogio) {
    this.provedorClima = Objects.requireNonNull(provedorClima, "provedorClima");
    this.relogio = Objects.requireNonNull(relogio, "relogio");
  }

  @Override
  public EfeitoTerreno obterEfeitoAtual() {
    Clima clima = provedorClima.obterClima();
    if (clima == Clima.ENSOLARADO) {
      return estaDeDia() ? EfeitoTerreno.ASFALTO_QUENTE : EfeitoTerreno.NENHUM;
    }
    if (clima == Clima.CHUVOSO) {
      return EfeitoTerreno.CHAO_MOLHADO;
    }
    if (clima == Clima.NUBLADO) {
      return EfeitoTerreno.TERRENO_FLORIDO;
    }
    return EfeitoTerreno.NENHUM;
  }

  private boolean estaDeDia() {
    LocalTime agora = LocalTime.now(relogio);
    return !agora.isBefore(INICIO_DIA) && agora.isBefore(FIM_DIA);
  }
}
