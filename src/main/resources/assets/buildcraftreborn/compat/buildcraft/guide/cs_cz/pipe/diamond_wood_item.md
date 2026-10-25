<lore>
Chceš vytahovat určité předměty a dopravit je jinam? Stačí dodat energii a sledovat, jak putují.
</lore>
<no_lore>
Dřevěná diamantová transportní trubka po dodání energie vytahuje z inventáře předměty zvolené v jejím filtru.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="Mechanika trubky"/>
Po dodání energie vytahuje předměty zvolené ve filtru a posílá je do dalších připojených inventářů či trubek.
Předměty se vytahují z plné strany trubky a posílají se směrem k průhledným stranám.
Pokud má trubka více průhledných stran, předměty poputují náhodným směrem.
Pokud vytažené předměty nemají kam jít, vypadnou z trubky na zem.

Některé stroje dokážou automaticky posílat předměty přímo do dřevěné diamantové transportní trubky, i když trubka nemá energii (filtr se pak nebere v úvahu).
Propojení trubek zabráníš oddělovači trubek nebo obarvením trubek různými barvami.

<chapter name="Filtrování"/>
Po otevření rozhraní dřevěné diamantové transportní trubky můžeš nastavit filtr.
Vložením požadovaných předmětů do slotů určíš, co trubka vytahuje.
Volbami „Seznam povolených“, „Seznam zakázaných“ a „Postupně“ můžeš změnit, jak filtr funguje:
Seznam povolených: vytahují se jen zvolené předměty.
Seznam zakázaných: vytahují se všechny předměty kromě zvolených.
Postupně: vytahuje předměty přesně v pořadí nastaveném ve slotech filtru výše (zleva doprava)

Do slotu filtru lze vložit seznam a získat tak rozšířené možnosti filtrování.
<link to="buildcraftreborn:item/list"/>

<chapter name="Napájení"/>
Energii může dostávat z jakéhokoli zdroje MJ, například z motoru, energetického adaptéru nebo trubkového pulzaru.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
Při každém přijatém pulzu energie vytáhne předměty ze sousedního inventáře.
Čím víc energie pulz má, tím víc předmětů se jím vytáhne.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>