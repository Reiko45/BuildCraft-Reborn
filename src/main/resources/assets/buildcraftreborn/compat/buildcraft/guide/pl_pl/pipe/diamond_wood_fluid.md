<lore>
Chcesz wyciągnąć konkretną ciecz i przesłać ją gdzie indziej? Wystarczy dostarczyć energię i patrzeć, jak płynie.
</lore>
<no_lore>
Drewniano-diamentowa wodoszczelna rura po zasileniu wyciąga wybrane ciecze ze zbiornika.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_fluid"/>

<chapter name="Mechanika rury"/>
Po zasileniu wyciąga ciecz wybraną w filtrze do innych połączonych zbiorników lub rur.
Ciecze są wyciągane od pełnego boku rury w stronę przezroczystych boków.
Jeśli rura ma kilka przezroczystych boków, ciecze rozdzielą się równo na każdy kierunek.

Niektóre maszyny potrafią wysyłać ciecze bezpośrednio do tej rury bez zasilania (filtr nie jest wtedy uwzględniany).
Korki do rur lub pomalowanie rur na różne kolory zapobiegają łączeniu się rur.

<chapter name="Filtrowanie"/>
Po otwarciu interfejsu drewniano-diamentowej wodoszczelnej rury możesz ustawić filtr.
Umieszczając w slotach wiadro (lub inny pojemnik na ciecz) z wybraną cieczą, określasz, co rura wyciąga.
Za pomocą przycisków „Biała lista” i „Czarna lista” możesz zmienić działanie filtra:
Biała lista: wyciągane są tylko wybrane ciecze.
Czarna lista: wyciągana jest każda ciecz oprócz wybranej.

<chapter name="Zasilanie"/>
Może otrzymywać energię z dowolnego źródła MJ, takiego jak silnik, przejściówka energetyczna czy pulsar do rur.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
Przy każdym otrzymanym impulsie energii wyciąga ciecz z sąsiedniego zbiornika.
Im więcej energii ma impuls, tym więcej cieczy zostanie wyciągnięte za jednym razem.

<no_detail>
Drewniano-diamentowa wodoszczelna rura przesyła ciecze do sąsiednich rur i zbiorników z prędkością 1,6 wiadra na sekundę.
Rury wodoszczelne mają wewnętrzny zbiornik, który mieści 1 wiadro na każdą stronę i 1 wiadro w środku.
</no_detail>

<usages stack="buildcraftreborn:pipe_diamond_wood_fluid"/>