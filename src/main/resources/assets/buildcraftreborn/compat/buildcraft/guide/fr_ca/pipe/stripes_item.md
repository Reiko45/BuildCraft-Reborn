<lore>
Imaginez un tuyau capable de poser, de casser et d'utiliser des blocs. C'est exactement le tuyau que vous imaginez.
</lore>
<no_lore>
Le Tuyau de transport rayé sert à casser des blocs, à poser des blocs, à utiliser les objets qu'on y fait passer, et à les transporter vers un tuyau ou un inventaire connecté.
</no_lore>

<recipes stack="buildcraftreborn:pipe_stripes_item"/>

<chapter name="Utilisation du tuyau"/>
Lorsqu'il est alimenté, il casse le bloc situé juste devant lui et le transmet au tuyau ou à l'inventaire adjacent.
Lorsqu'un bloc le traverse, il le pose juste devant lui.
Lorsqu'un objet le traverse, il essaie de l'« utiliser » sur le bloc ou l'entité situé devant lui.

<chapter name="Orientation du tuyau"/>
Un Tuyau de transport rayé doit « pointer » dans une direction pour agir sur le bloc situé devant lui.
Si plusieurs tuyaux ou inventaires y sont connectés, il perd sa capacité à agir sur le monde.

<chapter name="Fonctionnement du tuyau"/>
Si les objets en transit n'ont nulle part où aller, ils sont éjectés du tuyau et tombent au sol.
Les Bouchons pour tuyau, ou des tuyaux peints de couleurs différentes, empêchent les tuyaux de se connecter.

<chapter name="Alimentation"/>
Il peut recevoir de l'énergie de n'importe quelle source de MJ : un moteur, un adaptateur d'énergie ou un pulsar de tuyau.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
À chaque impulsion d'énergie reçue, il utilise cette énergie pour tenter de casser le bloc situé devant lui.
Plus une impulsion est puissante, plus l'énergie utilisée pour casser le bloc est grande.

<usages stack="buildcraftreborn:pipe_stripes_item"/>