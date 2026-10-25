Cada página do guia é uma "entrada": um nome, o assunto da página e onde ela fica no conteúdo. Pacotes de recursos podem adicionar, substituir ou remover entradas.

<chapter name="Arquivos de entradas"/>
As entradas são listadas em um arquivo json em cada namespace de um pacote de recursos:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Ele contém um único objeto que associa o nome de cada entrada à sua definição. Por exemplo:
<json_insn>
{
  "item/wrench": {
    "type": "item_stack",
    "book": "buildcraftreborn:main",
    "tag_type": "item",
    "tag_subtype": "tool",
    "stack": "buildcraftreborn:wrench"
  }
}
</json_insn>
<new_page/>
A página em si é um arquivo separado, com o nome da entrada:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
A página en_us é sempre carregada, e a página no seu idioma (se houver) a substitui. Veja <link to="buildcraftreborn:config/guide_page_format"/> para saber o que vai em uma página.

Os pacotes de recursos mais acima têm prioridade: uma entrada com o mesmo nome substitui a que está abaixo dela, e uma entrada definida como null a remove.

<chapter name="Tipos de entrada"/>
Toda entrada tem um "type":
- item_stack: uma página sobre um item (ou bloco). "stack" é o id do item.
- statement: uma página sobre um gatilho ou uma ação de porta lógica. "statement" é o seu id, como "buildcraft:redstone.input.active".
- external: uma página que não trata de nada do jogo, como esta. "title" é uma chave de tradução, que vem depois de "buildcraft.guide.page.".

Uma entrada cujo item ou statement não existe (por exemplo, porque um mod não está instalado) é ignorada.

<new_page/>
<chapter name="Conteúdo"/>
"book" é o livro em que a entrada está. O livro-guia tem "buildcraftreborn:main" (o Livro-Guia do BuildCraft) e "buildcraftreborn:meta" (este livro).

"tag_type" e "tag_subtype" indicam onde a entrada aparece na página de conteúdo. Eles são traduzidos como "buildcraft.guide.chapter.type.[tag_type]" e "buildcraft.guide.chapter.subtype.[tag_subtype]".

Os livros são listados da mesma forma, em:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Por exemplo:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" é uma chave de tradução, e "all_entries" indica se o conteúdo do livro também lista tudo o que não está em nenhum livro.

Pressione F3 e T com o guia aberto para recarregar tudo.

<italic>O BuildCraft 8.0 definia as entradas em arquivos "guide.txt", com sua própria linguagem de instruções. O BuildCraft Reborn usa json puro.</italic>
