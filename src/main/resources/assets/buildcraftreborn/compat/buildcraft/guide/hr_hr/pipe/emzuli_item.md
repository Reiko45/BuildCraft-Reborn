<lore>
Izvlačenje određenih predmeta pomoću logičkog sklopa najnovija je tehnička revolucija u tehnologiji filtriranja.
</lore>
<no_lore>
Emzuli transportna cijev je cijev kojom upravlja logički sklop, a kada dobije energiju, izvlači iz inventara predmete određene njezinim filtrom.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="Mehanika cijevi"/>
Kada je napajana, izvlači predmete odabrane u filtru i prenosi ih u ostale spojene inventare ili cijevi.
Predmeti se izvlače kroz punu stranu cijevi i prenose prema prozirnim stranama.
Ako cijev ima više prozirnih strana, predmeti nastavljaju u nasumičnom smjeru.
Ako izvučeni predmeti nemaju kamo otići, ispadaju iz cijevi na tlo.

Neki strojevi mogu automatski izravno ispuštati predmete u Emzuli cijev, a da cijev ne mora biti napajana (pritom se filtri ne poštuju).
Spajanje cijevi možete spriječiti Cijevnim čepovima ili bojanjem cijevi u različite boje.

<chapter name="Filtriranje"/>
U sučelju Emzuli transportne cijevi nalaze se četiri boje za četiri zasebna filtra.
Stavljanjem predmeta u utore određujete što izvlači pojedini filtar.
Boje kistova pokraj utora određuju u koju će se boju predmeti obojiti pri izvlačenju, a mijenjaju se klikom na njih.
Za odabir filtra prema kojem se izvlači potreban je logički sklop na cijevi s radnjom obojene postavke izvlačenja. Npr. signal crvenog kamena → Postavka izvlačenja: Crvena.

U utore za filtriranje možete staviti i Popis, koji proširuje mogućnosti filtriranja.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Napajanje"/>
Energiju može primati iz bilo kojeg izvora MJ, primjerice iz motora, Strujnog adaptera ili Cijevnog pulsara.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Pri svakom primljenom energetskom pulsu izvlači predmete iz susjednog inventara.
Što puls nosi više energije, to se po pulsu izvlači više predmeta.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>