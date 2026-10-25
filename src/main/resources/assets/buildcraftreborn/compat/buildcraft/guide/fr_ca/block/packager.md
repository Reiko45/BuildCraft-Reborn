<lore>
Une recette, pliée et mise à la poste.
</lore>
<no_lore>
L'Empaqueteur emballe les ingrédients d'une recette de fabrication dans un paquet, prêt à être fabriqué par une Table d'étiquetage.
</no_lore>
<chapter name="Le modèle"/>
La grille 3x3 est la grille de fabrication de la recette. Cliquez sur un emplacement avec un objet pour que cet emplacement demande cet objet. Cliquez sur un emplacement vide à main nue pour qu'il demande « n'importe quoi » (il s'allume), et cliquez de nouveau pour le désactiver. Cliquer à main nue sur un emplacement qui demande un objet le vide.
Tous les emplacements qui demandent « n'importe quoi » reçoivent le même objet, un objet qu'aucun autre emplacement ne demande explicitement.
<recipes_usages stack="buildcraftreborn:packager"/>
<chapter name="Emballage"/>
Placez du papier dans l'emplacement en haut à droite. Tous les quarts de seconde, l'empaqueteur prend un objet pour chaque emplacement allumé, d'abord dans ses neuf emplacements de stockage, puis dans les inventaires de ses quatre côtés, et transforme une feuille de papier en paquet contenant chaque objet à sa place. Le paquet sort dans l'emplacement situé sous le papier, et l'empaqueteur attend qu'il soit retiré.
Vous pouvez remettre un paquet à la place du papier pour remplir ses emplacements vides : l'empaqueteur ne met rien là où le paquet contient déjà quelque chose.
<chapter name="Automatisation"/>
Les tuyaux insèrent les ingrédients dans les emplacements de stockage par les côtés (uniquement les objets qui complètent une pile déjà présente ou que le modèle peut utiliser), le papier ou les paquets par le haut ou par le bas, et extraient les paquets par n'importe quel côté. Les robots livreurs peuvent apporter les paquets à une Table d'étiquetage.
