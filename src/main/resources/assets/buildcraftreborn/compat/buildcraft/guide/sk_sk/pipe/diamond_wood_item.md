<lore>
Chcete vybrať určité predmety a prepraviť ich inam? Stačí dodať energiu a sledovať, ako plynú.
</lore>
<no_lore>
Drevená diamantová transportačná rúra po dodaní energie vyťahuje z inventára predmety určené jej filtrom.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="Mechanika rúry"/>
Keď je napájaná, vyťahuje predmety zvolené vo filtri a prepravuje ich do ďalších pripojených inventárov či rúr.
Predmety sa vyťahujú cez plnú stranu rúry a prepravujú sa smerom k priehľadným stranám.
Ak má rúra viacero priehľadných strán, predmety pokračujú náhodným smerom.
Ak vytiahnuté predmety nemajú kam ísť, vypadnú z rúry na zem.

Niektoré stroje dokážu automaticky vydávať predmety priamo do Drevenej diamantovej transportačnej rúry bez toho, aby musela byť napájaná (filter sa vtedy neuplatní).
Spájaniu rúr zabránite pomocou Napojení rúr alebo zafarbením rúr inou farbou.

<chapter name="Filtrovanie"/>
V rozhraní Drevenej diamantovej transportačnej rúry môžete nastaviť filter.
Vložením požadovaných predmetov do slotov určíte, čo bude rúra vyťahovať.
Pomocou možností Biela listina, Čierna listina a Postupné striedanie zmeníte, ako filter funguje:
Biela listina: Vyťahujú sa iba vybrané predmety.
Čierna listina: Vyťahujú sa akékoľvek predmety okrem vybraných.
Postupné striedanie: Vyťahuje predmety presne v poradí, v akom sú nastavené vo filtračných slotoch vyššie (zľava doprava).

Do filtračných slotov môžete vložiť aj Zoznam, ktorý rozšíri možnosti filtrovania.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Napájanie"/>
Energiu môže prijímať z akéhokoľvek zdroja MJ, napríklad z motora, Energetického adaptéra alebo Potrubného pulzara.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Pri každom prijatom energetickom pulze vytiahne predmety zo susedného inventára.
Čím viac energie pulz nesie, tým viac predmetov sa ním vytiahne.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>