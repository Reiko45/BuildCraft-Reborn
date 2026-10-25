<lore>
Želite da izvučete određene predmete i prenesete ih negde drugde? Samo dovedite energiju cevi i gledajte kako putuju.
</lore>
<no_lore>
Drvena dijamantna transportna cev, kada dobije energiju, izvlači iz inventara predmete izabrane njenim filterom.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="Mehanika cevi"/>
Kada se napaja, izvlači predmete izabrane u filteru i prenosi ih u ostale povezane inventare ili cevi.
Predmeti se izvlače sa pune strane cevi i prenose ka providnim stranama.
Ako cev ima više providnih strana, predmeti idu u nasumičnom smeru.
Ako izvučeni predmeti nemaju kuda da idu, ispadaju iz cevi na zemlju.

Neke mašine mogu automatski da izbacuju predmete direktno u Drvenu dijamantnu transportnu cev, a da cev ne mora da se napaja (pri tom se filter ne poštuje).
Cevni čepovi ili bojenje cevi različitim bojama mogu da spreče povezivanje cevi.

<chapter name="Filtriranje"/>
Kada otvorite interfejs Drvene dijamantne transportne cevi, možete da podesite filter.
Ako željene predmete stavite u slotove, određujete šta cev izvlači.
Opcijama „Bela lista“, „Crna lista“ i „Kružni redosled“ menjate način rada filtera:
Bela lista: izvlače se samo izabrani predmeti.
Crna lista: izvlače se svi predmeti osim izabranih.
Kružni redosled: izvlači predmete tačno onim redom kojim su izabrani u gornjim slotovima filtera (sleva nadesno)

Lista može da se stavi u slot za filtriranje i tako pruži proširene mogućnosti filtriranja.
<link to="buildcraftreborn:item/list"/>

<chapter name="Napajanje"/>
Može da prima energiju iz bilo kog izvora MJ, kao što su motor, Strujni adapter ili Cevni pulsar.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
Sa svakim impulsom energije koji primi, izvlači predmete iz susednog inventara.
Što impuls nosi više energije, to se po impulsu izvuče više predmeta.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>