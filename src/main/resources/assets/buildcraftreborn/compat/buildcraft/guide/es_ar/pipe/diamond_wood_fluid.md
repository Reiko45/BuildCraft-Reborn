<lore>
¿Quieres extraer un líquido concreto y llevarlo a otro sitio? Solo tienes que darle energía y verlo fluir.
</lore>
<no_lore>
La Tubería para líquidos de madera y diamante sirve para extraer de un tanque los líquidos seleccionados cuando recibe energía.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_fluid"/>

<chapter name="Mecánica de la tubería"/>
Cuando recibe energía, extrae el líquido seleccionado en el filtro y lo envía a cualquier otro tanque o tubería conectado.
La extracción se produce por el lado opaco de la tubería, y los líquidos salen hacia los lados transparentes.
Si tiene varios lados transparentes, los líquidos se reparten a partes iguales en cada dirección.

Algunas máquinas pueden enviar líquidos directamente a ella sin que necesite energía (en ese caso no se respeta el filtro).
Para evitar que las tuberías se conecten, puedes usar Tapones para Tubería o pintarlas de colores distintos.

<chapter name="Filtrado"/>
Al abrir la interfaz de la Tubería para líquidos de madera y diamante, puedes configurar un filtro.
Si colocas en las ranuras un cubo (u otro recipiente de líquido) con el líquido que quieras, podrás filtrar lo que extrae.
Puedes usar las opciones Lista blanca y Lista negra para cambiar el funcionamiento del filtro:
Lista blanca: solo se extraen los líquidos seleccionados.
Lista negra: se extrae cualquier líquido excepto el seleccionado.

<chapter name="Alimentación"/>
Puede recibir energía de cualquier fuente de MJ, como un motor, un adaptador de energía o un pulsar de tubería.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Por cada pulso de energía que recibe, extrae líquido del tanque adyacente.
Cuanta más energía tenga un pulso, más líquido se extrae por pulso.

<no_detail>
La Tubería para líquidos de madera y diamante transporta líquidos a las tuberías y tanques adyacentes a 1.6 cubos por segundo.
Las tuberías para líquidos tienen un tanque interno que puede contener 1 cubo por lado y 1 cubo en el centro.
</no_detail>

<usages stack="buildcraftreborn:pipe_diamond_wood_fluid"/>