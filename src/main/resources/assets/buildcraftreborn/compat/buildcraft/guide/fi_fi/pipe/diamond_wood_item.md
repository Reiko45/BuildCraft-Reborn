<lore>
Haluatko ottaa tietyt tavarat talteen ja siirtää ne muualle? Anna putkelle vain energiaa ja katso, kun ne virtaavat.
</lore>
<no_lore>
Puutimanttisella kuljetusputkella poimitaan suodattimen määräämiä tavaroita varastosta, kun putki saa energiaa.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="Putken toiminta"/>
Kun putki saa energiaa, se poimii suodattimessa valitut tavarat ja kuljettaa ne muihin liitettyihin varastoihin tai putkiin.
Tavarat poimitaan putken umpinaiselta sivulta ja kuljetetaan kohti kirkkaita, suunnattuja sivuja.
Jos kirkkaita sivuja on useita, tavarat liikkuvat satunnaiseen suuntaan.
Jos tavaralla ei ole minne mennä, poimitut tavarat putoavat putkesta maahan.

Jotkin koneet voivat syöttää tavaroita automaattisesti suoraan Puutimanttiseen kuljetusputkeen ilman, että putki tarvitsee energiaa (tällöin suodatinta ei noudateta).
Putken tulpilla tai maalaamalla putket eri värisiksi voit estää putkia liittymästä toisiinsa.

<chapter name="Suodatus"/>
Avaamalla Puutimanttisen kuljetusputken käyttöliittymän voit asettaa suodattimen.
Asettamalla haluamasi tavarat paikkoihin voit suodattaa, mitä putki poimii.
Voit muuttaa suodattimen toimintaa valinnoilla Sallittujen lista, Kieltolista ja Vuorottelu:
Sallittujen lista: vain valitut tavarat poimitaan.
Kieltolista: kaikki muut paitsi valitut tavarat poimitaan.
Vuorottelu: poimii tavarat täsmälleen siinä järjestyksessä, jossa ne on valittu yllä oleviin suodatinpaikkoihin (vasemmalta oikealle)

Suodatuspaikoissa voi käyttää Listaa, joka laajentaa suodatusvaihtoehtoja.
<link to="buildcraftreborn:item/list"/>

<chapter name="Energian syöttö"/>
Se voi saada energiaa mistä tahansa MJ-energialähteestä, kuten moottorista, Energiasovittimesta tai Putkipulssittajasta.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
Jokaisella saamallaan energiapulssilla se poimii tavaroita viereisestä varastosta.
Mitä enemmän energiaa pulssissa on, sitä enemmän tavaroita poimitaan pulssia kohden.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>