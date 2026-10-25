Todas las páginas de la guía están escritas en un subconjunto de markdown.

<italic>(Gran parte de esta página aún está por completar, pero aquí tienes una lista de etiquetas básicas)</italic>

# Etiquetas simples individuales
Todas las etiquetas tienen la forma &lt;name arguments/&gt;, donde name es el nombre de la etiqueta y la '/' final indica que la etiqueta va sola. Algunas etiquetas también tienen argumentos en forma de "pares clave-valor", por ejemplo:
<guide_md>
"level"="42"
</guide_md>

## Nueva página
&lt;new_page/&gt; mueve a la página siguiente el contenido que va después.

## Enlaces
Crean un enlace a otra página del libro guía, usando el título y el icono de la página enlazada. También puedes crear un enlace a un ItemStack o a un Statement aunque no tengan una página propia en el libro guía.
Por ejemplo:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
dará como resultado:
<link to="buildcraftreborn:config/guide_entry_format"/>

o:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
dará como resultado:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Capítulos
&lt;chapter/&gt; crea un "encabezado de capítulo", que aparece en la lista de capítulos como un encabezado de color. Admite un argumento: "level", que debe ser un número entero igual o mayor que 0. También puedes usar varios símbolos de numeral (#), como en el formato markdown normal, para crear encabezados. El número de # (menos uno) equivale al nivel del encabezado.
Por ejemplo:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
dará como resultado:
### Capítulo de nivel 3
<chapter name="Otro capítulo de nivel 3" level="2"/>

# Etiquetas simples dobles
Todas estas deben rodear algún contenido y necesitan tanto una etiqueta de apertura &lt;italic&gt; como una de cierre &lt;/italic&gt;.
Todas afectan al texto que queda entre la etiqueta de apertura y la de cierre.

- <italic>cursiva</italic> (italic)
- <bold>negrita</bold> (bold)
- <underline>subrayado</underline> (underline)
- <strikethrough>tachado</strikethrough> (strikethrough)
- <red>rojo</red> (red)
- <black>negro</black> (black)
- <dark_blue>azul oscuro</dark_blue> (dark_blue)
- <dark_green>verde oscuro</dark_green> (dark_green)
- <dark_aqua>aguamarina oscuro</dark_aqua> (dark_aqua)
- <dark_red>rojo oscuro</dark_red> (dark_red)
- <dark_purple>morado oscuro</dark_purple> (dark_purple)
- <gold>dorado</gold> (gold)
- <gray>gris</gray> (gray)
- <dark_gray>gris oscuro</dark_gray> (dark_gray)
- <blue>azul</blue> (blue)
- <green>verde</green> (green)
- <aqua>aguamarina</aqua> (aqua)
- <light_purple>morado claro</light_purple> (light_purple)
- <yellow>amarillo</yellow> (yellow)
- <white>blanco</white> (white)

No es buena idea usar verde, aguamarina, amarillo ni blanco, porque en general cuesta mucho distinguir esos colores en la página.

# Etiquetas activables
Todas las etiquetas siguientes dependen de opciones, y el jugador puede activarlas o desactivarlas.

## Trasfondo
Si la casilla "Mostrar historia [ ]" está marcada, se oculta todo lo que haya en la etiqueta &lt;no_lore&gt;; si no, se oculta todo lo que haya en la etiqueta &lt;lore&gt;.

La etiqueta lore debe usarse para mostrar información de la guía "dentro del juego", narrada desde la perspectiva del jugador (como si estuviera investigando activamente cómo funciona todo). La otra sirve para un tono más formal, al estilo de una wiki, que transmite la información sin más.

Esta opción está activada de forma predeterminada en el juego y siempre desactivada en las exportaciones.

## Pistas
Si la casilla "Mostrar pistas [ ]" está marcada, se oculta todo lo que haya en la etiqueta &lt;no_hint&gt;; si no, se oculta todo lo que haya en la etiqueta &lt;hint&gt;.

La etiqueta hint debe usarse para mostrar "pistas" adicionales sobre para qué puede servir lo que se describe, o distintas formas de usarlo en combinación con otros bloques u objetos. Básicamente, sirve para los "usos recomendados" de las cosas. (Como descubrirlo forma parte de la diversión, no es obligatorio). Por eso está desactivada de forma predeterminada, pero se puede activar en el juego y está previsto que también se pueda activar al exportar.

## Detalle
Si la opción de configuración del cliente "guideBookEnableDetail" está en "true", se oculta todo lo que haya en la etiqueta &lt;no_detail&gt;; si no, se oculta todo lo que haya en la etiqueta &lt;detail&gt;.

La etiqueta detail debe usarse para mostrar todas las cifras que intervienen en el cálculo de varias cosas, como el caudal de las tuberías, la velocidad de extracción, la frecuencia de pulsos, etc.

Está desactivada de forma predeterminada, pero se puede activar en el juego y está previsto que también se pueda activar al exportar.

# Varios
## Receta (singular)
Esta etiqueta muestra la primera receta encontrada para la pila de objetos indicada. Por ejemplo:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Recetas
Esta etiqueta muestra todas las recetas de la pila de objetos indicada. Por ejemplo:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Usos
Al igual que recipes, muestra todos los usos de la pila de objetos indicada. Por ejemplo:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Recetas y usos
En la práctica, combina las dos etiquetas anteriores en una sola, por ejemplo:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Grupo
Muestra los grupos correspondientes al objeto, statement, etc. indicado.

## Imágenes
Muestra una imagen. Tiene 3 argumentos:
* src: la ubicación de origen de la imagen.
* width: el ancho con el que se mostrará la imagen. De forma predeterminada es el ancho de la imagen de origen (aunque puede que no sea lo que quieres).
* height: la altura con la que se mostrará la imagen.

Por ejemplo:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
