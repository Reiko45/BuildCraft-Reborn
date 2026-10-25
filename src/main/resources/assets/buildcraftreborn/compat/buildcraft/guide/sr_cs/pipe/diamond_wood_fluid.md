<lore>
Želite da izvučete određenu tečnost i prenesete je negde drugde? Samo dovedite energiju cevi i gledajte kako tečnost teče.
</lore>
<no_lore>
Drvena dijamantna vodootporna cev izvlači izabrane tečnosti iz rezervoara kada dobije energiju.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_fluid"/>

<chapter name="Mehanika cevi"/>
Kada se napaja, izvlači tečnost izabranu filterom u ostale povezane rezervoare ili cevi.
Tečnost se izvlači sa pune strane cevi i šalje ka providnim stranama.
Ako cev ima više providnih strana, tečnost se ravnomerno deli na sve smerove.

Neke mašine mogu da izbacuju tečnost direktno u nju bez potrebe za energijom (pri tom se filter ne poštuje).
Cevni čepovi ili bojenje cevi različitim bojama mogu da spreče povezivanje cevi.

<chapter name="Filtriranje"/>
Kada otvorite interfejs Drvene dijamantne vodootporne cevi, možete da podesite filter.
Ako u slotove stavite kofu (ili drugu posudu) sa željenom tečnošću, određujete šta cev izvlači.
Opcijama „Bela lista“ i „Crna lista“ menjate način rada filtera:
Bela lista: izvlače se samo izabrane tečnosti.
Crna lista: izvlači se svaka tečnost osim izabrane.

<chapter name="Napajanje"/>
Može da prima energiju iz bilo kog izvora MJ, kao što su motor, Strujni adapter ili Cevni pulsar.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
Sa svakim impulsom energije koji primi, izvlači tečnost iz susednog rezervoara.
Što impuls nosi više energije, to se po impulsu izvuče više tečnosti.

<no_detail>
Drvena dijamantna vodootporna cev prenosi tečnost u susedne cevi i rezervoare brzinom od 1,6 kofe u sekundi.
Vodootporne cevi imaju unutrašnji rezervoar u koji stane po 1 kofa za svaku stranu i 1 kofa u sredini.
</no_detail>

<usages stack="buildcraftreborn:pipe_diamond_wood_fluid"/>