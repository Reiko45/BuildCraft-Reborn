<lore>
Izvlačenje određenih predmeta pomoću logičke kapije najnovija je tehnička revolucija u tehnologiji filtriranja.
</lore>
<no_lore>
Emzuli transportna cev je cev kojom upravlja logička kapija i koja, kada dobije energiju, izvlači iz inventara predmete izabrane njenim filterom.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="Mehanika cevi"/>
Kada se napaja, izvlači predmete izabrane u filteru i prenosi ih u ostale povezane inventare ili cevi.
Predmeti se izvlače sa pune strane cevi i prenose ka providnim stranama.
Ako cev ima više providnih strana, predmeti idu u nasumičnom smeru.
Ako izvučeni predmeti nemaju kuda da idu, ispadaju iz cevi na zemlju.

Neke mašine mogu automatski da izbacuju predmete direktno u Emzuli cev, a da cev ne mora da se napaja (pri tom se filteri ne poštuju).
Cevni čepovi ili bojenje cevi različitim bojama mogu da spreče povezivanje cevi.

<chapter name="Filtriranje"/>
U interfejsu Emzuli transportne cevi nalaze se četiri boje za četiri zasebna filtera.
Predmetima koje stavite u slotove određujete šta svaki filter izvlači.
Boje četkica pored slotova određuju kojom će bojom predmeti biti obojeni pri izvlačenju, a menjaju se klikom na četkice.
Da biste izabrali koji se od četiri filtera izvlači, na cev mora biti postavljena logička kapija sa akcijom „Postavka izvlačenja“ u odgovarajućoj boji. Npr. Crvenokamenski signal - > Postavka izvlačenja: Crvena.

Lista može da se stavi u slot za filtriranje i tako pruži proširene mogućnosti filtriranja.
<link to="buildcraftreborn:item/list"/>

<chapter name="Napajanje"/>
Može da prima energiju iz bilo kog izvora MJ, kao što su motor, Strujni adapter ili Cevni pulsar.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
Sa svakim impulsom energije koji primi, izvlači predmete iz susednog inventara.
Što impuls nosi više energije, to se po impulsu izvuče više predmeta.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>