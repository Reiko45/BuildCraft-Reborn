Visi vadovo puslapiai rašomi markdown poaibiu.

<italic>(Didžiąją šio puslapio dalį dar reikia užpildyti, tačiau štai pagrindinių žymių sąrašas)</italic>

# Paprastos pavienės žymės
Kiekviena žymė yra pavidalo &lt;name arguments/&gt; – čia name yra žymės pavadinimas, o galinis '/' reiškia, kad žymė yra savarankiška. Kai kurios žymės turi ir argumentų „rakto ir reikšmės porų“ pavidalu, pavyzdžiui:
<guide_md>
"level"="42"
</guide_md>

## Naujas puslapis
&lt;new_page/&gt; perkelia po jo esantį turinį į kitą puslapį.

## Nuorodos
Šios žymės sukuria nuorodą į kitą vadovo puslapį, naudodamos to puslapio pavadinimą ir piktogramą. Taip pat galite sukurti nuorodą į ItemStack ar Statement, net jei jiems nėra vadovo puslapio.
Pavyzdžiui:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
rezultatas:
<link to="buildcraftreborn:config/guide_entry_format"/>

arba:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
rezultatas:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Skyriai
&lt;chapter/&gt; sukuria „skyriaus antraštę“, kuri skyrių sąraše rodoma kaip spalvota antraštė. Ji turi vieną galimą argumentą – "level", kuris turi būti sveikasis skaičius, ne mažesnis už 0. Antraštes taip pat galite kurti keliais grotelių ženklais (#), kaip įprastame markdown formate. Ženklų # skaičius (atėmus vieną) lygus antraštės lygiui.
Pavyzdžiui:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
rezultatas:
### 3 lygio skyrius
<chapter name="Dar vienas 3 lygio skyrius" level="2"/>

# Paprastos porinės žymės
Visos šios žymės turi apgaubti kokį nors turinį, todėl turi turėti ir pradžios žymę &lt;italic&gt;, ir pabaigos žymę &lt;/italic&gt;
Visos jos veikia tekstą tarp pradžios ir pabaigos žymių.

- <italic>kursyvas</italic>
- <bold>paryškinta</bold>
- <underline>pabraukta</underline>
- <strikethrough>perbraukta</strikethrough>
- <red>raudona</red>
- <black>juoda</black>
- <dark_blue>tamsiai mėlyna</dark_blue>
- <dark_green>tamsiai žalia</dark_green>
- <dark_aqua>tamsiai žydra</dark_aqua>
- <dark_red>tamsiai raudona</dark_red>
- <dark_purple>tamsiai violetinė</dark_purple>
- <gold>auksinė</gold>
- <gray>pilka</gray>
- <dark_gray>tamsiai pilka</dark_gray>
- <blue>mėlyna</blue>
- <green>žalia</green> (žalia)
- <aqua>žydra</aqua> (žydra)
- <light_purple>šviesiai violetinė</light_purple>
- <yellow>geltona</yellow> (geltona)
- <white>balta</white> (balta)

Nepatartina naudoti žalios, žydros, geltonos ar baltos spalvos, nes šias spalvas puslapyje paprastai labai sunku įžiūrėti.

# Perjungiamos žymės
Visos toliau nurodytos žymės priklauso nuo nustatymų, ir žaidėjas gali jas įjungti arba išjungti.

## Pasakojimas
Jei pažymėtas langelis „Rodyti istoriją [ ]“, viskas, kas yra žymėje &lt;no_lore&gt;, paslepiama, o priešingu atveju paslepiama viskas, kas yra žymėje &lt;lore&gt;.

Žymė lore skirta „žaidimo viduje“ pateikiamai vadovo informacijai, pasakojamai žaidėjo akimis (lyg jis pats aktyviai tyrinėtų, kaip viskas veikia). Priešinga žymė skirta formalesniam, Vikipediją primenančiam tonui, kuriuo informacija perteikiama paprastai ir aiškiai.

Žaidime tai įjungta pagal numatymą, o eksportuojant visada išjungta.

## Patarimai
Jei pažymėtas langelis „Rodyti patarimus [ ]“, viskas, kas yra žymėje &lt;no_hint&gt;, paslepiama, o priešingu atveju paslepiama viskas, kas yra žymėje &lt;hint&gt;.

Žymė hint skirta papildomiems „patarimams“, kam galima panaudoti aprašomą dalyką arba kaip jį derinti su kitais blokais ar daiktais. Iš esmės tai skirta įvairių dalykų „rekomenduojamiems panaudojimams“. (Kadangi tai išsiaiškinti – dalis malonumo, tai nėra privaloma. Todėl pagal numatymą tai išjungta, tačiau žaidime galima įjungti, o ateityje planuojama leisti įjungti ir eksportuojant.

## Išsamumas
Jei kliento konfigūracijos parinktis "guideBookEnableDetail" nustatyta į "true", viskas, kas yra žymėje &lt;no_detail&gt;, paslepiama, o priešingu atveju paslepiama viskas, kas yra žymėje &lt;detail&gt;.

Žymė detail skirta parodyti visus skaičius, naudojamus įvairiems dydžiams apskaičiuoti, pavyzdžiui, vamzdžio pralaidumą, ištraukimo spartą, impulsų dažnį ir pan.

Pagal numatymą tai išjungta, tačiau žaidime galima įjungti, o ateityje planuojama leisti įjungti ir eksportuojant.

# Kita
## Receptas (vienaskaita)
Ši žymė rodo pirmą rastą nurodytos daiktų krūvelės receptą. Pavyzdžiui:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Receptai
Ši žymė rodo visus nurodytos daiktų krūvelės receptus. Pavyzdžiui:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Panaudojimai
Panašiai kaip receptų žymė, ši žymė rodo visus nurodytos daiktų krūvelės panaudojimus. Pavyzdžiui:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Receptai ir panaudojimai
Ši žymė iš esmės sujungia abi aukščiau aprašytas žymes į vieną, pavyzdžiui:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Grupė
Ši žymė rodo grupes, tinkančias nurodytam daiktui, teiginiui (statement) ir pan.

## Paveikslėliai
Ši žymė rodo paveikslėlį. Ji turi 3 argumentus:
* src: paveikslėlio šaltinio vieta.
* width: plotis, kuriuo bus rodomas paveikslėlis. Pagal numatymą – šaltinio paveikslėlio plotis (tačiau tai gali būti ne tai, ko norite)
* height: aukštis, kuriuo bus rodomas paveikslėlis.

Pavyzdžiui:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
