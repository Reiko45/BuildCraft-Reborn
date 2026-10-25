<lore>
Szeretnél kinyerni bizonyos tárgyakat, és máshová szállítani őket? Csak adj neki energiát, és nézd, ahogy áramlanak.
</lore>
<no_lore>
A fa-gyémánt szállítócső energia hatására a szűrője által kiválasztott tárgyakat nyeri ki egy tárolóból.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="A cső működése"/>
Energiát kapva kinyeri a szűrőben kiválasztott tárgyakat, és a többi csatlakoztatott tárolóba vagy csőbe szállítja őket.
A tárgyak kinyerése a cső tömör oldalán történik, és onnan az átlátszó oldalak felé haladnak.
Ha több átlátszó oldala van, a tárgyak véletlenszerű irányba haladnak.
Ha a tárgyaknak nincs hová menniük, a kinyert tárgyak kiesnek a csőből a földre.

Egyes gépek automatikusan, közvetlenül a fa-gyémánt szállítócsőbe tudják juttatni a tárgyakat anélkül, hogy a csőnek energia kellene (ilyenkor a szűrő nem érvényesül).
Csődugókkal vagy a csövek eltérő színűre festésével megakadályozhatod, hogy a csövek összekapcsolódjanak.

<chapter name="Szűrés"/>
A fa-gyémánt szállítócső felületét megnyitva beállíthatsz egy szűrőt.
Ha a kívánt tárgyakat a helyekre teszed, megszűrheted, mit nyerjen ki.
A „Fehérlista”, a „Feketelista” és a „Körbeforgó” lehetőséggel módosíthatod a szűrő működését:
Fehérlista: csak a kiválasztott tárgyakat nyeri ki.
Feketelista: a kiválasztottakon kívül minden tárgyat kinyer.
Körbeforgó: a tárgyakat pontosan a fenti szűrőhelyeken beállított sorrendben nyeri ki (balról jobbra).

A szűrőhelyeken listát is használhatsz a bővebb szűrési lehetőségekért.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Energiaellátás"/>
Bármilyen MJ-energiaforrásból kaphat energiát, például motorból, energiaadapterből vagy csőpulzárból.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Minden kapott energiaimpulzusnál tárgyakat nyer ki a szomszédos tárolóból.
Minél erősebb egy impulzus, annál több tárgyat nyer ki impulzusonként.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>