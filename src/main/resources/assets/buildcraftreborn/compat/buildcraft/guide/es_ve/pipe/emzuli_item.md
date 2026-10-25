<lore>
Usar una compuerta para extraer ciertos objetos es la última revolución técnica en tecnología de filtrado.
</lore>
<no_lore>
La Tubería para Transporte de Emzuli es una tubería controlada por compuertas que sirve para extraer de un inventario los objetos que determine su filtro cuando recibe energía.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="Mecánica de la tubería"/>
Cuando recibe energía, extrae los objetos seleccionados en el filtro y los transporta a cualquier otro inventario o tubería conectado.
La extracción se produce por el lado opaco de la tubería, y los objetos se transportan hacia los lados transparentes.
Si tiene varios lados transparentes, los objetos se mueven en una dirección aleatoria.
Si los objetos extraídos no tienen adónde ir, salen de la tubería y caen al suelo.

Algunas máquinas pueden enviar objetos directamente a una Tubería de Emzuli de forma automática, sin que la tubería necesite energía (en ese caso no se respetan los filtros).
Para evitar que las tuberías se conecten, puedes usar Tapones para Tubería o pintarlas de colores distintos.

<chapter name="Filtrado"/>
La interfaz de la Tubería para Transporte de Emzuli muestra cuatro colores para cuatro opciones de filtrado independientes.
Colocando objetos en las ranuras puedes elegir lo que extrae cada filtro.
Los colores de los pinceles que hay junto a las ranuras determinan de qué color se pintarán los objetos al extraerlos, y se pueden cambiar haciendo clic en ellos.
Para elegir cuál de los cuatro filtros se extrae, hace falta colocar en ella una compuerta con una acción de extracción predefinida de color. Por ejemplo: Señal de redstone -> Extracción de rojo.

Puedes usar una Lista en una ranura de filtro para tener opciones de filtrado ampliadas.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Alimentación"/>
Puede recibir energía de cualquier fuente de MJ, como un motor, un adaptador de energía o un pulsar de tubería.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Por cada pulso de energía que recibe, extrae objetos del inventario adyacente.
Cuanta más energía tenga un pulso, más objetos se extraen por pulso.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>