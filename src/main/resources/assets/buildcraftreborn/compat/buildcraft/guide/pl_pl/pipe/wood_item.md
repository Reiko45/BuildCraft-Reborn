<lore>
Chcesz wyciągać przedmioty i przesyłać je gdzie indziej? Wystarczy dostarczyć energię i patrzeć, jak płyną.
</lore>
<no_lore>
Drewniana rura transportowa po zasileniu wyciąga przedmioty z pojemnika.
</no_lore>

<recipes stack="buildcraftreborn:pipe_wood_item"/>

<chapter name="Mechanika rury"/>
Po zasileniu wyciąga przedmioty i przesyła je do innych połączonych pojemników lub rur.
Przedmioty są wyciągane od pełnego boku rury i przesyłane w stronę przezroczystych boków.
Jeśli rura ma kilka przezroczystych boków, przedmioty będą losowo rozdzielane między nie.
Jeśli wyciągnięte przedmioty nie mają dokąd płynąć, wypadają z rury na ziemię.

Niektóre maszyny potrafią automatycznie wysyłać przedmioty bezpośrednio do drewnianej rury transportowej, nawet gdy rura nie jest zasilana.
Korki do rur lub pomalowanie rur na różne kolory zapobiegają łączeniu się rur.

<chapter name="Zasilanie"/>
Może otrzymywać energię z dowolnego źródła MJ, takiego jak silnik, przejściówka energetyczna czy pulsar do rur.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
Przy każdym otrzymanym impulsie energii wyciąga przedmioty z sąsiedniego pojemnika.
Im więcej energii ma impuls, tym więcej przedmiotów zostanie wyciągniętych za jednym razem.

<usages stack="buildcraftreborn:pipe_wood_item"/>