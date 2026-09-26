package tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Scanner;
import model.elementos.StatusEfeito;
import model.jogador.Jogador;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import org.junit.jupiter.api.Test;
import servicos.Servicos;

/** Testes da iniciativa por velocidade (quem ataca primeiro em um turno). */
public class ServicosOrdemAtaqueTest {

  private Servicos criarServicos() {
    Jogador jogador = new Jogador("Treinador", new ArrayList<>(), new ArrayList<>());
    return new Servicos(jogador, new Scanner(""));
  }

  @Test
  void testOrdemDeAtaquePorVelocidade() {
    Servicos servicos = criarServicos();

    ChikoSal maisRapido = new ChikoSal();
    maisRapido.setSpd(50);
    ChikoSal maisLento = new ChikoSal();
    maisLento.setSpd(30);

    assertTrue(servicos.atacaPrimeiro(maisRapido, maisLento),
        "PokeSal com maior SPD deveria agir primeiro");
    assertFalse(servicos.atacaPrimeiro(maisLento, maisRapido),
        "PokeSal com menor SPD não deveria agir primeiro");

    // Empate de velocidade: o PokeSal do jogador tem prioridade (>=), conforme o código-fonte.
    ChikoSal empateA = new ChikoSal();
    empateA.setSpd(40);
    ChikoSal empateB = new ChikoSal();
    empateB.setSpd(40);
    assertTrue(servicos.atacaPrimeiro(empateA, empateB),
        "Em caso de empate, o PokeSal do jogador deveria agir primeiro");

    // Paralisia reduz a SPD efetiva em 30% — pode inverter quem ataca primeiro.
    ChikoSal meuParalisado = new ChikoSal();
    meuParalisado.setSpd(50);
    meuParalisado.setStatus(StatusEfeito.PARALISADO); // spdEfetivo = round(50 * 0.7) = 35
    CyndaSal oponente = new CyndaSal();
    oponente.setSpd(40);
    assertFalse(servicos.atacaPrimeiro(meuParalisado, oponente),
        "PokeSal paralisado com SPD efetiva menor não deveria agir primeiro");

    CyndaSal oponenteParalisado = new CyndaSal();
    oponenteParalisado.setSpd(50);
    oponenteParalisado.setStatus(StatusEfeito.PARALISADO); // spdEfetivo = 35
    ChikoSal meuNormal = new ChikoSal();
    meuNormal.setSpd(40);
    assertTrue(servicos.atacaPrimeiro(meuNormal, oponenteParalisado),
        "Oponente paralisado com SPD efetiva menor deveria ceder a iniciativa ao jogador");
  }
}
