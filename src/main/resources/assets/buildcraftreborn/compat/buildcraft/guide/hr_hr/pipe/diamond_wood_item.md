<lore>
Želite izvući određene predmete i prenijeti ih negdje drugdje? Samo dovedite energiju i gledajte kako teku.
</lore>
<no_lore>
Drvena dijamantna transportna cijev kada dobije energiju izvlači iz inventara predmete određene njezinim filtrom.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="Mehanika cijevi"/>
Kada je napajana, izvlači predmete odabrane u filtru i prenosi ih u ostale spojene inventare ili cijevi.
Predmeti se izvlače kroz punu stranu cijevi i prenose prema prozirnim stranama.
Ako cijev ima više prozirnih strana, predmeti nastavljaju u nasumičnom smjeru.
Ako izvučeni predmeti nemaju kamo otići, ispadaju iz cijevi na tlo.

Neki strojevi mogu automatski izravno ispuštati predmete u Drvenu dijamantnu transportnu cijev, a da cijev ne mora biti napajana (pritom se filtar ne poštuje).
Spajanje cijevi možete spriječiti Cijevnim čepovima ili bojanjem cijevi u različite boje.

<chapter name="Filtriranje"/>
U sučelju Drvene dijamantne transportne cijevi možete postaviti filtar.
Stavljanjem željenih predmeta u utore određujete što će cijev izvlačiti.
Pomoću opcija Bijela lista, Crna lista i Kružni redoslijed mijenjate način rada filtra:
Bijela lista: Izvlače se samo odabrani predmeti.
Crna lista: Izvlače se bilo koji predmeti osim odabranih.
Kružni redoslijed: Izvlači predmete točno redoslijedom kojim su postavljeni u gornjim utorima filtra (slijeva nadesno).

U utore za filtriranje možete staviti i Popis, koji proširuje mogućnosti filtriranja.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Napajanje"/>
Energiju može primati iz bilo kojeg izvora MJ, primjerice iz motora, Strujnog adaptera ili Cijevnog pulsara.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Pri svakom primljenom energetskom pulsu izvlači predmete iz susjednog inventara.
Što puls nosi više energije, to se po pulsu izvlači više predmeta.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>