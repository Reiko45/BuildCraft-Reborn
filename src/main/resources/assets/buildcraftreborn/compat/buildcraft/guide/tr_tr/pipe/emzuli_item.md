<lore>
Belirli itemları çekmek için bir devre kullanmak, filtreleme teknolojisindeki en son teknik devrimdir.
</lore>
<no_lore>
Emzuli Taşıma Borusu, güç verildiğinde filtresiyle belirlenen seçili itemları bir envanterden çekmek için kullanılan, devreyle kontrol edilen bir borudur.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="Boru Mekaniği"/>
Güç verildiğinde, filtrede seçilen itemları çeker ve bağlı diğer envanterlere/borulara taşır.
Itemlar borunun dolu yüzünden çekilir ve şeffaf yüzlere doğru taşınır.
Birden çok şeffaf yüzü varsa itemlar rastgele bir yöne gider.
Itemın gidecek hiçbir yeri yoksa çekilen itemlar borudan yere düşer.

Bazı makineler, borunun güç almasına gerek kalmadan itemları otomatik olarak doğrudan bir Emzuli Borusu'na aktarabilir (bu durumda filtreler dikkate alınmaz).
Boru Tıpaları kullanmak ya da boruları farklı renklere boyamak boruların bağlanmasını engelleyebilir.

<chapter name="Filtreleme"/>
Emzuli Taşıma Borusu arayüzünü açtığında, dört ayrı filtreleme seçeneği için dört renk görürsün.
Yuvalara item koyarak her filtrenin neyi çekeceğini belirleyebilirsin.
Yuvaların yanındaki boya fırçalarının renkleri, itemların çekildiğinde hangi renge boyanacağını belirler ve üzerlerine tıklanarak değiştirilebilir.
Dört filtreden hangisinin çekileceğini seçmek için boruya, renkli bir Ekstraksiyon Ön Ayarı eylemi sunan bir devre yerleştirilmesi gerekir. Örn. Kızıltaş sinyali - > Kırmızı Ekstraksiyon Ön Ayarı.

Genişletilmiş filtreleme seçenekleri için filtre yuvalarında bir Liste kullanılabilir.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Güç Verme"/>
Bir motor, Güç Adaptörü ya da Boru Ritimleyicisi gibi herhangi bir MJ güç kaynağından güç alabilir.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Aldığı her güç atımında bitişikteki envanterden item çeker.
Bir atımdaki güç ne kadar fazlaysa atım başına o kadar çok item çekilir.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>