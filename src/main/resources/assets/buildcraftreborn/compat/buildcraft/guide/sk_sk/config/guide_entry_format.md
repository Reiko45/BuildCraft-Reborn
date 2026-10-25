Každá stránka príručky je „položka“: má názov, tému stránky a miesto v obsahu. Balíky zdrojov môžu položky pridávať, nahrádzať alebo odstraňovať.

<chapter name="Súbory položiek"/>
Položky sú uvedené v súbore json v každom mennom priestore balíka zdrojov:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Obsahuje jeden objekt, ktorý ku každému názvu položky priraďuje jej definíciu. Napríklad:
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
Samotná stránka je samostatný súbor pomenovaný podľa položky:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
Stránka en_us sa načíta vždy a stránka vo vašom jazyku (ak existuje) ju nahradí. Čo môže stránka obsahovať, sa dočítate tu: <link to="buildcraftreborn:config/guide_page_format"/>.

Vyššie balíky zdrojov majú prednosť: položka s rovnakým názvom nahradí tú pod ňou a položka nastavená na null ju odstráni.

<chapter name="Typy položiek"/>
Každá položka má pole "type":
- item_stack: stránka o predmete (alebo bloku). "stack" je ID predmetu.
- statement: stránka o spúšťači alebo akcii brány. "statement" je ich ID, napríklad "buildcraft:redstone.input.active".
- external: stránka, ktorá nie je o ničom v hre, ako napríklad táto. "title" je prekladový kľúč za "buildcraft.guide.page.".

Položka, ktorej predmet alebo statement neexistuje (napríklad preto, že mod nie je nainštalovaný), sa vynechá.

<new_page/>
<chapter name="Obsah"/>
"book" je kniha, v ktorej sa položka nachádza. Príručka má knihy "buildcraftreborn:main" (Príručka BuildCraftu) a "buildcraftreborn:meta" (táto kniha).

"tag_type" a "tag_subtype" určujú, kam položka patrí na stránke obsahu. Prekladajú sa ako "buildcraft.guide.chapter.type.[tag_type]" a "buildcraft.guide.chapter.subtype.[tag_subtype]".

Knihy sa uvádzajú rovnakým spôsobom, v súbore:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Napríklad:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" je prekladový kľúč a "all_entries" určuje, či obsah knihy uvádza aj všetko, čo nie je v žiadnej knihe.

Ak chcete všetko znova načítať, stlačte pri otvorenej príručke F3 a T.

<italic>BuildCraft 8.0 definoval položky v súboroch "guide.txt" vo vlastnom inštrukčnom jazyku. BuildCraft Reborn namiesto toho používa obyčajný json.</italic>
