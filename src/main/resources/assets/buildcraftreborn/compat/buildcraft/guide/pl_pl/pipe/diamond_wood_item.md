<lore>
Chcesz wyciągać konkretne przedmioty i przesyłać je gdzie indziej? Wystarczy dostarczyć energię i patrzeć, jak płyną.
</lore>
<no_lore>
Drewniano-diamentowa rura transportowa po zasileniu wyciąga z pojemnika przedmioty wybrane w jej filtrze.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="Mechanika rury"/>
Po zasileniu wyciąga przedmioty wybrane w filtrze i przesyła je do innych połączonych pojemników lub rur.
Przedmioty są wyciągane od pełnego boku rury i przesyłane w stronę przezroczystych boków.
Jeśli rura ma kilka przezroczystych boków, przedmioty popłyną w losowym kierunku.
Jeśli wyciągnięte przedmioty nie mają dokąd płynąć, wypadają z rury na ziemię.

Niektóre maszyny potrafią automatycznie wysyłać przedmioty bezpośrednio do drewniano-diamentowej rury transportowej, nawet gdy rura nie jest zasilana (filtr nie jest wtedy uwzględniany).
Korki do rur lub pomalowanie rur na różne kolory zapobiegają łączeniu się rur.

<chapter name="Filtrowanie"/>
Po otwarciu interfejsu drewniano-diamentowej rury transportowej możesz ustawić filtr.
Umieszczając w slotach wybrane przedmioty, określasz, co rura wyciąga.
Za pomocą opcji „Biała lista”, „Czarna lista” i „Kolejno” możesz zmienić działanie filtra:
Biała lista: wyciągane są tylko wybrane przedmioty.
Czarna lista: wyciągane są wszystkie przedmioty oprócz wybranych.
Kolejno: wyciąga przedmioty dokładnie w kolejności ustawionej w slotach filtra powyżej (od lewej do prawej)

W slocie filtra można umieścić listę, aby uzyskać rozszerzone możliwości filtrowania.
<link to="buildcraftreborn:item/list"/>

<chapter name="Zasilanie"/>
Może otrzymywać energię z dowolnego źródła MJ, takiego jak silnik, przejściówka energetyczna czy pulsar do rur.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
Przy każdym otrzymanym impulsie energii wyciąga przedmioty z sąsiedniego pojemnika.
Im więcej energii ma impuls, tym więcej przedmiotów zostanie wyciągniętych za jednym razem.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>