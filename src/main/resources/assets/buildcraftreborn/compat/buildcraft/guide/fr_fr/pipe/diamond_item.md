<lore>
Choisir quels objets peuvent aller où pourrait être utile à votre système.
</lore>
<no_lore>
Le Tuyau de transport en diamant sert à transporter les objets d'un tuyau, dans une direction définie par son filtre, vers un autre tuyau ou un inventaire adjacent.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_item"/>

<chapter name="Régler le filtre"/>
Chaque côté du Tuyau de transport en diamant est identifié par une couleur différente.
L'interface du Tuyau de transport en diamant vous permet de configurer un filtre.
En plaçant les objets voulus dans les emplacements d'une couleur, vous choisissez quels objets peuvent aller dans quelle direction.
Placer plusieurs fois le même objet dans une couleur permet de donner plus de « poids » à cette direction.

Une Liste peut être placée dans un emplacement de filtre pour élargir les options de filtrage.
<link to="buildcraftreborn:item/list"/>

<chapter name="Fonctionnement du tuyau"/>
Si les objets peuvent aller dans plusieurs directions, ils sont répartis entre elles (selon le poids que vous leur donnez).
Toute direction valide sans filtre laisse passer n'importe quel objet, mais les directions filtrées sont prioritaires.
Les objets ne repartent jamais dans la direction d'où ils viennent, seulement dans une autre direction.
S'il n'y a aucun tuyau connecté, ou si l'inventaire adjacent est plein et que l'objet n'a nulle part ailleurs où aller, il est éjecté du tuyau et tombe au sol.

Certaines machines peuvent envoyer automatiquement des objets directement dans un Tuyau de transport en diamant, sans passer par un tuyau d'extraction.
Les Bouchons pour tuyau, ou des tuyaux peints de couleurs différentes, empêchent les tuyaux de se connecter.

<usages stack="buildcraftreborn:pipe_diamond_item"/>