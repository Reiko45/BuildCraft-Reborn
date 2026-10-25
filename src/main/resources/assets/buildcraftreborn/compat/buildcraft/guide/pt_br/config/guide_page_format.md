Todas as páginas do guia são escritas em um subconjunto de markdown.

<italic>(Grande parte desta página ainda precisa ser preenchida, mas aqui está uma lista de tags básicas)</italic>

# Tags simples individuais
Toda tag tem a forma &lt;name arguments/&gt;, em que name é o nome da tag e a '/' no final indica que a tag é independente. Algumas tags também têm argumentos na forma de "pares chave-valor", por exemplo:
<guide_md>
"level"="42"
</guide_md>

## Nova página
&lt;new_page/&gt; move para a próxima página o conteúdo que vem depois dela.

## Links
Criam um link para outra página do livro-guia, usando o título e o ícone da página vinculada. Você também pode criar um link para um ItemStack ou Statement mesmo que eles não tenham uma página no livro-guia.
Por exemplo:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
resultará em:
<link to="buildcraftreborn:config/guide_entry_format"/>

ou:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
resultará em:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Capítulos
&lt;chapter/&gt; cria um "título de capítulo", exibido na lista de capítulos como um título colorido. Ela aceita um argumento: "level", que deve ser um número inteiro maior ou igual a 0. Você também pode usar vários símbolos de cerquilha (#), como no markdown comum, para criar títulos. O número de # (menos um) é igual ao nível do título.
Por exemplo:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
resultará em:
### Capítulo de nível 3
<chapter name="Outro capítulo de nível 3" level="2"/>

# Tags simples duplas
Todas elas devem envolver algum conteúdo e precisam ter uma tag de abertura &lt;italic&gt; e uma tag de fechamento &lt;/italic&gt;.
Todas afetam o texto contido entre a tag de abertura e a de fechamento.

- <italic>itálico</italic> (italic)
- <bold>negrito</bold> (bold)
- <underline>sublinhado</underline> (underline)
- <strikethrough>tachado</strikethrough> (strikethrough)
- <red>vermelho</red> (red)
- <black>preto</black> (black)
- <dark_blue>azul-escuro</dark_blue> (dark_blue)
- <dark_green>verde-escuro</dark_green> (dark_green)
- <dark_aqua>ciano-escuro</dark_aqua> (dark_aqua)
- <dark_red>vermelho-escuro</dark_red> (dark_red)
- <dark_purple>roxo-escuro</dark_purple> (dark_purple)
- <gold>dourado</gold> (gold)
- <gray>cinza</gray> (gray)
- <dark_gray>cinza-escuro</dark_gray> (dark_gray)
- <blue>azul</blue> (blue)
- <green>verde</green> (green)
- <aqua>ciano</aqua> (aqua)
- <light_purple>roxo-claro</light_purple> (light_purple)
- <yellow>amarelo</yellow> (yellow)
- <white>branco</white> (white)

Não é uma boa ideia usar verde, ciano, amarelo ou branco, porque normalmente é muito difícil enxergar essas cores na página.

# Tags alternáveis
Todas as tags a seguir dependem de opções e podem ser ativadas ou desativadas pelo jogador.

## História
Se a caixa "Mostrar história [ ]" estiver marcada, tudo o que estiver na tag &lt;no_lore&gt; será ocultado; caso contrário, tudo o que estiver na tag &lt;lore&gt; será ocultado.

A tag lore deve ser usada para mostrar informações do guia "dentro do jogo", narradas do ponto de vista do jogador (como se ele estivesse pesquisando ativamente como tudo funciona). O inverso serve para um tom mais formal, no estilo de uma wiki, usado para transmitir a informação de forma pura e simples.

Isto vem ativado por padrão no jogo e sempre desativado nas exportações.

## Dicas
Se a caixa "Mostrar dicas [ ]" estiver marcada, tudo o que estiver na tag &lt;no_hint&gt; será ocultado; caso contrário, tudo o que estiver na tag &lt;hint&gt; será ocultado.

A tag hint deve ser usada para mostrar "dicas" extras sobre para que a coisa descrita pode ser usada, ou maneiras diferentes de usá-la em combinação com outros blocos ou itens. Basicamente, serve para os "usos recomendados" das coisas. (Como descobrir isso faz parte da diversão, não é obrigatório.) Por isso, vem desativada por padrão, mas pode ser alternada no jogo, e está previsto que também possa ser alternada na exportação.

## Detalhes
Se a opção de configuração do cliente "guideBookEnableDetail" estiver definida como "true", tudo o que estiver na tag &lt;no_detail&gt; será ocultado; caso contrário, tudo o que estiver na tag &lt;detail&gt; será ocultado.

A tag detail deve ser usada para mostrar todos os números usados no cálculo de várias coisas, como a vazão dos tubos, a taxa de extração, a frequência de pulsos etc.

Vem desativada por padrão, mas pode ser alternada no jogo, e está previsto que também possa ser alternada na exportação.

# Diversos
## Receita (singular)
Esta tag mostra a primeira receita encontrada para a pilha de itens indicada. Por exemplo:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Receitas
Esta tag mostra todas as receitas da pilha de itens indicada. Por exemplo:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Usos
Semelhante a recipes, mostra todos os usos da pilha de itens indicada. Por exemplo:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Receitas e usos
Na prática, combina as duas tags acima em uma só, por exemplo:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Grupo
Mostra os grupos correspondentes ao item, statement etc. indicado.

## Imagens
Mostra uma imagem. Há 3 argumentos:
* src: o local de origem da imagem.
* width: a largura com que a imagem será exibida. O padrão é a largura da imagem de origem (mas talvez não seja o que você quer).
* height: a altura com que a imagem será exibida.

Por exemplo:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
