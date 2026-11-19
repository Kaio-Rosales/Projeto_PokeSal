package terreno;

import model.elementos.EfeitoTerreno;

/**
 * Descobre qual efeito de terreno está valendo agora. O efeito deixou de ser fixo: depende do
 * clima e do horário (ver {@link ServicoTerrenoPadrao}).
 */
public interface ServicoTerreno {

  /** Efeito de terreno ativo neste momento (nunca {@code null}). */
  EfeitoTerreno obterEfeitoAtual();
}
