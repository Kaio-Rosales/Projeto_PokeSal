package registro;

import model.elementos.EfeitoTerreno;
import model.elementos.StatusEfeito;
import model.itens.Item;
import model.pokesal.Ataque;
import model.pokesal.PokeSal;

/**
 * Recebe cada evento da batalha. A batalha NUNCA imprime nada: ela apenas avisa o registro.
 *
 * <p>Ordem dos eventos em um turno (requisito 2.4): ataque, dano, efeito de status, efeito de
 * terreno e uso de item. Um teste com {@code InOrder} valida essa ordem.
 */
public interface RegistroBatalha {

  /** Uma batalha entre dois treinadores vai começar (emitido uma única vez, no 1º turno). */
  void registrarInicioBatalha(String nomeTreinadorA, String nomeTreinadorB);

  /** O atacante usou um golpe; {@code acertou} é false quando o golpe errou o alvo. */
  void registrarAtaque(PokeSal atacante, Ataque ataque, boolean acertou);

  /** O alvo sofreu dano (já com crítico, efetividade e terreno aplicados). */
  void registrarDano(PokeSal alvo, int dano, boolean critico, double efetividade);

  /** Um efeito de status entrou em ação (aplicado, dano por turno, curado ou impediu a ação). */
  void registrarEfeitoStatus(PokeSal alvo, StatusEfeito status, String descricao);

  /** O terreno agiu sobre o PokeSal (por exemplo, cura do Terreno Florido). */
  void registrarEfeitoTerreno(PokeSal alvo, EfeitoTerreno efeito, String descricao);

  /** Um item foi usado sobre o alvo. */
  void registrarUsoItem(Item item, PokeSal alvo);

  /** Algo deu errado, mas o jogo continua (ex.: repositório indisponível). */
  void registrarErro(String mensagem, Throwable causa);
}
