Strony poradnika są pisane w podzbiorze języka markdown.

<italic>(Większość tej strony wymaga jeszcze uzupełnienia, ale oto lista podstawowych tagów)</italic>

# Proste tagi pojedyncze
Każdy tag ma postać &lt;name arguments/&gt; – gdzie name to nazwa tagu, a końcowy znak '/' oznacza, że tag występuje samodzielnie. Niektóre tagi mają też argumenty w postaci „par klucz-wartość”, na przykład:
<guide_md>
"level"="42"
</guide_md>

## Nowa strona
&lt;new_page/&gt; przenosi treść, która po nim następuje, na następną stronę.

## Odnośniki
Tworzą one odnośnik do innej strony poradnika, z tytułem i ikoną tej strony. Możesz też utworzyć odnośnik do przedmiotu (ItemStack) albo wyzwalacza lub akcji (Statement), nawet jeśli nie mają one własnej strony w poradniku.
Na przykład:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
da w wyniku:
<link to="buildcraftreborn:config/guide_entry_format"/>

lub:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
da w wyniku:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Rozdziały
&lt;chapter/&gt; tworzy „nagłówek rozdziału”, wyświetlany na liście rozdziałów jako kolorowy nagłówek. Ma jeden możliwy argument: "level", który musi być liczbą całkowitą równą 0 lub większą. Możesz też, tak jak w zwykłym markdownie, tworzyć nagłówki za pomocą kilku krzyżyków (#). Liczba znaków # (minus jeden) to poziom nagłówka.
Na przykład:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
da w wyniku:
### Rozdział poziomu 3
<chapter name="Inny rozdział poziomu 3" level="2"/>

# Proste tagi podwójne
Wszystkie te tagi muszą otaczać jakąś treść: muszą mieć zarówno tag otwierający &lt;italic&gt;, jak i zamykający &lt;/italic&gt;
Wszystkie wpływają na tekst zawarty między tagiem otwierającym a zamykającym.

- <italic>kursywa</italic>
- <bold>pogrubienie</bold>
- <underline>podkreślenie</underline>
- <strikethrough>przekreślenie</strikethrough>
- <red>czerwony</red>
- <black>czarny</black>
- <dark_blue>ciemnoniebieski</dark_blue>
- <dark_green>ciemnozielony</dark_green>
- <dark_aqua>ciemnoturkusowy</dark_aqua>
- <dark_red>ciemnoczerwony</dark_red>
- <dark_purple>ciemnofioletowy</dark_purple>
- <gold>złoty</gold>
- <gray>szary</gray>
- <dark_gray>ciemnoszary</dark_gray>
- <blue>niebieski</blue>
- <green>zielony</green> (zielony)
- <aqua>turkusowy</aqua> (turkusowy)
- <light_purple>jasnofioletowy</light_purple>
- <yellow>żółty</yellow> (żółty)
- <white>biały</white> (biały)

Lepiej nie używać koloru zielonego, turkusowego, żółtego ani białego, bo zwykle bardzo trudno je dostrzec na stronie.

# Tagi przełączane
Wszystkie poniższe tagi zależą od opcji, które gracz może włączać i wyłączać.

## Fabuła
Jeśli pole „Pokaż fabułę [ ]” jest zaznaczone, wszystko w tagu &lt;no_lore&gt; jest ukryte; w przeciwnym razie ukryte jest wszystko w tagu &lt;lore&gt;.

Tagu lore należy używać do pokazywania informacji „z perspektywy gry”, opowiadanych z punktu widzenia gracza (tak, jakby sam badał, jak wszystko działa). Tag przeciwny służy do przekazywania informacji w bardziej formalnym, encyklopedycznym tonie – krótko i rzeczowo.

W grze jest to domyślnie włączone, a w eksportach zawsze wyłączone.

## Wskazówki
Jeśli pole „Pokaż wskazówki [ ]” jest zaznaczone, wszystko w tagu &lt;no_hint&gt; jest ukryte; w przeciwnym razie ukryte jest wszystko w tagu &lt;hint&gt;.

Tagu hint należy używać do pokazywania dodatkowych „wskazówek”: do czego można użyć opisywanej rzeczy lub jak łączyć ją z innymi blokami i przedmiotami. Zasadniczo służy on do opisu „zalecanych zastosowań” różnych rzeczy. (Ponieważ samodzielne odkrywanie tego to część zabawy, nie jest to wymagane). Dlatego jest to domyślnie wyłączone, ale można to przełączać w grze, a w planach jest też przełączanie przy eksporcie.

## Szczegóły
Jeśli opcja konfiguracji klienta "guideBookEnableDetail" jest ustawiona na "true", wszystko w tagu &lt;no_detail&gt; jest ukryte; w przeciwnym razie ukryte jest wszystko w tagu &lt;detail&gt;.

Tagu detail należy używać do pokazywania wszystkich liczb używanych w różnych obliczeniach, takich jak przepustowość rur, szybkość wyciągania, częstotliwość impulsów itp.

Domyślnie jest to wyłączone, ale można to przełączać w grze, a w planach jest też przełączanie przy eksporcie.

# Różne
## Receptura (pojedyncza)
Ten tag pokazuje pierwszą znalezioną recepturę dla podanego stosu przedmiotów. Na przykład:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Receptury
Ten tag pokazuje wszystkie receptury dla podanego stosu przedmiotów. Na przykład:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Zastosowania
Podobnie jak przy recepturach, ten tag pokazuje wszystkie zastosowania podanego stosu przedmiotów. Na przykład:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Receptury i zastosowania
Ten tag w praktyce łączy dwa powyższe tagi w jeden, na przykład:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Grupa
Pokazuje grupy odpowiednie dla podanego przedmiotu, wyzwalacza, akcji itp.

## Obrazy
Pokazuje obraz. Ma 3 argumenty:
* src: lokalizacja źródłowa obrazu.
* width: szerokość, z jaką obraz zostanie wyświetlony. Domyślnie jest to szerokość obrazu źródłowego (choć może to nie być to, czego chcesz)
* height: wysokość, z jaką obraz zostanie wyświetlony.

Na przykład:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
