Juhendi lehed on kõik kirjutatud markdowni alamhulgas.

<italic>(Suurem osa sellest lehest on veel täitmata, kuid siin on nimekiri põhimärgenditest)</italic>

# Lihtsad üksikmärgendid
Iga märgend on kujul &lt;name arguments/&gt;, kus name on märgendi nimi ja lõpus olev '/' tähendab, et märgend on omaette. Mõnel märgendil on ka argumendid „võti-väärtus paaridena“, näiteks:
<guide_md>
"level"="42"
</guide_md>

## Uus leht
&lt;new_page/&gt; viib pärast seda tuleva sisu järgmisele lehele.

## Lingid
Need loovad lingi teisele juhendraamatu lehele, kasutades lingitud lehe pealkirja ja ikooni. Teise võimalusena saad luua lingi ItemStackile või Statementile, isegi kui neil pole oma juhendraamatu lehte.
Näiteks:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
annab tulemuseks:
<link to="buildcraftreborn:config/guide_entry_format"/>

või:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
annab tulemuseks:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Peatükid
&lt;chapter/&gt; loob „peatüki pealkirja“, mis kuvatakse peatükkide loendis värvilise pealkirjana. Sellel on üks võimalik argument: "level", mis peab olema täisarv 0 või suurem. Teise võimalusena saad pealkirjade loomiseks kasutada mitut trellimärki (#), täpselt nagu tavalises markdownis. #-märkide arv (miinus üks) võrdub pealkirja tasemega.
Näiteks:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
annab tulemuseks:
### 3. taseme peatükk
<chapter name="Teine 3. taseme peatükk" level="2"/>

# Lihtsad paarismärgendid
Kõik need peavad ümbritsema mingit sisu ning neil peab olema nii algusmärgend &lt;italic&gt; kui ka lõpumärgend &lt;/italic&gt;
Need kõik mõjutavad algus- ja lõpumärgendi vahele jäävat teksti.

- <italic>kaldkiri</italic> (italic)
- <bold>paks kiri</bold> (bold)
- <underline>allajoonitud</underline> (underline)
- <strikethrough>läbikriipsutatud</strikethrough> (strikethrough)
- <red>punane</red> (red)
- <black>must</black> (black)
- <dark_blue>tumesinine</dark_blue> (dark_blue)
- <dark_green>tumeroheline</dark_green> (dark_green)
- <dark_aqua>tume türkiis</dark_aqua> (dark_aqua)
- <dark_red>tumepunane</dark_red> (dark_red)
- <dark_purple>tumelilla</dark_purple> (dark_purple)
- <gold>kuldne</gold> (gold)
- <gray>hall</gray> (gray)
- <dark_gray>tumehall</dark_gray> (dark_gray)
- <blue>sinine</blue> (blue)
- <green>roheline</green> (green)
- <aqua>türkiis</aqua> (aqua)
- <light_purple>helelilla</light_purple> (light_purple)
- <yellow>kollane</yellow> (yellow)
- <white>valge</white> (white)

Rohelist, türkiisi, kollast ega valget pole hea mõte kasutada, sest neid värve on lehel üldiselt väga raske näha.

# Lülitatavad märgendid
Kõik järgmised märgendid on valikulised ja mängija saab need sisse või välja lülitada.

## Taustalugu
Kui kast „Näita taustalugu [ ]“ on märgitud, peidetakse kogu &lt;no_lore&gt; märgendi sisu, vastasel juhul peidetakse kogu &lt;lore&gt; märgendi sisu.

Lore-märgendit tuleks kasutada „mängusisese“ juhendi teabe näitamiseks, jutustatuna mängija vaatenurgast (justkui uuriks ta ise aktiivselt, kuidas kõik töötab). Vastand on mõeldud ametlikuma, Vikipeedia-laadse tooni jaoks, millega teave edastatakse lihtsalt ja selgelt.

See on mängus vaikimisi sisse lülitatud ja eksportides alati välja lülitatud.

## Vihjed
Kui kast „Näita vihjeid [ ]“ on märgitud, peidetakse kogu &lt;no_hint&gt; märgendi sisu, vastasel juhul peidetakse kogu &lt;hint&gt; märgendi sisu.

Hint-märgendit tuleks kasutada lisavihjete näitamiseks selle kohta, milleks kirjeldatud asja kasutada saab, või kuidas seda teiste plokkide või esemetega kombineerides kasutada. Sisuliselt on see eri asjade „soovitatud kasutusviiside“ jaoks. (Kuna nende väljamõtlemine on osa lõbust, pole see kohustuslik.) Seetõttu on see vaikimisi välja lülitatud, kuid mängus sisse lülitatav ja plaanis on teha see lülitatavaks ka eksportimisel.

## Üksikasjad
Kui kliendi seadistusvalik "guideBookEnableDetail" on seatud väärtusele "true", peidetakse kogu &lt;no_detail&gt; märgendi sisu, vastasel juhul peidetakse kogu &lt;detail&gt; märgendi sisu.

Detail-märgendit tuleks kasutada kõigi arvude näitamiseks, mida kasutatakse mitmesuguste asjade arvutamisel, näiteks toru läbilaskevõime, väljatõmbekiiruse, impulsisageduse jne puhul.

See on vaikimisi välja lülitatud, kuid mängus sisse lülitatav ja plaanis on teha see lülitatavaks ka eksportimisel.

# Muu
## Retsept (ainsus)
See märgend näitab antud esemevirna esimest leitud retsepti. Näiteks:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Retseptid
See märgend näitab kõiki antud esemevirna retsepte. Näiteks:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Kasutused
Sarnaselt retseptidele näitab see kõiki antud esemevirna kasutusi. Näiteks:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Retseptid ja kasutused
See ühendab sisuliselt kaks ülaltoodud märgendit üheks, näiteks:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Grupp
See näitab antud esemele, päästikule, toimingule jne sobivaid gruppe.

## Pildid
See näitab pilti. Argumente on 3:
* src: Pildi lähteasukoht.
* width: Laius, millega pilti näidatakse. Vaikimisi lähtepildi laius (see ei pruugi aga olla see, mida soovid)
* height: Kõrgus, millega pilti näidatakse.

Näiteks:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
