<lore>
Vous voulez extraire un fluide précis et l'envoyer ailleurs ? Alimentez simplement le tuyau et regardez-le s'écouler.
</lore>
<no_lore>
Le Tuyau étanche en bois et diamant sert à extraire des fluides choisis d'une citerne lorsqu'il est alimenté.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_fluid"/>

<chapter name="Fonctionnement du tuyau"/>
Lorsqu'il est alimenté, il extrait le fluide sélectionné par le filtre vers les autres citernes ou tuyaux connectés.
L'extraction se fait par le côté opaque du tuyau, vers les côtés transparents.
S'il a plusieurs côtés transparents, les fluides se répartissent équitablement dans chaque direction.

Certaines machines peuvent y envoyer directement des fluides sans qu'il soit alimenté (le filtre n'est alors pas respecté).
Les Bouchons pour tuyau, ou des tuyaux peints de couleurs différentes, empêchent les tuyaux de se connecter.

<chapter name="Filtrage"/>
L'interface du Tuyau étanche en bois et diamant vous permet de configurer un filtre.
En plaçant un seau (ou un autre contenant) du fluide voulu dans les emplacements, vous filtrez ce qu'il extrait.
Les options Liste blanche et Liste noire permettent de changer le comportement du filtre :
Liste blanche : seuls les fluides sélectionnés sont extraits.
Liste noire : tous les fluides sauf celui sélectionné sont extraits.

<chapter name="Alimentation"/>
Il peut recevoir de l'énergie de n'importe quelle source de MJ : un moteur, un adaptateur d'énergie ou un pulsar de tuyau.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
À chaque impulsion d'énergie reçue, il extrait du fluide de la citerne adjacente.
Plus une impulsion est puissante, plus la quantité de fluide extraite par impulsion est grande.

<no_detail>
Le tuyau étanche en bois et diamant transporte les fluides vers les tuyaux et les citernes adjacents à 1,6 seau par seconde.
Les tuyaux étanches ont un réservoir interne qui peut contenir 1 seau par côté et 1 seau au centre.
</no_detail>

<usages stack="buildcraftreborn:pipe_diamond_wood_fluid"/>