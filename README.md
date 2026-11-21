# Projeto_PokeSal
Relatório de requisitos:\
De início, os requisitos iniciais e autorais estavam se adequando ao código, mas, ao avançar nos requisitos, o código se tornou cada vez mais difícil de atualizar e adequar para novas funcionalidades. Isso decorreu de uma falta de estrutura inicial para o sistema e da má utilização e criação dos modelos das peças do sistema.

Relatório de contribuição:\
O projeto foi realizado somente por um indivíduo: Kaio de Jesus Rosales Costa.

Requisitos adicionais:

1. O sistema deve permitir que o jogador mude o nome do personagem.\
    1.1. Após a classe Jogador ser instanciada no método "main", haveria uma opção de configuração e, dentro dela, estaria a opção de trocar o nome default (Dummy).

2. O jogador pode andar pelos campus da UCSAL.\
    2.1. Na classe de serviços, foi criada a matriz "localizacao", uma matriz de 3 linhas, em que 2 linhas têm 3 colunas e a terceira somente uma.\
    2.2. Quando o jogador navegava pela matriz, os 'O's eram trocados por '1's, até chegar ao limite de colunas da linha.\
    2.3. Era possível retornar o avanço, apagando assim os rastros de '1's pela matriz.\
    2.4. Haveria um item chamado "ReturnPass", que permitiria ao jogador voltar para o início de maneira segura e apagaria todos os rastros.

3. Eventos aleatórios devem acontecer quando se faz um movimento de ida ou de volta.\
    3.1. Cada movimentação do jogador acionaria um evento e um terreno.\
    3.2. Os eventos consistiam em achar um item aleatório ou encontrar um "PokeSal" selvagem, ocasionando uma batalha.\
    3.3. Os terrenos eram aleatórios, sendo eles "Quente", "Molhado" e "Florido", cada um com suas peculiaridades. Era possível não pegar nenhum.

![Diagrama de caso de uso](assets/imagens/PokesalUseCase.png)

---

## Fase 2
Checklist de Teste Estático de Código (Revisão Manual):
1. Variáveis inicializadas antes do uso? \
    As variáveis inicializadas antes do uso seriam a matriz localização, usada como "mapa" para movimentação do jogador, as variáveis  escolha e caminho também usadas para movimentação, porém na modificações de valores na matriz e uma menção extra seria para os atributos dos PokeSals que tem valores base.
2. Variáveis/métodos declarados e nunca usados? \
    Os setters setHpMax, setDef e setSpd em *ChikoSal, CyndaSal e TotoSal*, acabaram não sendo utilizados.
3. Código inacessível? \
    Não encontrei códigos inacessíveis.
4. Código duplicado? \
    *ChikoSal, CyndaSal, TotoSal* são parecidos, uma possível herança de uma falta de planejamento e os métodos avancarMovimento() e retornarMovimento() da classe *Servicos* repetem o mesmo loop para percorrer a matriz.
5. Erros de sintaxe? \
    Não encontrei problema ao compilar o código.
6. Erros de lógica que quebram regras do negócio?\
    Ao usar a opção "Usar Itens" no menu da classe *Main* é possível notar que os itens não são empilhados de maneira totalmente correta.
7. Erros de tipagem? \
    Não encontrei erro de tipagem.
8. Fluxo de controle válido (sem loops infinitos/condições impossíveis)? \
    Não encontrei falhas em loops que tornem o fluxo infinito.

---

## Fase 3
Evolução da Fase 02 para suportar **precisão e acerto crítico**, **terreno por clima e horário**, **torneio eliminatório**, **registro de batalha** e **repositório de resultados**, com o código estruturado para ser **testável com Mockito**.

Na Fase 02 a classe `Servicos` fazia tudo: lia o teclado, desenhava o mapa, sorteava eventos e calculava o combate. Ela criava seus próprios `Random` e `Scanner` e lia um terreno guardado em variáveis `static`. Por isso era impossível testar de forma previsível qualquer coisa que dependesse de sorte, clima ou horário. O combate saiu de `Servicos` e foi para `MecanicaCombate`, usada tanto pela batalha selvagem quanto pelo torneio.

**Foram adicionadas as regras novas**: precisão, crítico, terreno dinâmico, batalha entre treinadores, torneio, ranking e premiação.

**Requisitos autorais**
1. **Ranking de treinadores**: 3 pontos por vitória, +1 por vitória relâmpago (até 6 turnos); desempate por vitórias, menos turnos e nome;
2. **Premiação do campeão**: itens na mochila (4 treinadores: Super Poção + Antídoto; 8 treinadores: 2 Mega Poções + Super Poção + Passe de Retorno) e uma notificação.

**Bugs corrigidos**
1. Itens iguais nunca empilhavam na mochila (a comparação usava `equals` de objetos) | Correção: `Jogador.adicionarItem` compara por nome e soma a quantidade;
2. Digitar uma opção inválida no mapa e avançar derrubava o jogo (`ArrayIndexOutOfBoundsException`) | Correção: Estado volta a `-1` no ramo inválido;
3. Adversário podia ter o mesmo nome do jogador e o ranking misturava os dois (código novo da Fase 03) | Correção: Nomes únicos; `Torneio` rejeita nomes repetidos