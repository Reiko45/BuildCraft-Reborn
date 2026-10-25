Varje sida i guiden är en ”post”: ett namn, vad sidan handlar om och var den hamnar i innehållsförteckningen. Resurspaket kan lägga till, ersätta eller ta bort poster.

<chapter name="Postfiler"/>
Posterna listas i en json-fil i varje namnrymd i ett resurspaket:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Den innehåller ett enda objekt som kopplar varje posts namn till dess definition. Till exempel:
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
Själva sidan är en separat fil som är döpt efter posten:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
Sidan för en_us läses alltid in, och sidan på ditt eget språk (om det finns en) ersätter den. Se <link to="buildcraftreborn:config/guide_page_format"/> för vad en sida kan innehålla.

Resurspaket som ligger högre upp vinner: en post med samma namn ersätter den som ligger under, och en post som är satt till null tar bort den.

<chapter name="Posttyper"/>
Varje post har en "type":
- item_stack: en sida om ett föremål (eller block). "stack" är föremålets id.
- statement: en sida om en grindutlösare eller grindåtgärd. "statement" är dess id, till exempel "buildcraft:redstone.input.active".
- external: en sida som inte handlar om något i spelet, som den här. "title" är en översättningsnyckel, efter "buildcraft.guide.page.".

En post vars föremål, utlösare eller åtgärd inte finns (till exempel för att en mod inte är installerad) utelämnas.

<new_page/>
<chapter name="Innehåll"/>
"book" är boken som posten ligger i. Guiden har "buildcraftreborn:main" (BuildCrafts guidebok) och "buildcraftreborn:meta" (den här boken).

"tag_type" och "tag_subtype" anger var posten hamnar på innehållssidan. De översätts som "buildcraft.guide.chapter.type.[tag_type]" och "buildcraft.guide.chapter.subtype.[tag_subtype]".

Böcker listas på samma sätt, i:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Till exempel:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" är en översättningsnyckel, och "all_entries" anger om bokens innehållsförteckning även listar allt som inte finns i någon bok.

Tryck på F3 och T medan guiden är öppen för att läsa in allt igen.

<italic>BuildCraft 8.0 definierade poster i "guide.txt"-filer, med ett eget instruktionsspråk. BuildCraft Reborn använder vanlig json i stället.</italic>
