<lore>
Una receta, doblada y enviada por correo.
</lore>
<no_lore>
El Empaquetador de recetas guarda los ingredientes de una receta de fabricación en un paquete, listo para que una Mesa de estampado lo fabrique.
</no_lore>
<chapter name="El patrón"/>
La cuadrícula de 3x3 es la cuadrícula de fabricación de la receta. Haz clic en una ranura con un objeto para que esa ranura pida ese objeto. Haz clic en una ranura vacía con la mano vacía para que pida cualquier cosa (se ilumina), y vuelve a hacer clic para desactivarla. Si haces clic con la mano vacía en una ranura que pide un objeto, se borra.
Todas las ranuras que piden "cualquier cosa" reciben el mismo objeto, uno que ninguna otra ranura pida por su nombre.
<recipes_usages stack="buildcraftreborn:packager"/>
<chapter name="Empaquetado"/>
Pon papel en la ranura de arriba a la derecha. Cada cuarto de segundo, el empaquetador toma un objeto por cada ranura iluminada, primero de sus nueve ranuras de almacenamiento y luego de los inventarios de sus cuatro lados, y convierte una hoja de papel en un paquete con cada objeto en su ranura. El paquete sale por la ranura que hay debajo del papel, y el empaquetador espera hasta que alguien lo retire.
También puedes volver a meter un paquete en lugar de papel para llenar sus ranuras vacías: el empaquetador no pondrá nada donde el paquete ya tenga algo.
<chapter name="Automatización"/>
Las tuberías introducen ingredientes en las ranuras de almacenamiento por los lados (solo objetos que completen una pila que ya esté allí o que el patrón pueda usar), papel o paquetes por arriba o por abajo, y extraen paquetes por cualquier lado. Los robots repartidores pueden llevar los paquetes a una Mesa de estampado.
