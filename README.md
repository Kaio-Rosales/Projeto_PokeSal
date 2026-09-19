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