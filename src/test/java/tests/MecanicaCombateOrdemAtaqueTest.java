package tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import aleatorio.GeradorAleatorio;
import batalha.MecanicaCombate;
import model.elementos.StatusEfeito;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import registro.RegistroBatalha;

/**
 * Testes da iniciativa por velocidade (quem ataca primeiro em um turno).
 * MIGRADO da Fase 02 ({@code ServicosOrdemAtaqueTest}): {@code atacaPrimeiro} saiu de
 * {@code Servicos} (que exigia Jogador e Scanner só para ser construída) para a
 * {@code MecanicaCombate}.
 */
@ExtendWith(MockitoExtension.class)
class MecanicaCombateOrdemAtaqueTest {

  @Mock
  private GeradorAleatorio gerador;
  @Mock
  private RegistroBatalha registro;
  @InjectMocks
  private MecanicaCombate mecanica;

  @Test
  void testOrdemDeAtaquePorVelocidade() {
    ChikoSal maisRapido = new ChikoSal();
    maisRapido.setSpd(50);
    ChikoSal maisLento = new ChikoSal();
    maisLento.setSpd(30);

    assertTrue(mecanica.atacaPrimeiro(maisRapido, maisLento),
        "PokeSal com maior SPD deveria agir primeiro");
    assertFalse(mecanica.atacaPrimeiro(maisLento, maisRapido),
        "PokeSal com menor SPD não deveria agir primeiro");

    // Empate de velocidade: o primeiro argumento tem prioridade (>=), conforme o código-fonte.
    ChikoSal empateA = new ChikoSal();
    empateA.setSpd(40);
    ChikoSal empateB = new ChikoSal();
    empateB.setSpd(40);
    assertTrue(mecanica.atacaPrimeiro(empateA, empateB),
        "Em caso de empate, o primeiro PokeSal deveria agir primeiro");

    // Paralisia reduz a SPD efetiva em 30% — pode inverter quem ataca primeiro.
    ChikoSal meuParalisado = new ChikoSal();
    meuParalisado.setSpd(50);
    meuParalisado.setStatus(StatusEfeito.PARALISADO); // spdEfetivo = round(50 * 0.7) = 35
    CyndaSal oponente = new CyndaSal();
    oponente.setSpd(40);
    assertFalse(mecanica.atacaPrimeiro(meuParalisado, oponente),
        "PokeSal paralisado com SPD efetiva menor não deveria agir primeiro");

    CyndaSal oponenteParalisado = new CyndaSal();
    oponenteParalisado.setSpd(50);
    oponenteParalisado.setStatus(StatusEfeito.PARALISADO); // spdEfetivo = 35
    ChikoSal meuNormal = new ChikoSal();
    meuNormal.setSpd(40);
    assertTrue(mecanica.atacaPrimeiro(meuNormal, oponenteParalisado),
        "Oponente paralisado com SPD efetiva menor deveria ceder a iniciativa ao jogador");
  }
}
