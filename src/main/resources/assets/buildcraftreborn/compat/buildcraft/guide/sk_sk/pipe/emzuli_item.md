<lore>
Vyťahovanie určitých predmetov pomocou brány je najnovšou technickou revolúciou vo filtrovaní.
</lore>
<no_lore>
Lazuraldová transportačná rúra je rúra ovládaná bránou, ktorá po dodaní energie vyťahuje z inventára predmety určené jej filtrom.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="Mechanika rúry"/>
Keď je napájaná, vyťahuje predmety zvolené vo filtri a prepravuje ich do ďalších pripojených inventárov či rúr.
Predmety sa vyťahujú cez plnú stranu rúry a prepravujú sa smerom k priehľadným stranám.
Ak má rúra viacero priehľadných strán, predmety pokračujú náhodným smerom.
Ak vytiahnuté predmety nemajú kam ísť, vypadnú z rúry na zem.

Niektoré stroje dokážu automaticky vydávať predmety priamo do Lazuraldovej rúry bez toho, aby musela byť napájaná (filtre sa vtedy neuplatnia).
Spájaniu rúr zabránite pomocou Napojení rúr alebo zafarbením rúr inou farbou.

<chapter name="Filtrovanie"/>
V rozhraní Lazuraldovej transportačnej rúry nájdete štyri farby pre štyri samostatné filtre.
Vložením predmetov do slotov určíte, čo bude každý filter vyťahovať.
Farby štetcov vedľa slotov určujú, akou farbou sa predmety pri vytiahnutí zafarbia; kliknutím na štetec farbu zmeníte.
Na výber filtra, podľa ktorého sa bude vyťahovať, potrebujete na rúre bránu s akciou farebnej predvoľby extrakcie. Napr. ruditový signál → Červená predvoľba extrakcie.

Do filtračných slotov môžete vložiť aj Zoznam, ktorý rozšíri možnosti filtrovania.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Napájanie"/>
Energiu môže prijímať z akéhokoľvek zdroja MJ, napríklad z motora, Energetického adaptéra alebo Potrubného pulzara.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Pri každom prijatom energetickom pulze vytiahne predmety zo susedného inventára.
Čím viac energie pulz nesie, tým viac predmetov sa ním vytiahne.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>