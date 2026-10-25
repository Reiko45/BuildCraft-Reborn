<lore>
Hangi itemların nereye gidebileceğini seçmek sistemin için faydalı olabilir.
</lore>
<no_lore>
Elmas Taşıma Borusu, itemları bir borudan, filtresiyle belirlenen yönde bitişikteki başka bir boruya ya da envantere taşımak için kullanılır.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_item"/>

<chapter name="Filtreyi Ayarlama"/>
Elmas Taşıma Borusu'nun her yüzü farklı bir renkle ayırt edilir.
Elmas Taşıma Borusu arayüzünü açarak bir filtre ayarlayabilirsin.
İstediğin itemları belirli bir rengin yuvasına koyarak hangi itemların hangi yöne gidebileceğini filtreleyebilirsin.
Bir renge aynı itemdan birden fazla koyarak itemlarının nereye gideceğine 'ağırlık' ekleyebilirsin.

Genişletilmiş filtreleme seçenekleri için filtre yuvalarında bir Liste kullanılabilir.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Boru Mekaniği"/>
Itemların gidebileceği birden çok yön varsa, itemlar (onlara verdiğin ağırlığa bağlı olarak) bu yönler arasında dağıtılır.
Filtre ayarlanmamış her geçerli yön her itemın geçmesine izin verir, ancak filtrelenmiş yönler önceliklidir.
Itemlar geldikleri yöne geri gitmez, yalnızca farklı bir yöne ilerler.
Bağlı bir boru yoksa ya da bitişikteki envanter doluysa ve itemın gidebileceği başka bir yer yoksa item borudan yere düşer.

Bazı makineler, bir ekstraksiyon borusundan geçirilmeye gerek kalmadan itemları otomatik olarak doğrudan bir Elmas Taşıma Borusu'na aktarabilir.
Boru Tıpaları kullanmak ya da boruları farklı renklere boyamak boruların bağlanmasını engelleyebilir.

<usages stack="buildcraftreborn:pipe_diamond_item"/>