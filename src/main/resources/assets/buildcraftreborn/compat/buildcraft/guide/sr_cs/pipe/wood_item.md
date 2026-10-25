<lore>
Želite da izvučete predmete i prenesete ih negde drugde? Samo dovedite energiju cevi i gledajte kako putuju.
</lore>
<no_lore>
Drvena transportna cev izvlači predmete iz inventara kada dobije energiju.
</no_lore>

<recipes stack="buildcraftreborn:pipe_wood_item"/>

<chapter name="Mehanika cevi"/>
Kada se napaja, izvlači predmete i prenosi ih u ostale povezane inventare ili cevi.
Predmeti se izvlače sa pune strane cevi i prenose ka providnim stranama.
Ako cev ima više providnih strana, predmeti se nasumično raspoređuju među njima.
Ako izvučeni predmeti nemaju kuda da idu, ispadaju iz cevi na zemlju.

Neke mašine mogu automatski da izbacuju predmete direktno u Drvenu transportnu cev, a da cev ne mora da se napaja.
Cevni čepovi ili bojenje cevi različitim bojama mogu da spreče povezivanje cevi.

<chapter name="Napajanje"/>
Može da prima energiju iz bilo kog izvora MJ, kao što su motor, Strujni adapter ili Cevni pulsar.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
Sa svakim impulsom energije koji primi, izvlači predmete iz susednog inventara.
Što impuls nosi više energije, to se po impulsu izvuče više predmeta.

<usages stack="buildcraftreborn:pipe_wood_item"/>