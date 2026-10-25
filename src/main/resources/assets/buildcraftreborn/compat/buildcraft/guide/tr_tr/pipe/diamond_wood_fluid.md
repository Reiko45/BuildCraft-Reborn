<lore>
Belirli bir sıvıyı çekip başka bir yere taşımak mı istiyorsun? Ona güç ver ve akışını izle.
</lore>
<no_lore>
Tahta Elmas Sıvı Borusu, güç verildiğinde bir tanktan seçilen sıvıları çekmek için kullanılır.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_fluid"/>

<chapter name="Boru Mekaniği"/>
Güç verildiğinde, filtrede seçilen sıvıyı çekip bağlı diğer tanklara/borulara gönderir.
Sıvı, borunun dolu yüzünden çekilir ve şeffaf yüzlere doğru gönderilir.
Birden çok şeffaf yüzü varsa sıvılar her yöne eşit olarak bölünür.

Bazı makineler, güç gerekmeden sıvıları doğrudan bu boruya aktarabilir (bu durumda filtre dikkate alınmaz).
Boru Tıpaları kullanmak ya da boruları farklı renklere boyamak boruların bağlanmasını engelleyebilir.

<chapter name="Filtreleme"/>
Tahta Elmas Sıvı Borusu arayüzünü açarak bir filtre ayarlayabilirsin.
İstediğin sıvıyla dolu bir kovayı (ya da başka bir sıvı kabını) yuvalara koyarak borunun neyi çekeceğini filtreleyebilirsin.
Filtrenin ne yapacağını değiştirmek için Beyaz Liste ve Kara Liste seçeneklerini kullanabilirsin:
Beyaz Liste: Yalnızca seçilen sıvılar çekilir.
Kara Liste: Seçilen sıvı dışındaki her sıvı çekilir.

<chapter name="Güç Verme"/>
Bir motor, Güç Adaptörü ya da Boru Ritimleyicisi gibi herhangi bir MJ güç kaynağından güç alabilir.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Aldığı her güç atımında bitişikteki tanktan sıvı çeker.
Bir atımdaki güç ne kadar fazlaysa atım başına o kadar çok sıvı çekilir.

<no_detail>
Tahta Elmas Sıvı Borusu, sıvıları bitişikteki borulara ve tanklara saniyede 1,6 kova hızında taşır.
Sıvı borularının, her yüz için 1 kova ve merkezde 1 kova tutabilen bir iç sıvı tankı vardır.
</no_detail>

<usages stack="buildcraftreborn:pipe_diamond_wood_fluid"/>