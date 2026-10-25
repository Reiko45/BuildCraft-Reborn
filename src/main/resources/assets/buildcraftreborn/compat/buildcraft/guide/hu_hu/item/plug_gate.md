<lore>
A vöröskő manapság rengeteg helyet foglal. Miért ne irányíthatnád a rendszeredet pontosabban, kisebb helyen, csövek segítségével? Csak tegyél egy kaput egy csőre, és kezdj el gondolkodni.
</lore>
<no_lore>
A kapu egy fejleszthető csőkiegészítő, amely logisztikai keretet ad a csőrendszered kezeléséhez.
</no_lore>

<recipes stack="buildcraftreborn:plug_gate"/>

<chapter name="A kapu elhelyezése"/>
A kapu csőkiegészítő, ezért csak csövekre helyezhető, de a cső bármelyik oldalára.
Az azon az oldalon mellé helyezett csövek nem csatlakoznak.

<chapter name="Kapuszintek"/>
A kapuknak különböző szintjei vannak, amelyek eltérő számú helyet biztosítanak:
Egyszerű kapu – 1 helykészlet (nem módosítható).
Vas kapu – 2 helykészlet.
Alvilági tégla kapu – 4 helykészlet.
Arany kapu – 8 helykészlet.

<chapter name="Kapumódosítók"/>
A kapuk módosíthatók, hogy paraméterhelyeket kapjanak:
Lazurit bővítés – minden triggerhez 1 paraméterhely.
Kvarc bővítés – minden készlethez 1 paraméterhely (a kapu helyeinek számát a felére csökkenti).
Gyémánt bővítés – minden készlethez 3 paraméterhely (a kapu helyeinek számát a felére csökkenti).

<chapter name="Kapuváltozatok"/>
Az ÉS kapu csak akkor aktiválja a cselekvések felé vezető kapcsolatot, ha <bold>az összes</bold> csatlakoztatott trigger aktív.
A VAGY kapu akkor aktiválja a cselekvések felé vezető kapcsolatot, ha <bold>legalább egy</bold> csatlakoztatott trigger aktív.

<chapter name="Triggerek"/>
A bal oldali ikonokat triggereknek nevezzük. Ezeket a felület bal oldalán lévő helyekre kell húzni.
A különböző triggerek különböző dolgokat érzékelnek, mindegyik más-más feltétel szerint. Ha egy trigger feltétele teljesül, a mellette lévő kapcsolat aktívvá válik.

<chapter name="Cselekvések"/>
A jobb oldali ikonokat cselekvéseknek nevezzük. Ezeket a felület jobb oldalán lévő helyekre kell húzni.
A különböző cselekvések különböző feladatokat hajtanak végre. Ha a mellettük lévő kapcsolat aktív, a csatlakoztatott cselekvések végrehajtódnak.

<chapter name="Paraméterek"/>
Ha paraméterhelyek vannak telepítve, egyes triggerek és cselekvések mellett további helyek válnak elérhetővé.
Ezek a további helyek extra funkciókat nyújtanak, hogy pontosabban beállíthasd a triggereidet és cselekvéseidet.
Ezeknél a helyeknél vagy rákattintasz a helyre további beállításokért, vagy tárgyakat teszel bele szűrőként.

<chapter name="Kapcsolatok"/>
A középen futó vonalak kötik össze a triggereket a cselekvésekkel, hogy együtt működhessenek.
Rájuk kattintva összekötheted vagy szétválaszthatod őket, például hogy több trigger teljesülését követeld meg, vagy több cselekvést indíts el.
Ha egy trigger teljesül, a középső vonal felvilágít (csak részben, ha több trigger szükséges).
Ha egy vonal világít, a hozzá kapcsolt cselekvések végrehajtódnak.

<usages stack="buildcraftreborn:plug_gate"/>