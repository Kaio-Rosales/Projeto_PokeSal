package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Scanner;
import model.elementos.Terrenos;
import model.elementos.TipoElemental;
import model.itens.Potion;
import model.jogador.Jogador;
import model.pokesal.Ataque;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import servicos.Servicos;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Testes de valores limite (boundary values) para HP, ATK e DEF no cálculo de dano. */
class ServicosCalculoDanoTest {

  private Servicos servicos;

  @BeforeEach
  void configurar() {
    Jogador jogador = new Jogador("Treinador", new ArrayList<>(), new ArrayList<>());
    servicos = new Servicos(jogador, new Scanner(""));
    Terrenos.limparTerrenos(); // estado static compartilhado: garante um cenário neutro
  }

  @AfterEach
  void limpar() {
    Terrenos.limparTerrenos();
  }

  @Test
  void testCalculoDanoBoundaryValues() {
    // --- Boundary de ATK = 0: o dano bruto deve cair só no danoBase (atk / divisor = 0) ---
    Ataque golpeNeutro = new Ataque("Golpe Teste", TipoElemental.NORMAL, 10, 5, 1);
    ChikoSal atacanteSemAtaque = new ChikoSal();
    atacanteSemAtaque.setAtk(0);
    ChikoSal alvoDefesaZero = new ChikoSal();
    alvoDefesaZero.setDef(0);
    // danoBruto = 10 + 0/5 = 10; efetividade Normal->Grama = 1.0; terreno = 1.0; def/10 = 0.
    assertEquals(10, servicos.calcularDano(atacanteSemAtaque, alvoDefesaZero, golpeNeutro),
        "Com ATK 0 e DEF 0, o dano deveria ser exatamente o danoBase do golpe");

    // --- Boundary de DEF muito alta: o dano nunca pode ser menor que 1 ---
    ChikoSal alvoDefesaAltissima = new ChikoSal();
    alvoDefesaAltissima.setDef(100_000);
    int danoComDefesaAltissima =
        servicos.calcularDano(atacanteSemAtaque, alvoDefesaAltissima, golpeNeutro);
    assertEquals(1, danoComDefesaAltissima,
        "Dano nunca deveria ser menor que 1, mesmo com DEF absurdamente alta");

    // --- Vantagem elemental (2.0x) combinada com terreno (Fogo, +15%) ---
    Ataque golpeDeFogo = new Ataque("Brasa Teste", TipoElemental.FOGO, 10, 2, 1);
    CyndaSal atacanteDeFogo = new CyndaSal();
    atacanteDeFogo.setAtk(20); // danoBruto = 10 + 20/2 = 20
    ChikoSal alvoDeGrama = new ChikoSal();
    alvoDeGrama.setDef(0);
    Terrenos.validarTerrenos(0); // dia quente: +15% para golpes de Fogo
    // danoComBonus = round(20 * 2.0 * 1.15) = round(46.0) = 46; menos def/10 (=0) => 46
    assertEquals(46, servicos.calcularDano(atacanteDeFogo, alvoDeGrama, golpeDeFogo),
        "Vantagem elemental e bônus de terreno deveriam se multiplicar e arredondar corretamente");

    // --- Boundary de HP: estaVivo() na fronteira exata entre 0 e 1 ---
    ChikoSal pokeSal = new ChikoSal();
    pokeSal.setHpAtual(0);
    assertFalse(pokeSal.estaVivo(), "HP igual a 0 deveria significar desmaiado");
    pokeSal.setHpAtual(1);
    assertTrue(pokeSal.estaVivo(), "HP igual a 1 já deveria significar vivo");

    // --- Boundary de cura: não pode ultrapassar o HP máximo ---
    ChikoSal quaseCheio = new ChikoSal();
    quaseCheio.setHpAtual(quaseCheio.getHpMax() - 5);
    Potion potion = new Potion();
    potion.setQuantidade(1);
    potion.usar(quaseCheio); // cura 20, mas deveria travar em hpMax
    assertEquals(quaseCheio.getHpMax(), quaseCheio.getHpAtual(),
        "Cura não deveria ultrapassar o HP máximo do PokeSal");
  }
}
