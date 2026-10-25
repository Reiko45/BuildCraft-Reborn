Oppaan sivut kirjoitetaan kaikki markdownin osajoukolla.

<italic>(Suurin osa tästä sivusta on vielä täydentämättä, mutta tässä on luettelo perustageista)</italic>

# Yksinkertaiset yksittäiset tagit
Jokainen tagi on muotoa &lt;name arguments/&gt;, missä name on tagin nimi ja lopun '/' tarkoittaa, että tagi on yksinään. Joillakin tageilla on myös argumentteja ”avain–arvo-pareina”, esimerkiksi:
<guide_md>
"level"="42"
</guide_md>

## Uusi sivu
&lt;new_page/&gt; siirtää sen jälkeen tulevan sisällön seuraavalle sivulle.

## Linkit
Nämä luovat linkin toiselle opaskirjan sivulle käyttäen linkitetyn sivun otsikkoa ja kuvaketta. Vaihtoehtoisesti voit luoda linkin ItemStackiin tai Statementiin, vaikka niillä ei olisi omaa opaskirjan sivua.
Esimerkiksi:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
tuloksena on:
<link to="buildcraftreborn:config/guide_entry_format"/>

tai:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
tuloksena on:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Luvut
&lt;chapter/&gt; luo ”luvun otsikon”, joka näytetään lukuluettelossa värillisenä otsikkona. Sillä on yksi mahdollinen argumentti, "level", jonka on oltava kokonaisluku 0 tai suurempi. Vaihtoehtoisesti voit luoda otsikoita usealla risuaitamerkillä (#) aivan kuten tavallisessa markdownissa. #-merkkien määrä miinus yksi on otsikon taso.
Esimerkiksi:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
tuloksena on:
### Tason 3 luku
<chapter name="Toinen tason 3 luku" level="2"/>

# Yksinkertaiset tagiparit
Kaikkien näiden on ympäröitävä jotain sisältöä, ja niillä on oltava sekä aloitustagi &lt;italic&gt; että lopetustagi &lt;/italic&gt;
Ne kaikki vaikuttavat aloitus- ja lopetustagin väliseen tekstiin.

- <italic>kursiivi</italic> (italic)
- <bold>lihavoitu</bold> (bold)
- <underline>alleviivattu</underline> (underline)
- <strikethrough>yliviivattu</strikethrough> (strikethrough)
- <red>punainen</red> (red)
- <black>musta</black> (black)
- <dark_blue>tummansininen</dark_blue> (dark_blue)
- <dark_green>tummanvihreä</dark_green> (dark_green)
- <dark_aqua>tumma turkoosi</dark_aqua> (dark_aqua)
- <dark_red>tummanpunainen</dark_red> (dark_red)
- <dark_purple>tummanvioletti</dark_purple> (dark_purple)
- <gold>kulta</gold> (gold)
- <gray>harmaa</gray> (gray)
- <dark_gray>tummanharmaa</dark_gray> (dark_gray)
- <blue>sininen</blue> (blue)
- <green>vihreä</green> (green)
- <aqua>turkoosi</aqua> (aqua)
- <light_purple>vaaleanvioletti</light_purple> (light_purple)
- <yellow>keltainen</yellow> (yellow)
- <white>valkoinen</white> (white)

Vihreää, turkoosia, keltaista tai valkoista ei kannata käyttää, koska niitä on yleensä hyvin vaikea erottaa sivulta.

# Kytkettävät tagit
Kaikki seuraavat tagit ovat valinnaisia, ja pelaaja voi ottaa ne käyttöön tai poistaa ne käytöstä.

## Tarinateksti
Jos ”Näytä taustatarina [ ]” -ruutu on valittuna, kaikki &lt;no_lore&gt;-tagin sisältö piilotetaan, muuten kaikki &lt;lore&gt;-tagin sisältö piilotetaan.

Lore-tagia käytetään näyttämään ”pelinsisäistä” oppaan tietoa pelaajan näkökulmasta kerrottuna (ikään kuin pelaaja tutkisi itse, miten kaikki toimii). Sen vastakohta on muodollisempi, Wikipedian kaltainen tyyli, jolla tieto välitetään selkeästi ja yksinkertaisesti.

Tämä on pelissä oletuksena käytössä ja vienneissä aina pois käytöstä.

## Vihjeet
Jos ”Näytä vihjeet [ ]” -ruutu on valittuna, kaikki &lt;no_hint&gt;-tagin sisältö piilotetaan, muuten kaikki &lt;hint&gt;-tagin sisältö piilotetaan.

Hint-tagia käytetään näyttämään lisävihjeitä siitä, mihin kuvattua asiaa voi käyttää, tai eri tavoista käyttää sitä yhdessä muiden palikoiden tai tavaroiden kanssa. Pohjimmiltaan se on tarkoitettu eri asioiden ”suositelluille käyttötavoille”. (Koska näiden selvittäminen on osa hauskuutta, se ei ole pakollista.) Siksi tämä on oletuksena pois käytöstä, mutta sen voi kytkeä päälle pelissä, ja kytkentä on tarkoitus tuoda myös vienteihin.

## Yksityiskohdat
Jos asiakasohjelman määritysasetus "guideBookEnableDetail" on arvossa "true", kaikki &lt;no_detail&gt;-tagin sisältö piilotetaan, muuten kaikki &lt;detail&gt;-tagin sisältö piilotetaan.

Detail-tagia käytetään näyttämään kaikki luvut, joita käytetään erilaisissa laskelmissa, kuten putken virtausnopeudessa, poimintanopeudessa, pulssitaajuudessa jne.

Tämä on oletuksena pois käytöstä, mutta sen voi kytkeä päälle pelissä, ja kytkentä on tarkoitus tuoda myös vienteihin.

# Muut
## Resepti (yksikkö)
Tämä tagi näyttää ensimmäisen annetulle tavarapinolle löytyvän reseptin. Esimerkiksi:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Reseptit
Tämä tagi näyttää kaikki annetun tavarapinon reseptit. Esimerkiksi:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Käytöt
Reseptien tapaan tämä näyttää annetun tavarapinon kaikki käytöt. Esimerkiksi:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Reseptit ja käytöt
Tämä yhdistää käytännössä kaksi yllä olevaa tagia yhdeksi, esimerkiksi:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Ryhmä
Tämä näyttää ryhmät, jotka sopivat annetulle tavaralle, laukaisimelle tai toiminnolle jne.

## Kuvat
Tämä näyttää kuvan. Argumentteja on 3:
* src: Kuvan lähdesijainti.
* width: Leveys, jolla kuva näytetään. Oletuksena lähdekuvan leveys (tämä ei kuitenkaan välttämättä ole sitä, mitä haluat)
* height: Korkeus, jolla kuva näytetään.

Esimerkiksi:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
