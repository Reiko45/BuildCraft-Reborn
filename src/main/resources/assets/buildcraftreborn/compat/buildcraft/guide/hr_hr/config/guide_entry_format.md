Svaka stranica priručnika jest „unos“: ima naziv, temu stranice i mjesto u sadržaju. Paketi resursa mogu dodavati, zamjenjivati ili uklanjati unose.

<chapter name="Datoteke unosa"/>
Unosi su navedeni u json datoteci u svakom imenskom prostoru paketa resursa:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Sadrži jedan objekt koji svakom nazivu unosa pridružuje njegovu definiciju. Na primjer:
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
Sama stranica zasebna je datoteka nazvana po unosu:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
Stranica en_us uvijek se učitava, a stranica na vašem jeziku (ako postoji) zamjenjuje je. Što sve može sadržavati stranica, pogledajte ovdje: <link to="buildcraftreborn:config/guide_page_format"/>.

Prednost imaju viši paketi resursa: unos s istim nazivom zamjenjuje onaj ispod sebe, a unos postavljen na null uklanja ga.

<chapter name="Vrste unosa"/>
Svaki unos ima polje "type":
- item_stack: stranica o predmetu (ili bloku). "stack" je ID predmeta.
- statement: stranica o okidaču ili radnji logičkog sklopa. "statement" je njihov ID, primjerice "buildcraft:redstone.input.active".
- external: stranica koja nije ni o čemu u igri, poput ove. "title" je ključ prijevoda nakon "buildcraft.guide.page.".

Unos čiji predmet ili statement ne postoji (na primjer zato što mod nije instaliran) izostavlja se.

<new_page/>
<chapter name="Sadržaj"/>
"book" je knjiga u kojoj se unos nalazi. Priručnik ima knjige "buildcraftreborn:main" (BuildCraft priručnik) i "buildcraftreborn:meta" (ova knjiga).

"tag_type" i "tag_subtype" određuju gdje unos ide na stranici sadržaja. Prevode se kao "buildcraft.guide.chapter.type.[tag_type]" i "buildcraft.guide.chapter.subtype.[tag_subtype]".

Knjige se navode na isti način, u datoteci:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Na primjer:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" je ključ prijevoda, a "all_entries" određuje navodi li sadržaj knjige i sve ono što nije ni u jednoj knjizi.

Pritisnite F3 i T dok je priručnik otvoren kako biste sve ponovno učitali.

<italic>BuildCraft 8.0 definirao je unose u datotekama "guide.txt", na vlastitom jeziku uputa. BuildCraft Reborn umjesto toga koristi običan json.</italic>
