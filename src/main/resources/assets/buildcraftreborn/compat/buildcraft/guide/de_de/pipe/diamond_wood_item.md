<lore>
Du willst bestimmte Gegenstände herausholen und woanders hinbringen? Gib dem Rohr einfach Energie und sieh zu, wie sie fließen.
</lore>
<no_lore>
Ein Hölzernes Diamant-Transportrohr extrahiert die durch seinen Filter bestimmten Gegenstände aus einem Inventar, wenn es Energie erhält.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="Funktionsweise des Rohrs"/>
Mit Energie versorgt, extrahiert es die im Filter gewählten Gegenstände und transportiert sie zu allen anderen angeschlossenen Inventaren oder Rohren.
Die Gegenstände werden an der massiven Seite des Rohrs entnommen und zu den durchsichtigen Seiten transportiert.
Hat es mehrere durchsichtige Seiten, wandern die Gegenstände in eine zufällige Richtung.
Können die extrahierten Gegenstände nirgendwohin, fallen sie aus dem Rohr auf den Boden.

Manche Maschinen können Gegenstände automatisch direkt in ein Hölzernes Diamant-Transportrohr ausgeben, ohne dass das Rohr Energie braucht (dabei wird der Filter nicht beachtet).
Mit Rohrstöpseln oder durch unterschiedliches Färben kannst du verhindern, dass sich Rohre verbinden.

<chapter name="Filtern"/>
In der Oberfläche des Hölzernen Diamant-Transportrohrs kannst du einen Filter einrichten.
Lege die gewünschten Gegenstände in die Slots, um festzulegen, was extrahiert wird.
Mit den Optionen „Whitelist“, „Blacklist“ und „Round Robin“ legst du fest, was dein Filter tut:
Whitelist: Nur die gewählten Gegenstände werden extrahiert.
Blacklist: Alle Gegenstände außer den gewählten werden extrahiert.
Round Robin: Extrahiert die Gegenstände genau in der Reihenfolge der Filterslots oben (von links nach rechts).

Eine Liste kann in einen Filterslot gelegt werden, um die Filtermöglichkeiten zu erweitern.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Energieversorgung"/>
Es kann Energie von jeder MJ-Energiequelle erhalten, etwa von einem Motor, einem Energieadapter oder einem Rohrpulsar.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Bei jedem Energiepuls, den es erhält, extrahiert es Gegenstände aus dem angrenzenden Inventar.
Je mehr Energie ein Puls hat, desto mehr Gegenstände werden pro Puls extrahiert.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>