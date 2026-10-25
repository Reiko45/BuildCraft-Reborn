A kézikönyv minden oldala egy „bejegyzés”: egy név, az oldal témája, és hogy hová kerül a tartalomjegyzékben. A forráscsomagok bejegyzéseket adhatnak hozzá, cserélhetnek le vagy távolíthatnak el.

<chapter name="Bejegyzésfájlok"/>
A bejegyzéseket egy json-fájl sorolja fel a forráscsomag minden névterében:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Egyetlen objektumot tartalmaz, amely minden bejegyzés nevéhez hozzárendeli a definícióját. Például:
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
Maga az oldal egy külön fájl, amely a bejegyzésről kapta a nevét:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
Az en_us oldal mindig betöltődik, a saját nyelveden írt oldal pedig (ha van ilyen) lecseréli. Hogy mi kerül egy oldalra, azt itt találod: <link to="buildcraftreborn:config/guide_page_format"/>.

A magasabban lévő forráscsomagok élveznek elsőbbséget: az azonos nevű bejegyzés lecseréli az alatta lévőt, a null értékre állított bejegyzés pedig eltávolítja.

<chapter name="Bejegyzéstípusok"/>
Minden bejegyzésnek van egy "type" mezője:
- item_stack: egy tárgyról (vagy blokkról) szóló oldal. A "stack" a tárgy azonosítója.
- statement: egy kaputriggerről vagy kapucselekvésről szóló oldal. A "statement" ennek az azonosítója, például "buildcraft:redstone.input.active".
- external: olyan oldal, amely nem a játék valamelyik eleméről szól, mint ez is. A "title" egy fordítási kulcs a "buildcraft.guide.page." előtag nélkül.

Az a bejegyzés, amelynek a tárgya vagy statementje nem létezik (például mert egy mod nincs telepítve), kimarad.

<new_page/>
<chapter name="Tartalom"/>
A "book" az a könyv, amelyben a bejegyzés szerepel. A kézikönyvben van "buildcraftreborn:main" (a BuildCraft kézikönyv) és "buildcraftreborn:meta" (ez a könyv).

A "tag_type" és a "tag_subtype" adja meg, hová kerül a bejegyzés a tartalomjegyzékben. A nevüket a "buildcraft.guide.chapter.type.[tag_type]", illetve a "buildcraft.guide.chapter.subtype.[tag_subtype]" fordítási kulcs adja.

A könyveket ugyanígy kell felsorolni, ebben a fájlban:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Például:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
A "title" egy fordítási kulcs, az "all_entries" pedig azt adja meg, hogy a könyv tartalomjegyzéke felsorolja-e azokat a bejegyzéseket is, amelyek egyetlen könyvben sincsenek benne.

Nyomd meg az F3 és a T billentyűt, amíg a kézikönyv nyitva van, hogy mindent újratölts.

<italic>A BuildCraft 8.0 a bejegyzéseket "guide.txt" fájlokban, saját utasításnyelvén határozta meg. A BuildCraft Reborn ehelyett sima jsont használ.</italic>
