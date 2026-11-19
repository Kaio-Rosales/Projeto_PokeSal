package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import aleatorio.GeradorAleatorio;
import batalha.MecanicaCombate;
import model.elementos.EfeitoTerreno;
import model.elementos.TipoElemental;
import model.itens.Potion;
import model.pokesal.Ataque;
import model.pokesal.ChikoSal;
import model.pokesal.CyndaSal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import registro.RegistroBatalha;

/**
 * Testes de valores limite (boundary values) para HP, ATK e DEF no cálculo de dano.
 * MIGRADO da Fase 02 ({@code ServicosCalculoDanoTest}).
 *
 * <p>O que mudou: o cálculo agora mora em {@code MecanicaCombate} e recebe o terreno como
 * ARGUMENTO (antes lia um campo static). O {@code gerador} e o {@code registro} são mocks que
 * ninguém chama aqui: {@code calcularDano} é determinístico (sem sorteio e sem registro).
 */
@ExtendWith(MockitoExtension.class)
class MecanicaCombateCalculoDanoTest {

  @Mock
  private GeradorAleatorio gerador;
  @Mock
  private RegistroBatalha registro;
  @InjectMocks
  private MecanicaCombate mecanica;

  @Test
  void testCalculoDanoBoundaryValues() {
    // --- Boundary de ATK = 0: o dano bruto deve cair só no danoBase (atk / divisor = 0) ---
    Ataque golpeNeutro = new Ataque("Golpe Teste", TipoElemental.NORMAL, 10, 5, 1);
    ChikoSal atacanteSemAtaque = new ChikoSal();
    atacanteSemAtaque.setAtk(0);
    ChikoSal alvoDefesaZero = new ChikoSal();
    alvoDefesaZero.setDef(0);
    // danoBruto = 10 + 0/5 = 10; efetividade Normal->Grama = 1.0; terreno = 1.0; def/10 = 0.
    assertEquals(10,
        mecanica.calcularDano(atacanteSemAtaque, alvoDefesaZero, golpeNeutro,
            EfeitoTerreno.NENHUM),
        "Com ATK 0 e DEF 0, o dano deveria ser exatamente o danoBase do golpe");

    // --- Boundary de DEF muito alta: o dano nunca pode ser menor que 1 ---
    ChikoSal alvoDefesaAltissima = new ChikoSal();
    alvoDefesaAltissima.setDef(100_000);
    assertEquals(1,
        mecanica.calcularDano(atacanteSemAtaque, alvoDefesaAltissima, golpeNeutro,
            EfeitoTerreno.NENHUM),
        "Dano nunca deveria ser menor que 1, mesmo com DEF absurdamente alta");

    // --- Vantagem elemental (2.0x) combinada com terreno (Fogo, +15%) ---
    Ataque golpeDeFogo = new Ataque("Brasa Teste", TipoElemental.FOGO, 10, 2, 1);
    CyndaSal atacanteDeFogo = new CyndaSal();
    atacanteDeFogo.setAtk(20); // danoBruto = 10 + 20/2 = 20
    ChikoSal alvoDeGrama = new ChikoSal();
    alvoDeGrama.setDef(0);
    // danoComBonus = round(20 * 2.0 * 1.15) = round(46.0) = 46; menos def/10 (=0) => 46
    assertEquals(46,
        mecanica.calcularDano(atacanteDeFogo, alvoDeGrama, golpeDeFogo,
            EfeitoTerreno.ASFALTO_QUENTE),
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
