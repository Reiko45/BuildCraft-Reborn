Jokainen oppaan sivu on ”merkintä”: nimi, sivun aihe ja sen paikka sisällysluettelossa. Resurssipaketit voivat lisätä merkintöjä, korvata niitä tai poistaa niitä.

<chapter name="Merkintätiedostot"/>
Merkinnät luetellaan json-tiedostossa resurssipaketin jokaisessa nimiavaruudessa:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Se sisältää yhden objektin, joka liittää jokaisen merkinnän nimen sen määritelmään. Esimerkiksi:
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
Itse sivu on erillinen tiedosto, joka nimetään merkinnän mukaan:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
en_us-sivu ladataan aina, ja omankielinen sivusi (jos sellainen on) korvaa sen. Sivun sisällöstä kerrotaan sivulla <link to="buildcraftreborn:config/guide_page_format"/>.

Ylempänä olevat resurssipaketit voittavat: samanniminen merkintä korvaa alemman, ja arvoksi null asetettu merkintä poistaa sen.

<chapter name="Merkintätyypit"/>
Jokaisella merkinnällä on "type":
- item_stack: sivu tavarasta (tai palikasta). "stack" on tavaran tunniste.
- statement: sivu portin laukaisimesta tai toiminnosta. "statement" on sen tunniste, kuten "buildcraft:redstone.input.active".
- external: sivu, joka ei käsittele mitään pelin sisäistä, kuten tämä sivu. "title" on käännösavain "buildcraft.guide.page."-etuliitteen jälkeen.

Merkintä, jonka tavaraa tai laukaisinta/toimintoa ei ole olemassa (esimerkiksi koska jotain modia ei ole asennettu), jätetään pois.

<new_page/>
<chapter name="Sisältö"/>
"book" on kirja, johon merkintä kuuluu. Opaskirjassa on "buildcraftreborn:main" (BuildCraftin opaskirja) ja "buildcraftreborn:meta" (tämä kirja).

"tag_type" ja "tag_subtype" määräävät, mihin merkintä sijoittuu sisällysluettelosivulla. Ne käännetään avaimilla "buildcraft.guide.chapter.type.[tag_type]" ja "buildcraft.guide.chapter.subtype.[tag_subtype]".

Kirjat luetellaan samalla tavalla tiedostossa:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Esimerkiksi:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" on käännösavain, ja "all_entries" määrää, luetteleeko kirjan sisällysluettelo myös kaiken, mikä ei kuulu mihinkään kirjaan.

Paina F3 ja T oppaan ollessa auki, niin kaikki ladataan uudelleen.

<italic>BuildCraft 8.0 määritteli merkinnät "guide.txt"-tiedostoissa omalla ohjekielellään. BuildCraft Reborn käyttää sen sijaan tavallista jsonia.</italic>
