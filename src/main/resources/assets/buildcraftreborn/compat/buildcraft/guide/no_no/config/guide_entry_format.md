Hver side i guiden er en "oppføring": et navn, hva siden handler om, og hvor den hører hjemme i innholdsfortegnelsen. Ressurspakker kan legge til, erstatte eller fjerne oppføringer.

<chapter name="Oppføringsfiler"/>
Oppføringene står i en json-fil i hvert navnerom i en ressurspakke:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Den inneholder ett objekt som knytter navnet på hver oppføring til definisjonen av den. For eksempel:
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
Selve siden er en egen fil som er oppkalt etter oppføringen:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
Siden for en_us lastes alltid inn, og siden på ditt eget språk (hvis den finnes) erstatter den. Se <link to="buildcraftreborn:config/guide_page_format"/> for hva en side kan inneholde.

Ressurspakker som ligger høyere, vinner: En oppføring med samme navn erstatter den under, og en oppføring som er satt til null, fjerner den.

<chapter name="Oppføringstyper"/>
Hver oppføring har en "type":
- item_stack: en side om en gjenstand (eller blokk). "stack" er ID-en til gjenstanden.
- statement: en side om en portutløser eller porthandling. "statement" er ID-en, for eksempel "buildcraft:redstone.input.active".
- external: en side som ikke handler om noe i spillet, slik som denne. "title" er en oversettelsesnøkkel som kommer etter "buildcraft.guide.page.".

En oppføring der gjenstanden eller statement ikke finnes (for eksempel fordi en mod ikke er installert), utelates.

<new_page/>
<chapter name="Innhold"/>
"book" er boken oppføringen hører til. Guideboken har "buildcraftreborn:main" (BuildCraft-guideboken) og "buildcraftreborn:meta" (denne boken).

"tag_type" og "tag_subtype" bestemmer hvor oppføringen står på innholdssiden. De oversettes som "buildcraft.guide.chapter.type.[tag_type]" og "buildcraft.guide.chapter.subtype.[tag_subtype]".

Bøker angis på samme måte i:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
For eksempel:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" er en oversettelsesnøkkel, og "all_entries" angir om innholdet i boken også viser alt som ikke er i noen bok.

Trykk F3 og T mens guiden er åpen, for å laste inn alt på nytt.

<italic>BuildCraft 8.0 definerte oppføringer i "guide.txt"-filer, på sitt eget instruksjonsspråk. BuildCraft Reborn bruker vanlig json i stedet.</italic>
