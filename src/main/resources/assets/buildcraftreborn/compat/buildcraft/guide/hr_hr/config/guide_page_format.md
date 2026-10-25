Sve stranice priručnika napisane su u podskupu jezika markdown.

<italic>(Veći dio ove stranice tek treba dopuniti, no evo popisa osnovnih tagova)</italic>

# Jednostavni samostalni tagovi
Svaki tag ima oblik &lt;name arguments/&gt; – pri čemu je name naziv taga, a završni '/' znači da tag stoji samostalno. Neki tagovi imaju i argumente u obliku „parova ključ-vrijednost“, na primjer:
<guide_md>
"level"="42"
</guide_md>

## Nova stranica
&lt;new_page/&gt; premješta sadržaj koji slijedi iza njega na sljedeću stranicu.

## Poveznice
Ovi tagovi stvaraju poveznicu na drugu stranicu priručnika, s naslovom i ikonom te stranice. Možete napraviti i poveznicu na ItemStack ili Statement čak i ako nemaju vlastitu stranicu u priručniku.
Na primjer:
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
&lt;chapter/&gt; stvara „naslov poglavlja“ koji se u popisu poglavlja prikazuje kao obojeni naslov. Ima jedan mogući argument: "level", koji mora biti cijeli broj 0 ili veći. Naslove možete stvarati i pomoću više znakova ljestava (#), baš kao u običnom markdownu. Broj znakova # (umanjen za jedan) jednak je razini naslova.
Na primjer:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
daje:
### Poglavlje razine 3
<chapter name="Još jedno poglavlje razine 3" level="2"/>

# Jednostavni parni tagovi
Svi ovi tagovi moraju obuhvaćati neki sadržaj te moraju imati i početni tag &lt;italic&gt; i završni tag &lt;/italic&gt;
Svi utječu na tekst između početnog i završnog taga.

- <italic>kurziv</italic>
- <bold>podebljano</bold>
- <underline>podcrtano</underline>
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
- <light_purple>svijetloljubičasta</light_purple>
- <yellow>žuta</yellow> (žuta)
- <white>bijela</white> (bijela)

Nije dobro koristiti zelenu, tirkiznu, žutu ili bijelu jer se te boje na stranici obično vrlo teško vide.

# Tagovi s prekidačem
Svi sljedeći tagovi ovise o postavkama i igrač ih može uključiti ili isključiti.

## Priča
Ako je označen okvir „Prikaži priču [ ]“, sve u tagu &lt;no_lore&gt; bit će skriveno, a u suprotnom će biti skriveno sve u tagu &lt;lore&gt;.

Tag lore služi za prikaz informacija „unutar igre“, ispričanih iz perspektive igrača (kao da upravo istražuje kako sve funkcionira). Njegova suprotnost služi za formalniji ton nalik Wikipediji, kojim se informacije prenose jasno i jednostavno.

U igri je ova mogućnost zadano uključena, a pri izvozu uvijek isključena.

## Savjeti
Ako je označen okvir „Prikaži savjete [ ]“, sve u tagu &lt;no_hint&gt; bit će skriveno, a u suprotnom će biti skriveno sve u tagu &lt;hint&gt;.

Tag hint služi za dodatne „savjete“ o tome za što se opisana stvar može koristiti ili kako je kombinirati s drugim blokovima i predmetima. U biti je riječ o „preporučenim uporabama“ različitih stvari. (Budući da je otkrivanje toga dio zabave, nije obavezno.) Zato je ova mogućnost zadano isključena, ali se u igri može uključiti, a planira se i mogućnost uključivanja pri izvozu.

## Pojedinosti
Ako je klijentska konfiguracijska opcija "guideBookEnableDetail" postavljena na "true", sve u tagu &lt;no_detail&gt; bit će skriveno, a u suprotnom će biti skriveno sve u tagu &lt;detail&gt;.

Tag detail služi za prikaz svih brojeva koji se koriste pri izračunu raznih vrijednosti, poput protoka cijevi, brzine izvlačenja, učestalosti pulseva i sl.

Ova je mogućnost zadano isključena, ali se u igri može uključiti, a planira se i mogućnost uključivanja pri izvozu.

# Razno
## Recept (jednina)
Ovaj tag prikazuje prvi pronađeni recept za zadani snop predmeta. Na primjer:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Recepti
Ovaj tag prikazuje sve recepte za zadani snop predmeta. Na primjer:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Uporabe
Slično receptima, ovaj tag prikazuje sve uporabe zadanog snopa predmeta. Na primjer:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Recepti i uporabe
Ovaj tag zapravo spaja dva prethodna taga u jedan, na primjer:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Grupa
Prikazuje grupe koje odgovaraju zadanom predmetu, statementu i sl.

## Slike
Prikazuje sliku. Ima 3 argumenta:
* src: Lokacija izvorne slike.
* width: Širina s kojom će se slika prikazati. Zadana je širina izvorne slike (što možda nije ono što želite).
* height: Visina s kojom će se slika prikazati.

Na primjer:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
