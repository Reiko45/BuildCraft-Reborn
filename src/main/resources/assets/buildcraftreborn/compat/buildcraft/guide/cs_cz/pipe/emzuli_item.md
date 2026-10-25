<lore>
Vytahování určitých předmětů pomocí brány je nejnovější revolucí ve filtrovací technologii.
</lore>
<no_lore>
Emzuli transportní trubka je trubka ovládaná bránou, která po dodání energie vytahuje z inventáře předměty zvolené v jejím filtru.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="Mechanika trubky"/>
Po dodání energie vytahuje předměty zvolené ve filtru a posílá je do dalších připojených inventářů či trubek.
Předměty se vytahují z plné strany trubky a posílají se směrem k průhledným stranám.
Pokud má trubka více průhledných stran, předměty poputují náhodným směrem.
Pokud vytažené předměty nemají kam jít, vypadnou z trubky na zem.

Některé stroje dokážou automaticky posílat předměty přímo do trubky Emzuli, i když trubka nemá energii (filtry se pak neberou v úvahu).
Propojení trubek zabráníš oddělovači trubek nebo obarvením trubek různými barvami.

<chapter name="Filtrování"/>
Rozhraní Emzuli transportní trubky obsahuje čtyři barvy pro čtyři samostatné filtry.
Vložením předmětů do slotů určíš, co který filtr vytahuje.
Barvy štětců vedle slotů určují, jakou barvou se předměty po vytažení obarví; kliknutím na ně je můžeš změnit.
Aby bylo možné zvolit, který ze čtyř filtrů se použije, musí být na trubce brána s barevnou akcí „Předvolba extrakce“. Např. ruditový signál -> „Červená předvolba extrakce“.

Do slotu filtru lze vložit seznam a získat tak rozšířené možnosti filtrování.
<link to="buildcraftreborn:item/list"/>

<chapter name="Napájení"/>
Energii může dostávat z jakéhokoli zdroje MJ, například z motoru, energetického adaptéru nebo trubkového pulzaru.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
Při každém přijatém pulzu energie vytáhne předměty ze sousedního inventáře.
Čím víc energie pulz má, tím víc předmětů se jím vytáhne.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>