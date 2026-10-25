Cada página do guia é uma «entrada»: um nome, o assunto da página e o sítio onde aparece no índice. Os pacotes de recursos podem adicionar, substituir ou remover entradas.

<chapter name="Ficheiros de entradas"/>
As entradas são listadas num ficheiro json em cada espaço de nomes de um pacote de recursos:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Contém um único objeto que associa o nome de cada entrada à sua definição. Por exemplo:
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
A página propriamente dita é um ficheiro separado, com o nome da entrada:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
A página en_us é sempre carregada, e a página no teu idioma (se existir) substitui-a. Consulta <link to="buildcraftreborn:config/guide_page_format"/> para saberes o que se pode pôr numa página.

Os pacotes de recursos mais acima na lista têm prioridade: uma entrada com o mesmo nome substitui a que está abaixo dela, e uma entrada definida como null remove-a.

<chapter name="Tipos de entrada"/>
Cada entrada tem um "type":
- item_stack: uma página sobre um item (ou bloco). "stack" é o id do item.
- statement: uma página sobre um gatilho ou uma ação de gate. "statement" é o seu id, por exemplo "buildcraft:redstone.input.active".
- external: uma página que não é sobre nada do jogo, como esta. "title" é uma chave de tradução, a seguir a "buildcraft.guide.page.".

Uma entrada cujo item, gatilho ou ação não exista (por exemplo, porque um mod não está instalado) é omitida.

<new_page/>
<chapter name="Índice"/>
"book" é o livro em que a entrada está. O guia tem "buildcraftreborn:main" (o Livro-guia do BuildCraft) e "buildcraftreborn:meta" (este livro).

"tag_type" e "tag_subtype" indicam onde a entrada aparece na página de índice. São traduzidos como "buildcraft.guide.chapter.type.[tag_type]" e "buildcraft.guide.chapter.subtype.[tag_subtype]".

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
"title" é uma chave de tradução, e "all_entries" indica se o índice do livro também lista tudo o que não está em nenhum livro.

Prime F3 e T com o guia aberto para voltar a carregar tudo.

<italic>O BuildCraft 8.0 definia as entradas em ficheiros "guide.txt", na sua própria linguagem de instruções. O BuildCraft Reborn usa simplesmente json.</italic>
