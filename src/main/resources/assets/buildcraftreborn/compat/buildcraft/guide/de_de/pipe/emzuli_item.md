<lore>
Mit einem Gatter bestimmte Gegenstände zu extrahieren ist die neueste technische Revolution der Filtertechnik.
</lore>
<no_lore>
Ein Emzuli-Transportrohr ist ein gattergesteuertes Rohr, das die durch seine Filter bestimmten Gegenstände aus einem Inventar extrahiert, wenn es Energie erhält.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="Funktionsweise des Rohrs"/>
Mit Energie versorgt, extrahiert es die im Filter gewählten Gegenstände und transportiert sie zu allen anderen angeschlossenen Inventaren oder Rohren.
Die Gegenstände werden an der massiven Seite des Rohrs entnommen und zu den durchsichtigen Seiten transportiert.
Hat es mehrere durchsichtige Seiten, wandern die Gegenstände in eine zufällige Richtung.
Können die extrahierten Gegenstände nirgendwohin, fallen sie aus dem Rohr auf den Boden.

Manche Maschinen können Gegenstände automatisch direkt in ein Emzuli-Rohr ausgeben, ohne dass das Rohr Energie braucht (dabei werden die Filter nicht beachtet).
Mit Rohrstöpseln oder durch unterschiedliches Färben kannst du verhindern, dass sich Rohre verbinden.

<chapter name="Filtern"/>
Die Oberfläche des Emzuli-Transportrohrs zeigt vier Farben für vier getrennte Filteroptionen.
Lege Gegenstände in die Slots, um festzulegen, was jeder Filter extrahiert.
Die Farben der Pinsel neben den Slots bestimmen, in welcher Farbe die Gegenstände beim Extrahieren gefärbt werden; du kannst sie durch Anklicken ändern.
Um auszuwählen, welcher der vier Filter extrahiert wird, brauchst du ein Gatter auf dem Rohr mit einer farbigen Aktion „Extraktionseinstellung“. Zum Beispiel: Redstone-Signal -> „Rot Extraktionseinstellung“.

Eine Liste kann in einen Filterslot gelegt werden, um die Filtermöglichkeiten zu erweitern.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Energieversorgung"/>
Es kann Energie von jeder MJ-Energiequelle erhalten, etwa von einem Motor, einem Energieadapter oder einem Rohrpulsar.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Bei jedem Energiepuls, den es erhält, extrahiert es Gegenstände aus dem angrenzenden Inventar.
Je mehr Energie ein Puls hat, desto mehr Gegenstände werden pro Puls extrahiert.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>