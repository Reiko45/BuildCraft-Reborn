Todas as páginas do guia são escritas num subconjunto de markdown.

<italic>(A maior parte desta página ainda está por preencher, mas fica aqui uma lista das etiquetas básicas)</italic>

# Etiquetas individuais simples
Todas as etiquetas têm a forma &lt;name arguments/&gt;, em que name é o nome da etiqueta e a '/' final indica que a etiqueta é independente. Algumas etiquetas também têm argumentos na forma de «pares chave-valor», por exemplo:
<guide_md>
"level"="42"
</guide_md>

## Nova página
&lt;new_page/&gt; faz passar o conteúdo que vem a seguir para a página seguinte.

## Ligações
Estas etiquetas criam uma ligação para outra página do livro-guia, usando o título e o ícone dessa página. Em alternativa, podes criar uma ligação para um ItemStack ou um Statement, mesmo que não tenham uma página própria no livro-guia.
Por exemplo:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
resulta em:
<link to="buildcraftreborn:config/guide_entry_format"/>

ou:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
resulta em:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Capítulos
&lt;chapter/&gt; cria um «título de capítulo», que aparece na lista de capítulos como um título colorido. Tem um único argumento possível: "level", que tem de ser um número inteiro igual ou superior a 0. Em alternativa, podes usar vários cardinais (#), tal como no formato markdown normal, para criar títulos. O número de # (menos um) é igual ao nível do título.
Por exemplo:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
resulta em:
### Capítulo de nível 3
<chapter name="Outro capítulo de nível 3" level="2"/>

# Etiquetas duplas simples
Todas estas etiquetas têm de envolver algum conteúdo e têm de ter uma etiqueta de abertura &lt;italic&gt; e uma etiqueta de fecho &lt;/italic&gt;
Todas afetam o texto contido entre as etiquetas de abertura e de fecho.

- <italic>italic</italic> (itálico)
- <bold>bold</bold> (negrito)
- <underline>underlined</underline> (sublinhado)
- <strikethrough>strikethrough</strikethrough> (rasurado)
- <red>red</red> (vermelho)
- <black>black</black> (preto)
- <dark_blue>dark_blue</dark_blue> (azul-escuro)
- <dark_green>dark_green</dark_green> (verde-escuro)
- <dark_aqua>dark_aqua</dark_aqua> (ciano-escuro)
- <dark_red>dark_red</dark_red> (vermelho-escuro)
- <dark_purple>dark_purple</dark_purple> (roxo-escuro)
- <gold>gold</gold> (dourado)
- <gray>gray</gray> (cinzento)
- <dark_gray>dark_gray</dark_gray> (cinzento-escuro)
- <blue>blue</blue> (azul)
- <green>green</green> (green, verde)
- <aqua>aqua</aqua> (aqua, ciano)
- <light_purple>light_purple</light_purple> (roxo-claro)
- <yellow>yellow</yellow> (yellow, amarelo)
- <white>white</white> (white, branco)

Não é boa ideia usar verde, ciano, amarelo ou branco, porque costuma ser muito difícil ver qualquer uma dessas cores na página.

# Etiquetas alternáveis
Todas as etiquetas seguintes dependem de opções e podem ser ativadas ou desativadas pelo jogador.

## Narrativa
Se a caixa «Mostrar história [ ]» estiver assinalada, tudo o que estiver na etiqueta &lt;no_lore&gt; fica oculto; caso contrário, fica oculto tudo o que estiver na etiqueta &lt;lore&gt;.

A etiqueta lore deve ser usada para mostrar informação do guia «dentro do jogo», narrada da perspetiva do jogador (como se estivesse a investigar ativamente como tudo funciona). O inverso serve para um tom mais formal, ao estilo da Wikipédia, usado para transmitir a informação, pura e simplesmente.

Esta opção está ativada por predefinição no jogo e sempre desativada nas exportações.

## Dicas
Se a caixa «Mostrar dicas [ ]» estiver assinalada, tudo o que estiver na etiqueta &lt;no_hint&gt; fica oculto; caso contrário, fica oculto tudo o que estiver na etiqueta &lt;hint&gt;.

A etiqueta hint deve ser usada para mostrar «dicas» extra sobre aquilo para que a coisa descrita pode ser usada, ou sobre diferentes formas de a usar em combinação com outros blocos ou itens. No fundo, serve para as «utilizações recomendadas» de várias coisas. (Como descobrir isto faz parte da diversão, não é obrigatório.) Por isso, está desativada por predefinição, mas pode ser alternada no jogo e está previsto que também possa ser alternada nas exportações.

## Detalhe
Se a opção de configuração do cliente "guideBookEnableDetail" estiver definida como "true", tudo o que estiver na etiqueta &lt;no_detail&gt; fica oculto; caso contrário, fica oculto tudo o que estiver na etiqueta &lt;detail&gt;.

A etiqueta detail deve ser usada para mostrar todos os números usados no cálculo de várias coisas, como o caudal dos tubos, a taxa de extração, a frequência dos impulsos, etc.

Está desativada por predefinição, mas pode ser alternada no jogo e está previsto que também possa ser alternada nas exportações.

# Diversos
## Receita (singular)
Esta etiqueta mostra a primeira receita encontrada para a pilha de itens indicada. Por exemplo:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Receitas
Esta etiqueta mostra todas as receitas da pilha de itens indicada. Por exemplo:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Utilizações
Tal como recipes, esta etiqueta mostra todas as utilizações da pilha de itens indicada. Por exemplo:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Receitas e utilizações
Esta etiqueta junta, na prática, as duas anteriores numa só, por exemplo:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Grupo
Mostra os grupos adequados ao item, gatilho, ação, etc. indicado.

## Imagens
Mostra uma imagem. Tem 3 argumentos:
* src: a localização de origem da imagem.
* width: a largura com que a imagem é mostrada. Por predefinição, é a largura da imagem de origem (o que pode não ser o que pretendes).
* height: a altura com que a imagem é mostrada.

Por exemplo:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
