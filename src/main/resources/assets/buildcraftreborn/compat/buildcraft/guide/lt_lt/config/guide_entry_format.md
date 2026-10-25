Kiekvienas vadovo puslapis yra „įrašas“: jį sudaro pavadinimas, puslapio tema ir vieta turinyje. Resursų paketai gali įrašus pridėti, pakeisti arba pašalinti.

<chapter name="Įrašų failai"/>
Įrašai surašomi json faile kiekvienoje resursų paketo vardų srityje:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Jame yra vienas objektas, susiejantis kiekvieno įrašo pavadinimą su jo apibrėžtimi. Pavyzdžiui:
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
Pats puslapis yra atskiras failas, pavadintas pagal įrašą:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
Puslapis en_us įkeliamas visada, o puslapis jūsų kalba (jei toks yra) jį pakeičia. Ką rašyti puslapyje, žr. <link to="buildcraftreborn:config/guide_page_format"/>.

Pirmenybę turi aukščiau esantys resursų paketai: tokio paties pavadinimo įrašas pakeičia žemiau esantį, o įrašas, nustatytas į null, jį pašalina.

<chapter name="Įrašų tipai"/>
Kiekvienas įrašas turi "type":
- item_stack: puslapis apie daiktą (ar bloką). "stack" – daikto ID.
- statement: puslapis apie vartų sąlygą ar veiksmą. "statement" – jos ar jo ID, pavyzdžiui, "buildcraft:redstone.input.active".
- external: puslapis ne apie kokį nors žaidimo objektą, kaip šis. "title" – vertimo raktas, einantis po "buildcraft.guide.page.".

Įrašas, kurio daikto, sąlygos ar veiksmo nėra (pavyzdžiui, neįdiegus kurio nors modo), praleidžiamas.

<new_page/>
<chapter name="Turinys"/>
"book" – knyga, kurioje yra įrašas. Vadove yra "buildcraftreborn:main" (BuildCraft vadovas) ir "buildcraftreborn:meta" (ši knyga).

"tag_type" ir "tag_subtype" nurodo, kur įrašas atsiduria turinio puslapyje. Jie verčiami kaip "buildcraft.guide.chapter.type.[tag_type]" ir "buildcraft.guide.chapter.subtype.[tag_subtype]".

Knygos surašomos taip pat, faile:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Pavyzdžiui:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" – vertimo raktas, o "all_entries" nurodo, ar knygos turinyje taip pat išvardijama viskas, kas nepriklauso jokiai knygai.

Kai vadovas atidarytas, paspauskite F3 ir T, kad viskas būtų įkelta iš naujo.

<italic>BuildCraft 8.0 įrašus apibrėždavo "guide.txt" failuose savo instrukcijų kalba. BuildCraft Reborn vietoj to naudoja paprastą json.</italic>
