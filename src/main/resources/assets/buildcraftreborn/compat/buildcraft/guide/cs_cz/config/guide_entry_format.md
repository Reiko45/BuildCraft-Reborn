Každá stránka příručky je „položka“: název, o čem stránka je a kam patří v obsahu. Balíčky zdrojů mohou položky přidávat, nahrazovat nebo odebírat.

<chapter name="Soubory položek"/>
Položky jsou uvedené v souboru json v každém jmenném prostoru balíčku zdrojů:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Obsahuje jeden objekt, který ke jménu každé položky přiřazuje její definici. Například:
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
Samotná stránka je samostatný soubor pojmenovaný podle položky:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
Stránka en_us se načte vždy a stránka ve tvém jazyce (pokud existuje) ji nahradí. Co může stránka obsahovat, popisuje <link to="buildcraftreborn:config/guide_page_format"/>.

Výše umístěné balíčky zdrojů mají přednost: položka se stejným názvem nahradí tu z nižšího balíčku a položka nastavená na null ji odebere.

<chapter name="Typy položek"/>
Každá položka má pole "type":
- item_stack: stránka o předmětu (nebo bloku). "stack" je ID předmětu.
- statement: stránka o spouštěči nebo akci brány. "statement" je jejich ID, například "buildcraft:redstone.input.active".
- external: stránka, která se netýká ničeho ve hře, jako tato. "title" je překladový klíč, který následuje po "buildcraft.guide.page.".

Položka, jejíž předmět, spouštěč nebo akce neexistuje (například proto, že není nainstalovaný nějaký mod), se vynechá.

<new_page/>
<chapter name="Obsah"/>
"book" je kniha, ve které položka je. Příručka má knihy "buildcraftreborn:main" (Příručka BuildCraftu) a "buildcraftreborn:meta" (tuto knihu).

"tag_type" a "tag_subtype" určují, kam se položka zařadí v obsahu. Překládají se klíči "buildcraft.guide.chapter.type.[tag_type]" a "buildcraft.guide.chapter.subtype.[tag_subtype]".

Knihy jsou uvedené stejným způsobem, v souboru:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Například:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" je překladový klíč a "all_entries" určuje, zda obsah knihy uvádí i vše, co nepatří do žádné knihy.

Stiskem F3 a T při otevřené příručce vše znovu načteš.

<italic>BuildCraft 8.0 definoval položky v souborech "guide.txt" ve vlastním instrukčním jazyce. BuildCraft Reborn místo toho používá obyčejný json.</italic>
