Hver side i guiden er et "opslag": et navn, hvad siden handler om, og hvor den hører hjemme i indholdsfortegnelsen. Ressourcepakker kan tilføje opslag, erstatte dem eller fjerne dem.

<chapter name="Opslagsfiler"/>
Opslagene står i en json-fil i hvert navnerum i en ressourcepakke:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Den indeholder ét objekt, der knytter hvert opslags navn til dets definition. For eksempel:
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
Selve siden er en separat fil, der er opkaldt efter opslaget:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
Siden på en_us indlæses altid, og siden på dit eget sprog (hvis den findes) erstatter den. Se <link to="buildcraftreborn:config/guide_page_format"/> for, hvad en side kan indeholde.

Højere ressourcepakker vinder: Et opslag med samme navn erstatter det nedenunder, og et opslag, der er sat til null, fjerner det.

<chapter name="Opslagstyper"/>
Hvert opslag har en "type":
- item_stack: en side om en genstand (eller blok). "stack" er genstandens id.
- statement: en side om en portudløser eller porthandling. "statement" er dens id, f.eks. "buildcraft:redstone.input.active".
- external: en side, der ikke handler om noget i spillet, som denne. "title" er en oversættelsesnøgle, der kommer efter "buildcraft.guide.page.".

Et opslag, hvis genstand eller statement ikke findes (f.eks. fordi et mod ikke er installeret), udelades.

<new_page/>
<chapter name="Indhold"/>
"book" er den bog, opslaget hører til. Guidebogen har "buildcraftreborn:main" (BuildCraft-guidebogen) og "buildcraftreborn:meta" (denne bog).

"tag_type" og "tag_subtype" bestemmer, hvor opslaget står på indholdssiden. De oversættes som "buildcraft.guide.chapter.type.[tag_type]" og "buildcraft.guide.chapter.subtype.[tag_subtype]".

Bøger angives på samme måde i:
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
"title" er en oversættelsesnøgle, og "all_entries" angiver, om bogens indhold også viser alt det, der ikke er i nogen bog.

Tryk på F3 og T, mens guiden er åben, for at indlæse alt igen.

<italic>BuildCraft 8.0 definerede opslag i "guide.txt"-filer i sit eget instruktionssprog. BuildCraft Reborn bruger i stedet almindelig json.</italic>
