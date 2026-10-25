<lore>
Itemları çekip başka bir yere taşımak mı istiyorsun? Ona güç ver ve akışlarını izle.
</lore>
<no_lore>
Tahta Taşıma Borusu, güç verildiğinde bir envanterden item çekmek için kullanılır.
</no_lore>

<recipes stack="buildcraftreborn:pipe_wood_item"/>

<chapter name="Boru Mekaniği"/>
Güç verildiğinde itemları çeker ve bağlı diğer envanterlere/borulara taşır.
Itemlar borunun dolu yüzünden çekilir ve şeffaf yüzlere doğru taşınır.
Birden çok şeffaf yüzü varsa itemlar bunlar arasında rastgele dağılır.
Itemların gidecek hiçbir yeri yoksa çekilen itemlar borudan yere düşer.

Bazı makineler, borunun güç almasına gerek kalmadan itemları otomatik olarak doğrudan bir Tahta Taşıma Borusu'na aktarabilir.
Boru Tıpaları kullanmak ya da boruları farklı renklere boyamak boruların bağlanmasını engelleyebilir.

<chapter name="Güç Verme"/>
Bir motor, Güç Adaptörü ya da Boru Ritimleyicisi gibi herhangi bir MJ güç kaynağından güç alabilir.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Aldığı her güç atımında bitişikteki envanterden item çeker.
Bir atımdaki güç ne kadar fazlaysa atım başına o kadar çok item çekilir.

<usages stack="buildcraftreborn:pipe_wood_item"/>