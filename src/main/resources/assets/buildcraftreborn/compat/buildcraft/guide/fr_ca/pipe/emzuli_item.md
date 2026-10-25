<lore>
Utiliser une porte pour extraire certains objets, c'est la toute dernière révolution technique en matière de filtrage.
</lore>
<no_lore>
Le Tuyau de transport en emzuli est un tuyau contrôlé par une porte, qui sert à extraire d'un inventaire les objets choisis par son filtre lorsqu'il est alimenté.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="Fonctionnement du tuyau"/>
Lorsqu'il est alimenté, il extrait les objets sélectionnés dans le filtre et les transporte vers les autres inventaires ou tuyaux connectés.
L'extraction se fait par le côté opaque du tuyau, et les objets sont transportés vers les côtés transparents.
S'il a plusieurs côtés transparents, les objets partent dans une direction aléatoire.
Si les objets extraits n'ont nulle part où aller, ils sont éjectés du tuyau et tombent au sol.

Certaines machines peuvent envoyer automatiquement des objets directement dans un tuyau en emzuli sans que celui-ci soit alimenté (les filtres ne sont alors pas respectés).
Les Bouchons pour tuyau, ou des tuyaux peints de couleurs différentes, empêchent les tuyaux de se connecter.

<chapter name="Filtrage"/>
L'interface du Tuyau de transport en emzuli affiche quatre couleurs, pour quatre filtres distincts.
En plaçant des objets dans les emplacements, vous définissez ce que chaque filtre extrait.
La couleur des brosses à peinture à côté des emplacements détermine la couleur dont les objets seront colorés à l'extraction ; vous pouvez la changer en cliquant dessus.
Pour choisir lequel des quatre filtres est utilisé, il faut placer sur le tuyau une porte avec une action « Extraction prédéfinie » de la couleur voulue. Par exemple : Signal de redstone -> Extraction prédéfinie rouge.

Une Liste peut être placée dans un emplacement de filtre pour élargir les options de filtrage.
<link to="buildcraftreborn:item/list"/>

<chapter name="Alimentation"/>
Il peut recevoir de l'énergie de n'importe quelle source de MJ : un moteur, un adaptateur d'énergie ou un pulsar de tuyau.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
À chaque impulsion d'énergie reçue, il extrait des objets de l'inventaire adjacent.
Plus une impulsion est puissante, plus le nombre d'objets extraits par impulsion est grand.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>