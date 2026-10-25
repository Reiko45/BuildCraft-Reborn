<lore>
Chceš vytáhnout určitou tekutinu a dopravit ji jinam? Stačí dodat energii a sledovat, jak teče.
</lore>
<no_lore>
Dřevěná diamantová vodovodní trubka po dodání energie vytahuje z nádrže zvolené tekutiny.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_fluid"/>

<chapter name="Mechanika trubky"/>
Po dodání energie vytahuje tekutinu zvolenou ve filtru do dalších připojených nádrží či trubek.
Tekutiny se vytahují z plné strany trubky směrem k průhledným stranám.
Pokud má trubka více průhledných stran, tekutiny se rovnoměrně rozdělí do všech směrů.

Některé stroje dokážou posílat tekutiny přímo do této trubky i bez energie (filtr se pak nebere v úvahu).
Propojení trubek zabráníš oddělovači trubek nebo obarvením trubek různými barvami.

<chapter name="Filtrování"/>
Po otevření rozhraní dřevěné diamantové vodovodní trubky můžeš nastavit filtr.
Vložením kbelíku (nebo jiné nádoby na tekutinu) s požadovanou tekutinou do slotů určíš, co trubka vytahuje.
Volbami „Seznam povolených“ a „Seznam zakázaných“ můžeš změnit, jak filtr funguje:
Seznam povolených: vytahují se jen zvolené tekutiny.
Seznam zakázaných: vytahuje se jakákoli tekutina kromě zvolené.

<chapter name="Napájení"/>
Energii může dostávat z jakéhokoli zdroje MJ, například z motoru, energetického adaptéru nebo trubkového pulzaru.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
Při každém přijatém pulzu energie vytáhne tekutinu ze sousední nádrže.
Čím víc energie pulz má, tím víc tekutiny se jím vytáhne.

<no_detail>
Dřevěná diamantová vodovodní trubka přepravuje tekutiny do sousedních trubek a nádrží rychlostí 1,6 kbelíku za sekundu.
Vodovodní trubky mají vnitřní nádržku, která pojme 1 kbelík na každou stranu a 1 kbelík uprostřed.
</no_detail>

<usages stack="buildcraftreborn:pipe_diamond_wood_fluid"/>