Elke pagina van de handleiding is een ‘vermelding’: een naam, waar de pagina over gaat en waar ze in de inhoud komt te staan. Bronpakketten kunnen vermeldingen toevoegen, vervangen of verwijderen.

<chapter name="Vermeldingsbestanden"/>
Vermeldingen staan in een json-bestand in elke naamruimte van een bronpakket:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Het bevat één object dat de naam van elke vermelding aan haar definitie koppelt. Bijvoorbeeld:
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
De pagina zelf is een apart bestand, genoemd naar de vermelding:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
De en_us-pagina wordt altijd geladen, en de pagina in je eigen taal (als die er is) vervangt haar. Zie <link to="buildcraftreborn:config/guide_page_format"/> voor wat er in een pagina staat.

Hoger geplaatste bronpakketten winnen: een vermelding met dezelfde naam vervangt die eronder, en een vermelding die op null staat, verwijdert haar.

<chapter name="Soorten vermeldingen"/>
Elke vermelding heeft een "type":
- item_stack: een pagina over een voorwerp (of blok). "stack" is de id van het voorwerp.
- statement: een pagina over een poorttrigger of poortactie. "statement" is de id daarvan, zoals "buildcraft:redstone.input.active".
- external: een pagina die niet over iets in het spel gaat, zoals deze. "title" is een vertaalsleutel die volgt op "buildcraft.guide.page.".

Een vermelding waarvan het voorwerp of het statement niet bestaat (bijvoorbeeld omdat een mod niet is geïnstalleerd), wordt weggelaten.

<new_page/>
<chapter name="Inhoud"/>
"book" is het boek waarin de vermelding staat. Er zijn de boeken "buildcraftreborn:main" (de BuildCraft-handleiding) en "buildcraftreborn:meta" (dit boek).

"tag_type" en "tag_subtype" bepalen waar de vermelding op de inhoudspagina komt. Ze worden vertaald als "buildcraft.guide.chapter.type.[tag_type]" en "buildcraft.guide.chapter.subtype.[tag_subtype]".

Boeken worden op dezelfde manier vermeld, in:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Bijvoorbeeld:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" is een vertaalsleutel, en "all_entries" bepaalt of de inhoud van het boek ook alles vermeldt wat in geen enkel boek staat.

Druk op F3 en T terwijl de handleiding open is om alles opnieuw te laden.

<italic>BuildCraft 8.0 definieerde vermeldingen in "guide.txt"-bestanden, in een eigen instructietaal. BuildCraft Reborn gebruikt in plaats daarvan gewoon json.</italic>
