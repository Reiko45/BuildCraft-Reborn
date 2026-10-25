Cada página de la guía es una "entrada": un nombre, de qué trata la página y dónde aparece en el contenido. Los paquetes de recursos pueden añadir entradas, reemplazarlas o eliminarlas.

<chapter name="Archivos de entradas"/>
Las entradas se enumeran en un archivo json dentro de cada espacio de nombres (namespace) de un paquete de recursos:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Contiene un único objeto que asocia el nombre de cada entrada con su definición. Por ejemplo:
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
La página en sí es un archivo aparte, con el nombre de la entrada:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
La página en_us siempre se carga, y la página en tu idioma (si existe) la reemplaza. Consulta <link to="buildcraftreborn:config/guide_page_format"/> para saber qué puede contener una página.

Los paquetes de recursos situados más arriba tienen prioridad: una entrada con el mismo nombre reemplaza a la de abajo, y una entrada con valor null la elimina.

<chapter name="Tipos de entrada"/>
Cada entrada tiene un "type":
- item_stack: una página sobre un objeto (o bloque). "stack" es el id del objeto.
- statement: una página sobre un disparador o una acción de compuerta. "statement" es su id, como "buildcraft:redstone.input.active".
- external: una página que no trata sobre nada del juego, como esta. "title" es una clave de traducción, que va después de "buildcraft.guide.page.".

Las entradas cuyo objeto o statement no existe (por ejemplo, porque un mod no está instalado) se omiten.

<new_page/>
<chapter name="Contenido"/>
"book" es el libro en el que está la entrada. El libro guía tiene "buildcraftreborn:main" (el Libro guía de BuildCraft) y "buildcraftreborn:meta" (este libro).

"tag_type" y "tag_subtype" indican dónde aparece la entrada en la página de contenido. Se traducen como "buildcraft.guide.chapter.type.[tag_type]" y "buildcraft.guide.chapter.subtype.[tag_subtype]".

Los libros se enumeran de la misma manera, en:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Por ejemplo:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" es una clave de traducción, y "all_entries" indica si el contenido del libro también enumera todo lo que no está en ningún libro.

Presiona F3 y T con la guía abierta para volver a cargarlo todo.

<italic>BuildCraft 8.0 definía las entradas en archivos "guide.txt", con su propio lenguaje de instrucciones. BuildCraft Reborn, en cambio, usa json simple.</italic>
