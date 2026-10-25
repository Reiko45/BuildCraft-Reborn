Jede Seite der Anleitung ist ein „Eintrag“: ein Name, das Thema der Seite und ihr Platz im Inhalt. Ressourcenpakete können Einträge hinzufügen, ersetzen oder entfernen.

<chapter name="Eintragsdateien"/>
Einträge werden in einer json-Datei in jedem Namensraum eines Ressourcenpakets aufgeführt:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Sie enthält ein einziges Objekt, das jedem Eintragsnamen seine Definition zuordnet. Zum Beispiel:
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
Die Seite selbst ist eine eigene Datei, die nach dem Eintrag benannt ist:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
Die en_us-Seite wird immer geladen, und die Seite in deiner eigenen Sprache (falls vorhanden) ersetzt sie. Was in eine Seite gehört, erfährst du unter <link to="buildcraftreborn:config/guide_page_format"/>.

Höher eingestufte Ressourcenpakete haben Vorrang: Ein Eintrag mit demselben Namen ersetzt den darunterliegenden, und ein auf null gesetzter Eintrag entfernt ihn.

<chapter name="Eintragstypen"/>
Jeder Eintrag hat einen "type":
- item_stack: eine Seite über einen Gegenstand (oder Block). "stack" ist die ID des Gegenstands.
- statement: eine Seite über einen Gatter-Trigger oder eine Gatteraktion. "statement" ist deren ID, etwa "buildcraft:redstone.input.active".
- external: eine Seite, die sich auf nichts im Spiel bezieht, so wie diese hier. "title" ist ein Übersetzungsschlüssel, der auf "buildcraft.guide.page." folgt.

Ein Eintrag, dessen Gegenstand oder Statement nicht existiert (etwa weil eine Mod nicht installiert ist), wird weggelassen.

<new_page/>
<chapter name="Inhalt"/>
"book" ist das Buch, in dem der Eintrag steht. Es gibt die Bücher "buildcraftreborn:main" (die Anleitung) und "buildcraftreborn:meta" (dieses Buch).

"tag_type" und "tag_subtype" bestimmen, wo der Eintrag auf der Inhaltsseite erscheint. Sie werden als "buildcraft.guide.chapter.type.[tag_type]" und "buildcraft.guide.chapter.subtype.[tag_subtype]" übersetzt.

Bücher werden auf dieselbe Weise aufgeführt, und zwar in:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Zum Beispiel:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" ist ein Übersetzungsschlüssel, und "all_entries" legt fest, ob der Inhalt des Buchs auch alles auflistet, was in keinem Buch steht.

Drücke F3 und T, während die Anleitung geöffnet ist, um alles neu zu laden.

<italic>BuildCraft 8.0 hat Einträge in "guide.txt"-Dateien in einer eigenen Anweisungssprache definiert. BuildCraft Reborn verwendet stattdessen einfaches json.</italic>
