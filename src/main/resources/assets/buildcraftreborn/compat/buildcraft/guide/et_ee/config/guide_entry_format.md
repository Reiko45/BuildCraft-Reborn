Juhendi iga leht on „kirje“: nimi, lehe teema ja koht sisukorras. Ressursipakid saavad kirjeid lisada, asendada või eemaldada.

<chapter name="Kirjefailid"/>
Kirjed on loetletud json-failis ressursipaki igas nimeruumis:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
See sisaldab ühte objekti, mis seob iga kirje nime selle definitsiooniga. Näiteks:
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
Leht ise on eraldi fail, mis on nimetatud kirje järgi:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
en_us leht laaditakse alati ja sinu enda keeles olev leht (kui see on olemas) asendab selle. Lehe sisu kohta vaata <link to="buildcraftreborn:config/guide_page_format"/>.

Kõrgemal asuvad ressursipakid võidavad: sama nimega kirje asendab allpool oleva ja väärtusele null seatud kirje eemaldab selle.

<chapter name="Kirjetüübid"/>
Igal kirjel on "type":
- item_stack: leht eseme (või ploki) kohta. "stack" on eseme ID.
- statement: leht värava päästiku või toimingu kohta. "statement" on selle ID, näiteks "buildcraft:redstone.input.active".
- external: leht, mis ei käsitle midagi mängus, nagu see leht. "title" on tõlkevõti pärast eesliidet "buildcraft.guide.page.".

Kirje, mille eset või päästikut/toimingut ei eksisteeri (näiteks seetõttu, et mõni mod pole paigaldatud), jäetakse välja.

<new_page/>
<chapter name="Sisukord"/>
"book" on raamat, kuhu kirje kuulub. Juhendraamatus on "buildcraftreborn:main" (BuildCrafti juhendraamat) ja "buildcraftreborn:meta" (see raamat).

"tag_type" ja "tag_subtype" määravad, kuhu kirje sisukorralehel paigutub. Need tõlgitakse võtmetega "buildcraft.guide.chapter.type.[tag_type]" ja "buildcraft.guide.chapter.subtype.[tag_subtype]".

Raamatud on loetletud samal viisil failis:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Näiteks:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" on tõlkevõti ja "all_entries" määrab, kas raamatu sisukord loetleb ka kõik, mis ei kuulu ühtegi raamatusse.

Kõige uuesti laadimiseks vajuta juhendi avatud olles F3 ja T.

<italic>BuildCraft 8.0 määratles kirjed "guide.txt" failides oma juhiskeeles. BuildCraft Reborn kasutab selle asemel tavalist jsoni.</italic>
