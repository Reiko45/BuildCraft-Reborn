<lore>
¿Quieres extraer ciertos objetos y llevarlos a otro sitio? Solo tienes que darle energía y verlos fluir.
</lore>
<no_lore>
La Tubería para transporte de madera y diamante sirve para extraer de un inventario los objetos que determine su filtro cuando recibe energía.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="Mecánica de la tubería"/>
Cuando recibe energía, extrae los objetos seleccionados en el filtro y los transporta a cualquier otro inventario o tubería conectado.
La extracción se produce por el lado opaco de la tubería, y los objetos se transportan hacia los lados transparentes.
Si tiene varios lados transparentes, los objetos se mueven en una dirección aleatoria.
Si los objetos extraídos no tienen adónde ir, salen de la tubería y caen al suelo.

Algunas máquinas pueden enviar objetos directamente a una Tubería para transporte de madera y diamante de forma automática, sin que la tubería necesite energía (en ese caso no se respeta el filtro).
Para evitar que las tuberías se conecten, puedes usar Tapones para Tubería o pintarlas de colores distintos.

<chapter name="Filtrado"/>
Al abrir la interfaz de la Tubería para transporte de madera y diamante, puedes configurar un filtro.
Si colocas en las ranuras los objetos que quieras, podrás filtrar lo que extrae.
Puedes usar las opciones Lista blanca, Lista negra o Rotación para cambiar el funcionamiento del filtro:
Lista blanca: solo se extraen los objetos seleccionados.
Lista negra: se extrae cualquier objeto excepto los seleccionados.
Rotación: extrae los objetos en el orden exacto en que están en las ranuras de filtro de arriba (de izquierda a derecha).

Puedes usar una Lista en una ranura de filtro para tener opciones de filtrado ampliadas.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Alimentación"/>
Puede recibir energía de cualquier fuente de MJ, como un motor, un adaptador de energía o un pulsar de tubería.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Por cada pulso de energía que recibe, extrae objetos del inventario adyacente.
Cuanta más energía tenga un pulso, más objetos se extraen por pulso.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>