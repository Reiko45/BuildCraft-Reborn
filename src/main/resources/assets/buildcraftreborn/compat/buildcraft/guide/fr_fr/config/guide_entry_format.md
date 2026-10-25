Chaque page du guide est une « entrée » : un nom, le sujet de la page et son emplacement dans le sommaire. Les packs de ressources peuvent ajouter, remplacer ou supprimer des entrées.

<chapter name="Fichiers d'entrées"/>
Les entrées sont listées dans un fichier json, dans chaque espace de noms d'un pack de ressources :
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Il contient un seul objet, qui associe le nom de chaque entrée à sa définition. Par exemple :
<json_insn>
{
  "item/wrench": {
    "type": "item_stack",
    "book": "buildcraftreborn:main",
    "tag_type": "item",
    "tag_subtype": "tool",
    "stack": "buildcraftreborn:wrench"
  }
}
</json_insn>
<new_page/>
La page elle-même est un fichier distinct, nommé d'après l'entrée :
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
La page en_us est toujours chargée, et la page dans votre langue (s'il y en a une) la remplace. Consultez <link to="buildcraftreborn:config/guide_page_format"/> pour savoir ce que contient une page.

Les packs de ressources placés plus haut l'emportent : une entrée du même nom remplace celle du dessous, et une entrée définie à null la supprime.

<chapter name="Types d'entrées"/>
Chaque entrée a un "type" :
- item_stack : une page sur un objet (ou un bloc). "stack" est l'identifiant de l'objet.
- statement : une page sur un modificateur ou une action de porte. "statement" est son identifiant, par exemple "buildcraft:redstone.input.active".
- external : une page qui ne porte sur rien du jeu, comme celle-ci. "title" est une clé de traduction, à la suite de "buildcraft.guide.page.".

Une entrée dont l'objet, le modificateur ou l'action n'existe pas (par exemple parce qu'un mod n'est pas installé) est ignorée.

<new_page/>
<chapter name="Sommaire"/>
"book" est le livre dans lequel se trouve l'entrée. Le guide comprend "buildcraftreborn:main" (le Guide de BuildCraft) et "buildcraftreborn:meta" (ce livre-ci).

"tag_type" et "tag_subtype" indiquent l'emplacement de l'entrée dans le sommaire. Ils sont traduits par les clés "buildcraft.guide.chapter.type.[tag_type]" et "buildcraft.guide.chapter.subtype.[tag_subtype]".

Les livres sont listés de la même façon, dans :
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Par exemple :
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" est une clé de traduction, et "all_entries" indique si le sommaire du livre liste aussi tout ce qui ne figure dans aucun livre.

Appuyez sur F3 et T pendant que le guide est ouvert pour tout recharger.

<italic>BuildCraft 8.0 définissait les entrées dans des fichiers "guide.txt", avec son propre langage d'instructions. BuildCraft Reborn utilise plutôt du json simple.</italic>
