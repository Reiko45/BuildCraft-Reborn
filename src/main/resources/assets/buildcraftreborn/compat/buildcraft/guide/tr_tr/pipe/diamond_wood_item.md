<lore>
Belirli itemları çekip başka bir yere taşımak mı istiyorsun? Ona güç ver ve akışlarını izle.
</lore>
<no_lore>
Tahta Elmas Taşıma Borusu, güç verildiğinde filtresiyle belirlenen seçili itemları bir envanterden çekmek için kullanılır.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="Boru Mekaniği"/>
Güç verildiğinde, filtrede seçilen itemları çeker ve bağlı diğer envanterlere/borulara taşır.
Itemlar borunun dolu yüzünden çekilir ve şeffaf yüzlere doğru taşınır.
Birden çok şeffaf yüzü varsa itemlar rastgele bir yöne gider.
Itemın gidecek hiçbir yeri yoksa çekilen itemlar borudan yere düşer.

Bazı makineler, borunun güç almasına gerek kalmadan itemları otomatik olarak doğrudan bir Tahta Elmas Taşıma Borusu'na aktarabilir (bu durumda filtre dikkate alınmaz).
Boru Tıpaları kullanmak ya da boruları farklı renklere boyamak boruların bağlanmasını engelleyebilir.

<chapter name="Filtreleme"/>
Tahta Elmas Taşıma Borusu arayüzünü açarak bir filtre ayarlayabilirsin.
İstediğin itemları yuvalara koyarak borunun neyi çekeceğini filtreleyebilirsin.
Filtrenin ne yapacağını değiştirmek için Beyaz Liste, Kara Liste ya da Sırayla seçeneklerini kullanabilirsin:
Beyaz Liste: Yalnızca seçilen itemlar çekilir.
Kara Liste: Seçilen itemlar dışındaki tüm itemlar çekilir.
Sırayla: Itemları, yukarıdaki filtre yuvalarında seçildikleri sıraya (soldan sağa) tam olarak uyarak çeker.

Genişletilmiş filtreleme seçenekleri için filtre yuvalarında bir Liste kullanılabilir.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Güç Verme"/>
Bir motor, Güç Adaptörü ya da Boru Ritimleyicisi gibi herhangi bir MJ güç kaynağından güç alabilir.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Aldığı her güç atımında bitişikteki envanterden item çeker.
Bir atımdaki güç ne kadar fazlaysa atım başına o kadar çok item çekilir.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>