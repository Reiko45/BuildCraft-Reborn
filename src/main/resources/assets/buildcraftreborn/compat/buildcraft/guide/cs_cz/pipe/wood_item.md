<lore>
Chceš vytahovat předměty a dopravit je jinam? Stačí dodat energii a sledovat, jak putují.
</lore>
<no_lore>
Dřevěná transportní trubka po dodání energie vytahuje předměty z inventáře.
</no_lore>

<recipes stack="buildcraftreborn:pipe_wood_item"/>

<chapter name="Mechanika trubky"/>
Po dodání energie vytahuje předměty a posílá je do dalších připojených inventářů či trubek.
Předměty se vytahují z plné strany trubky a posílají se směrem k průhledným stranám.
Pokud má trubka více průhledných stran, předměty se mezi ně náhodně rozdělují.
Pokud vytažené předměty nemají kam jít, vypadnou z trubky na zem.

Některé stroje dokážou automaticky posílat předměty přímo do dřevěné transportní trubky, i když trubka nemá energii.
Propojení trubek zabráníš oddělovači trubek nebo obarvením trubek různými barvami.

<chapter name="Napájení"/>
Energii může dostávat z jakéhokoli zdroje MJ, například z motoru, energetického adaptéru nebo trubkového pulzaru.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
Při každém přijatém pulzu energie vytáhne předměty ze sousedního inventáře.
Čím víc energie pulz má, tím víc předmětů se jím vytáhne.

<usages stack="buildcraftreborn:pipe_wood_item"/>