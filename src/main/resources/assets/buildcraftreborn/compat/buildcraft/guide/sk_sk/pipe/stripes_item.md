<lore>
Predstavte si rúru, ktorá dokáže bloky pokladať, rozbíjať aj používať. Toto je presne tá rúra, ktorú si predstavujete.
</lore>
<no_lore>
Prúžkovaná transportačná rúra slúži na rozbíjanie a pokladanie blokov a používanie predmetov, ktoré cez ňu prechádzajú, a na ich prepravu do pripojenej rúry či inventára.
</no_lore>

<recipes stack="buildcraftreborn:pipe_stripes_item"/>

<chapter name="Činnosť rúry"/>
Keď je napájaná, rozbije blok priamo pred sebou a odovzdá ho do susednej rúry alebo inventára.
Keď cez ňu prechádza blok, položí ho priamo pred seba.
Keď cez ňu prechádza predmet, pokúsi sa ho „použiť“ na blok alebo entitu pred sebou.

<chapter name="Smer rúry"/>
Prúžkovaná transportačná rúra musí „mieriť“ určitým smerom, aby mohla pôsobiť na blok pred sebou.
Ak je k nej pripojených viacero rúr či inventárov, stratí schopnosť pôsobiť na svet.

<chapter name="Mechanika rúry"/>
Ak predmety nemajú kam ísť, vypadnú z rúry na zem.
Spájaniu rúr zabránite pomocou Napojení rúr alebo zafarbením rúr inou farbou.

<chapter name="Napájanie"/>
Energiu môže prijímať z akéhokoľvek zdroja MJ, napríklad z motora, Energetického adaptéra alebo Potrubného pulzara.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Pri každom prijatom energetickom pulze využije prijatú energiu a pokúsi sa rozbiť blok pred sebou.
Čím viac energie pulz nesie, tým viac energie sa použije na rozbitie bloku.

<usages stack="buildcraftreborn:pipe_stripes_item"/>