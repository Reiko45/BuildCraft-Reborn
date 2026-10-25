<lore>
Wyciąganie konkretnych przedmiotów za pomocą bramki to najnowsza rewolucja w technologii filtrowania.
</lore>
<no_lore>
Emzuli rura transportowa to sterowana bramką rura, która po zasileniu wyciąga z pojemnika przedmioty wybrane w jej filtrze.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="Mechanika rury"/>
Po zasileniu wyciąga przedmioty wybrane w filtrze i przesyła je do innych połączonych pojemników lub rur.
Przedmioty są wyciągane od pełnego boku rury i przesyłane w stronę przezroczystych boków.
Jeśli rura ma kilka przezroczystych boków, przedmioty popłyną w losowym kierunku.
Jeśli wyciągnięte przedmioty nie mają dokąd płynąć, wypadają z rury na ziemię.

Niektóre maszyny potrafią automatycznie wysyłać przedmioty bezpośrednio do rury Emzuli, nawet gdy rura nie jest zasilana (filtry nie są wtedy uwzględniane).
Korki do rur lub pomalowanie rur na różne kolory zapobiegają łączeniu się rur.

<chapter name="Filtrowanie"/>
Interfejs Emzuli rury transportowej zawiera cztery kolory odpowiadające czterem osobnym filtrom.
Umieszczając przedmioty w slotach, określasz, co wyciąga każdy filtr.
Kolory pędzli obok slotów określają, na jaki kolor przedmioty zostaną pomalowane po wyciągnięciu; można je zmienić, klikając na nie.
Aby wybrać, który z czterech filtrów jest używany, na rurze musi znajdować się bramka z akcją „Ustawienia ekstrakcji” w danym kolorze. Np. sygnał redstone -> „Czerwone ustawienia ekstrakcji”.

W slocie filtra można umieścić listę, aby uzyskać rozszerzone możliwości filtrowania.
<link to="buildcraftreborn:item/list"/>

<chapter name="Zasilanie"/>
Może otrzymywać energię z dowolnego źródła MJ, takiego jak silnik, przejściówka energetyczna czy pulsar do rur.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
Przy każdym otrzymanym impulsie energii wyciąga przedmioty z sąsiedniego pojemnika.
Im więcej energii ma impuls, tym więcej przedmiotów zostanie wyciągniętych za jednym razem.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>