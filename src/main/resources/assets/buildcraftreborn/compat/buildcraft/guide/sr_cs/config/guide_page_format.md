Sve stranice priručnika napisane su u podskupu markdowna.

<italic>(Veći deo ove stranice tek treba popuniti, ali evo liste osnovnih tagova)</italic>

# Jednostavni pojedinačni tagovi
Svaki tag ima oblik &lt;name arguments/&gt; – gde je name ime taga, a završna '/' označava da tag stoji samostalno. Neki tagovi imaju i argumente u obliku „parova ključ-vrednost“, na primer:
<guide_md>
"level"="42"
</guide_md>

## Nova stranica
&lt;new_page/&gt; premešta sadržaj koji sledi na sledeću stranicu.

## Veze
Ovi tagovi prave vezu ka drugoj stranici priručnika, koristeći naslov i ikonu te stranice. Možete napraviti i vezu ka ItemStack-u ili Statement-u, čak i ako za njih ne postoji stranica u priručniku.
Na primer:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
daje:
<link to="buildcraftreborn:config/guide_entry_format"/>

ili:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
daje:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Poglavlja
&lt;chapter/&gt; pravi „naslov poglavlja“, koji se u listi poglavlja prikazuje kao obojeni naslov. Ima jedan mogući argument: "level", koji mora biti ceo broj, 0 ili veći. Naslove možete da napravite i pomoću više znakova taraba (#), baš kao u običnom markdown formatu. Broj znakova # (umanjen za jedan) jednak je nivou naslova.
Na primer:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
daje:
### Poglavlje nivoa 3
<chapter name="Još jedno poglavlje nivoa 3" level="2"/>

# Jednostavni dvostruki tagovi
Svi ovi tagovi moraju da obuhvataju neki sadržaj i moraju da imaju i početni tag &lt;italic&gt; i završni tag &lt;/italic&gt;
Svi oni utiču na tekst između početnog i završnog taga.

- <italic>kurziv</italic>
- <bold>podebljano</bold>
- <underline>podvučeno</underline>
- <strikethrough>precrtano</strikethrough>
- <red>crvena</red>
- <black>crna</black>
- <dark_blue>tamnoplava</dark_blue>
- <dark_green>tamnozelena</dark_green>
- <dark_aqua>tamnotirkizna</dark_aqua>
- <dark_red>tamnocrvena</dark_red>
- <dark_purple>tamnoljubičasta</dark_purple>
- <gold>zlatna</gold>
- <gray>siva</gray>
- <dark_gray>tamnosiva</dark_gray>
- <blue>plava</blue>
- <green>zelena</green> (zelena)
- <aqua>tirkizna</aqua> (tirkizna)
- <light_purple>svetloljubičasta</light_purple>
- <yellow>žuta</yellow> (žuta)
- <white>bela</white> (bela)

Nije dobra ideja koristiti zelenu, tirkiznu, žutu ili belu, jer se te boje na stranici obično veoma teško vide.

# Uključivi tagovi
Sve sledeće tagove igrač može da uključi ili isključi u podešavanjima.

## Priča
Ako je polje „Prikaži priču [ ]“ označeno, sve unutar taga &lt;no_lore&gt; biće sakriveno; u suprotnom će biti sakriveno sve unutar taga &lt;lore&gt;.

Tag lore treba koristiti za informacije u priručniku ispričane „iz igre“, iz perspektive igrača (kao da on sam aktivno istražuje kako sve funkcioniše). Suprotni tag služi za formalniji ton, nalik Vikipediji, kojim se informacije prenose jasno i jednostavno.

U igri je ovo podrazumevano uključeno, a u izvozima je uvek isključeno.

## Saveti
Ako je polje „Prikaži savete [ ]“ označeno, sve unutar taga &lt;no_hint&gt; biće sakriveno; u suprotnom će biti sakriveno sve unutar taga &lt;hint&gt;.

Tag hint treba koristiti za dodatne „savete“ o tome za šta opisana stvar može da se koristi ili kako može da se kombinuje sa drugim blokovima i predmetima. U suštini, ovo služi za „preporučene upotrebe“ raznih stvari. (Pošto je otkrivanje toga deo zabave, nije obavezno. Zato je ovo podrazumevano isključeno, ali može da se uključi u igri, a planirano je da može da se uključi i pri izvozu.

## Detalji
Ako je klijentska opcija konfiguracije "guideBookEnableDetail" postavljena na "true", sve unutar taga &lt;no_detail&gt; biće sakriveno; u suprotnom će biti sakriveno sve unutar taga &lt;detail&gt;.

Tag detail treba koristiti za prikaz svih brojeva koji se koriste pri izračunavanju raznih stvari, kao što su protok cevi, brzina izvlačenja, učestalost impulsa itd.

Ovo je podrazumevano isključeno, ali može da se uključi u igri, a planirano je da može da se uključi i pri izvozu.

# Razno
## Recept (jednina)
Ovaj tag prikazuje prvi pronađeni recept za dati stek predmeta. Na primer:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Recepti
Ovaj tag prikazuje sve recepte za dati stek predmeta. Na primer:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Upotrebe
Slično receptima, ovaj tag prikazuje sve upotrebe datog steka predmeta. Na primer:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Recepti i upotrebe
Ovaj tag u suštini spaja dva gornja taga u jedan, na primer:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Grupa
Ovaj tag prikazuje grupe koje odgovaraju datom predmetu, izrazu (statement) itd.

## Slike
Ovaj tag prikazuje sliku. Ima 3 argumenta:
* src: lokacija izvorne slike.
* width: širina sa kojom će slika biti prikazana. Podrazumevano je to širina izvorne slike (što možda nije ono što želite)
* height: visina sa kojom će slika biti prikazana.

Na primer:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
