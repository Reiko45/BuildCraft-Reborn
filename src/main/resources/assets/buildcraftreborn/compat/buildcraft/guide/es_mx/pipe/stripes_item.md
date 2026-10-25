<lore>
Imagina una tubería que puede colocar bloques, romperlos y usarlos. Esta es la tubería que estás imaginando.
</lore>
<no_lore>
La Tubería para Transporte Rayada sirve para romper bloques, colocar bloques, usar los objetos que pasan por ella y transportarlos a una tubería o inventario conectado.
</no_lore>

<recipes stack="buildcraftreborn:pipe_stripes_item"/>

<chapter name="Funcionamiento de la tubería"/>
Cuando recibe energía, rompe el bloque que tiene justo delante y lo pasa a la tubería o inventario adyacente.
Cuando le llega un bloque, lo coloca justo delante de ella.
Cuando le llega un objeto, intenta 'usarlo' sobre el bloque o la entidad que tiene delante.

<chapter name="Dirección de la tubería"/>
Una Tubería para Transporte Rayada tiene que estar 'apuntando' en una dirección para actuar sobre el bloque que tiene delante.
Si tiene varias tuberías o inventarios conectados, pierde la capacidad de actuar sobre el mundo.

<chapter name="Mecánica de la tubería"/>
Si los objetos que viajan no tienen adónde ir, salen de la tubería y caen al suelo.
Para evitar que las tuberías se conecten, puedes usar Tapones para Tubería o pintarlas de colores distintos.

<chapter name="Alimentación"/>
Puede recibir energía de cualquier fuente de MJ, como un motor, un adaptador de energía o un pulsar de tubería.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Por cada pulso de energía que recibe, usa esa energía para intentar romper el bloque que tiene delante.
Cuanta más energía tenga un pulso, más energía se usa para romper el bloque.

<usages stack="buildcraftreborn:pipe_stripes_item"/>