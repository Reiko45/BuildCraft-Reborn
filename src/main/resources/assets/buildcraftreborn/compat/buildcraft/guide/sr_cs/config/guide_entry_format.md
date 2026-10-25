Svaka stranica priručnika je „unos“: ima ime, temu i mesto u sadržaju. Paketi resursa mogu da dodaju, zamene ili uklone unose.

<chapter name="Datoteke unosa"/>
Unosi su navedeni u json datoteci u svakom imenskom prostoru paketa resursa:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Ona sadrži jedan objekat koji ime svakog unosa povezuje s njegovom definicijom. Na primer:
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
Sama stranica je zasebna datoteka, nazvana po unosu:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
Stranica en_us se uvek učitava, a stranica na vašem jeziku (ako postoji) je zamenjuje. Šta ide u stranicu, pogledajte u <link to="buildcraftreborn:config/guide_page_format"/>.

Prednost imaju paketi resursa koji su više na listi: unos sa istim imenom zamenjuje onaj ispod sebe, a unos postavljen na null ga uklanja.

<chapter name="Vrste unosa"/>
Svaki unos ima "type":
- item_stack: stranica o predmetu (ili bloku). "stack" je ID predmeta.
- statement: stranica o okidaču ili akciji logičke kapije. "statement" je njegov ID, na primer "buildcraft:redstone.input.active".
- external: stranica koja nije ni o čemu iz igre, kao što je ova. "title" je ključ prevoda, koji se nadovezuje na "buildcraft.guide.page.".

Unos čiji predmet, okidač ili akcija ne postoji (na primer zato što neki mod nije instaliran) se izostavlja.

<new_page/>
<chapter name="Sadržaj"/>
"book" je knjiga u kojoj se unos nalazi. Priručnik ima "buildcraftreborn:main" (BuildCraft priručnik) i "buildcraftreborn:meta" (ovu knjigu).

"tag_type" i "tag_subtype" određuju gde se unos nalazi na stranici sa sadržajem. Prevode se kao "buildcraft.guide.chapter.type.[tag_type]" i "buildcraft.guide.chapter.subtype.[tag_subtype]".

Knjige se navode na isti način, u datoteci:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Na primer:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" je ključ prevoda, a "all_entries" određuje da li sadržaj knjige navodi i sve ono što nije ni u jednoj knjizi.

Pritisnite F3 i T dok je priručnik otvoren da biste sve ponovo učitali.

<italic>BuildCraft 8.0 je unose definisao u datotekama "guide.txt", na sopstvenom jeziku instrukcija. BuildCraft Reborn umesto toga koristi običan json.</italic>
