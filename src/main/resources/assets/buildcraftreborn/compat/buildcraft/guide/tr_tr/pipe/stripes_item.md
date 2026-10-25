<lore>
Blok yerleştirebilen, blok kırabilen ve blok kullanabilen bir boru hayal et. İşte hayal ettiğin boru bu.
</lore>
<no_lore>
Stripes Taşıma Borusu, blok kırmak, blok yerleştirmek, içinden geçirilen itemları kullanmak ve onları bağlı bir boruya/envantere taşımak için kullanılır.
</no_lore>

<recipes stack="buildcraftreborn:pipe_stripes_item"/>

<chapter name="Borunun Çalışması"/>
Güç verildiğinde, hemen önündeki bloğu kırar ve bitişikteki boruya ya da envantere aktarır.
İçinden bir blok geçirildiğinde, bloğu hemen önüne yerleştirir.
İçinden bir item geçirildiğinde, onu önündeki blok/varlık üzerinde 'kullanmaya' çalışır.

<chapter name="Boru Yönü"/>
Stripes Taşıma Borusu'nun önündeki bloğu etkileyebilmesi için bir yöne 'dönük' olması gerekir.
Kendisine birden çok boru/envanter bağlıysa dünyayı etkileme yeteneğini kaybeder.

<chapter name="Boru Mekaniği"/>
Hareket eden itemların gidecek hiçbir yeri yoksa borudan yere düşerler.
Boru Tıpaları kullanmak ya da boruları farklı renklere boyamak boruların bağlanmasını engelleyebilir.

<chapter name="Güç Verme"/>
Bir motor, Güç Adaptörü ya da Boru Ritimleyicisi gibi herhangi bir MJ güç kaynağından güç alabilir.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Aldığı her güç atımında, aldığı gücü kullanarak önündeki bloğu kırmaya çalışır.
Bir atımdaki güç ne kadar fazlaysa bloğu kırmak için o kadar çok güç kullanılır.

<usages stack="buildcraftreborn:pipe_stripes_item"/>