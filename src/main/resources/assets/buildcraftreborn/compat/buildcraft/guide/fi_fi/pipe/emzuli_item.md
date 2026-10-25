<lore>
Tiettyjen tavaroiden poimiminen portin avulla on suodatusteknologian uusin vallankumous.
</lore>
<no_lore>
Emzulinen kuljetusputki on portilla ohjattava putki, jolla poimitaan suodattimen määräämiä tavaroita varastosta, kun putki saa energiaa.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="Putken toiminta"/>
Kun putki saa energiaa, se poimii suodattimessa valitut tavarat ja kuljettaa ne muihin liitettyihin varastoihin tai putkiin.
Tavarat poimitaan putken umpinaiselta sivulta ja kuljetetaan kohti kirkkaita, suunnattuja sivuja.
Jos kirkkaita sivuja on useita, tavarat liikkuvat satunnaiseen suuntaan.
Jos tavaralla ei ole minne mennä, poimitut tavarat putoavat putkesta maahan.

Jotkin koneet voivat syöttää tavaroita automaattisesti suoraan Emzuliseen kuljetusputkeen ilman, että putki tarvitsee energiaa (tällöin suodattimia ei noudateta).
Putken tulpilla tai maalaamalla putket eri värisiksi voit estää putkia liittymästä toisiinsa.

<chapter name="Suodatus"/>
Emzulisen kuljetusputken käyttöliittymässä on neljä väriä neljälle erilliselle suodatusvaihtoehdolle.
Asettamalla tavaroita paikkoihin voit valita, mitä kukin suodatin poimii.
Paikkojen vieressä olevien siveltimien värit määräävät, minkä värisiksi tavarat väritetään poimittaessa, ja niitä voi vaihtaa napsauttamalla.
Neljästä suodattimesta käytettävän valitsemiseen tarvitaan putkeen sijoitettu portti, jossa on värillinen poimintaesiasetus-toiminto. Esim. Punakivisignaali → Punainen poimintaesiasetus.

Suodatuspaikoissa voi käyttää Listaa, joka laajentaa suodatusvaihtoehtoja.
<link to="buildcraftreborn:item/list"/>

<chapter name="Energian syöttö"/>
Se voi saada energiaa mistä tahansa MJ-energialähteestä, kuten moottorista, Energiasovittimesta tai Putkipulssittajasta.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
Jokaisella saamallaan energiapulssilla se poimii tavaroita viereisestä varastosta.
Mitä enemmän energiaa pulssissa on, sitä enemmän tavaroita poimitaan pulssia kohden.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>