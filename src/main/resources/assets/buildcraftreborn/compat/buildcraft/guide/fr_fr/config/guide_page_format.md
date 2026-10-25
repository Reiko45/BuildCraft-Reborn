Les pages du guide sont toutes écrites dans un sous-ensemble du markdown.

<italic>(Cette page est en grande partie à compléter ; voici toutefois une liste des balises de base.)</italic>

# Balises simples autonomes
Chaque balise a la forme &lt;name arguments/&gt;, où name est le nom de la balise, et le '/' final signifie que la balise est autonome. Certaines balises ont aussi des arguments sous forme de « paires clé-valeur », par exemple :
<guide_md>
"level"="42"
</guide_md>

## Nouvelle page
&lt;new_page/&gt; déplace le contenu qui la suit sur la page suivante.

## Liens
Ces balises créent un lien vers une autre page du guide, avec le titre et l'icône de la page liée. Vous pouvez aussi créer un lien vers un ItemStack ou un Statement, même s'ils n'ont pas de page dans le guide.
Par exemple :
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
donne :
<link to="buildcraftreborn:config/guide_entry_format"/>

ou :
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
donne :
<link to="minecraft:iron_ingot" type="item_stack"/>

## Chapitres
&lt;chapter/&gt; crée un « titre de chapitre », affiché dans la liste des chapitres sous forme de titre coloré. Elle accepte un seul argument, "level", qui doit être un entier supérieur ou égal à 0. Vous pouvez aussi utiliser plusieurs dièses (#), comme dans le format markdown habituel, pour créer des titres. Le nombre de # (moins un) correspond au niveau du titre.
Par exemple :
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
donne :
### Chapitre de niveau 3
<chapter name="Un autre chapitre de niveau 3" level="2"/>

# Balises doubles simples
Toutes ces balises doivent entourer du contenu : elles doivent avoir à la fois une balise ouvrante &lt;italic&gt; et une balise fermante &lt;/italic&gt;.
Elles s'appliquent toutes au texte compris entre la balise ouvrante et la balise fermante.

- <italic>italique</italic> (italic)
- <bold>gras</bold> (bold)
- <underline>souligné</underline> (underline)
- <strikethrough>barré</strikethrough> (strikethrough)
- <red>rouge</red> (red)
- <black>noir</black> (black)
- <dark_blue>bleu foncé</dark_blue> (dark_blue)
- <dark_green>vert foncé</dark_green> (dark_green)
- <dark_aqua>cyan foncé</dark_aqua> (dark_aqua)
- <dark_red>rouge foncé</dark_red> (dark_red)
- <dark_purple>violet foncé</dark_purple> (dark_purple)
- <gold>or</gold> (gold)
- <gray>gris</gray> (gray)
- <dark_gray>gris foncé</dark_gray> (dark_gray)
- <blue>bleu</blue> (blue)
- <green>vert</green> (green)
- <aqua>cyan</aqua> (aqua)
- <light_purple>violet clair</light_purple> (light_purple)
- <yellow>jaune</yellow> (yellow)
- <white>blanc</white> (white)

Il est déconseillé d'utiliser le vert, le cyan, le jaune ou le blanc, car ces couleurs sont généralement très difficiles à distinguer sur la page.

# Balises activables
Les balises suivantes dépendent d'options que le joueur peut activer ou désactiver.

## Récit
Si la case "Afficher l'histoire [ ]" est cochée, tout ce qui se trouve dans la balise &lt;no_lore&gt; est masqué ; sinon, c'est tout ce qui se trouve dans la balise &lt;lore&gt; qui est masqué.

La balise lore sert à présenter les informations du guide « dans l'univers du jeu », racontées du point de vue du joueur (comme s'il était en train d'étudier le fonctionnement de chaque chose). L'inverse sert à adopter un ton plus formel, façon Wikipédia, pour transmettre l'information, purement et simplement.

Cette option est activée par défaut dans le jeu, et toujours désactivée dans les exports.

## Astuces
Si la case "Afficher les astuces [ ]" est cochée, tout ce qui se trouve dans la balise &lt;no_hint&gt; est masqué ; sinon, c'est tout ce qui se trouve dans la balise &lt;hint&gt; qui est masqué.

La balise hint sert à donner des « astuces » supplémentaires sur les usages possibles de l'élément décrit, ou sur différentes façons de le combiner avec d'autres blocs ou objets. Il s'agit essentiellement d'« usages recommandés ». (Comme les découvrir fait partie du plaisir, ce n'est pas indispensable.) Cette option est donc désactivée par défaut, mais elle peut être activée dans le jeu, et il est prévu de pouvoir l'activer dans les exports.

## Détails
Si l'option de configuration client "guideBookEnableDetail" est réglée sur "true", tout ce qui se trouve dans la balise &lt;no_detail&gt; est masqué ; sinon, c'est tout ce qui se trouve dans la balise &lt;detail&gt; qui est masqué.

La balise detail sert à afficher tous les chiffres utilisés dans divers calculs, comme le débit des tuyaux, la vitesse d'extraction, la fréquence des impulsions, etc.

Cette option est désactivée par défaut, mais elle peut être activée dans le jeu, et il est prévu de pouvoir l'activer dans les exports.

# Divers
## Recette (singulier)
Cette balise affiche la première recette trouvée pour la pile d'objets donnée. Par exemple :
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Recettes
Cette balise affiche toutes les recettes de la pile d'objets donnée. Par exemple :
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Utilisations
Comme pour les recettes, cette balise affiche toutes les utilisations de la pile d'objets donnée. Par exemple :
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Recettes et utilisations
Cette balise combine en fait les deux balises précédentes en une seule, par exemple :
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Groupe
Cette balise affiche les groupes correspondant à l'objet, au modificateur, à l'action, etc. donnés.

## Images
Cette balise affiche une image. Elle accepte 3 arguments :
* src : l'emplacement source de l'image.
* width : la largeur d'affichage de l'image. Par défaut, il s'agit de la largeur de l'image source (ce qui n'est pas forcément ce que vous voulez).
* height : la hauteur d'affichage de l'image.

Par exemple :
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
