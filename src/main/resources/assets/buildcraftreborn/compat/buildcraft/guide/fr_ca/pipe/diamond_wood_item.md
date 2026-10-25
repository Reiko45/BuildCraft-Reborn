<lore>
Vous voulez extraire certains objets et les envoyer ailleurs ? Alimentez simplement le tuyau et regardez-les défiler.
</lore>
<no_lore>
Le Tuyau de transport en bois et diamant sert à extraire d'un inventaire les objets choisis par son filtre lorsqu'il est alimenté.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="Fonctionnement du tuyau"/>
Lorsqu'il est alimenté, il extrait les objets sélectionnés dans le filtre et les transporte vers les autres inventaires ou tuyaux connectés.
L'extraction se fait par le côté opaque du tuyau, et les objets sont transportés vers les côtés transparents.
S'il a plusieurs côtés transparents, les objets partent dans une direction aléatoire.
Si les objets extraits n'ont nulle part où aller, ils sont éjectés du tuyau et tombent au sol.

Certaines machines peuvent envoyer automatiquement des objets directement dans un Tuyau de transport en bois et diamant sans que celui-ci soit alimenté (le filtre n'est alors pas respecté).
Les Bouchons pour tuyau, ou des tuyaux peints de couleurs différentes, empêchent les tuyaux de se connecter.

<chapter name="Filtrage"/>
L'interface du Tuyau de transport en bois et diamant vous permet de configurer un filtre.
En plaçant les objets voulus dans les emplacements, vous filtrez ce qu'il extrait.
Les options Liste blanche, Liste noire et Tourniquet permettent de changer le comportement du filtre :
Liste blanche : seuls les objets sélectionnés sont extraits.
Liste noire : tous les objets sauf ceux sélectionnés sont extraits.
Tourniquet : extrait les objets dans l'ordre exact des emplacements de filtre ci-dessus (de gauche à droite).

Une Liste peut être placée dans un emplacement de filtre pour élargir les options de filtrage.
<link to="buildcraftreborn:item/list"/>

<chapter name="Alimentation"/>
Il peut recevoir de l'énergie de n'importe quelle source de MJ : un moteur, un adaptateur d'énergie ou un pulsar de tuyau.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
À chaque impulsion d'énergie reçue, il extrait des objets de l'inventaire adjacent.
Plus une impulsion est puissante, plus le nombre d'objets extraits par impulsion est grand.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>