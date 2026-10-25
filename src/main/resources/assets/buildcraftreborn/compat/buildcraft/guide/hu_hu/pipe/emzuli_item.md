<lore>
Bizonyos tárgyak kapuval történő kinyerése a szűréstechnika legújabb forradalma.
</lore>
<no_lore>
Az Emzuli szállítócső egy kapuval vezérelt cső, amely energia hatására a szűrője által kiválasztott tárgyakat nyeri ki egy tárolóból.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="A cső működése"/>
Energiát kapva kinyeri a szűrőben kiválasztott tárgyakat, és a többi csatlakoztatott tárolóba vagy csőbe szállítja őket.
A tárgyak kinyerése a cső tömör oldalán történik, és onnan az átlátszó oldalak felé haladnak.
Ha több átlátszó oldala van, a tárgyak véletlenszerű irányba haladnak.
Ha a tárgyaknak nincs hová menniük, a kinyert tárgyak kiesnek a csőből a földre.

Egyes gépek automatikusan, közvetlenül az Emzuli csőbe tudják juttatni a tárgyakat anélkül, hogy a csőnek energia kellene (ilyenkor a szűrők nem érvényesülnek).
Csődugókkal vagy a csövek eltérő színűre festésével megakadályozhatod, hogy a csövek összekapcsolódjanak.

<chapter name="Szűrés"/>
Az Emzuli szállítócső felületén négy szín tartozik négy külön szűrőhöz.
A tárgyak helyekre tételével beállíthatod, mit nyerjenek ki az egyes szűrők.
A helyek melletti festőecsetek színe határozza meg, milyen színűre festődnek a tárgyak kinyeréskor; ez az ecsetekre kattintva módosítható.
Ahhoz, hogy kiválaszd, a négy szűrő közül melyik szerint történjen a kinyerés, egy kaput kell a csőre helyezni egy színes „Extrakciós beállítás” cselekvéssel. Például: „Vöröskő jel bekapcsolva” → „Piros extrakciós beállítás”.

A szűrőhelyeken listát is használhatsz a bővebb szűrési lehetőségekért.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Energiaellátás"/>
Bármilyen MJ-energiaforrásból kaphat energiát, például motorból, energiaadapterből vagy csőpulzárból.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Minden kapott energiaimpulzusnál tárgyakat nyer ki a szomszédos tárolóból.
Minél erősebb egy impulzus, annál több tárgyat nyer ki impulzusonként.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>