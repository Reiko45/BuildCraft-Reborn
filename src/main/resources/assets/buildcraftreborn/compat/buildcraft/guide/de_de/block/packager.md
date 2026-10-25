<lore>
Ein Rezept, zusammengefaltet und verschickt.
</lore>
<no_lore>
Der Rezeptverpacker packt die Zutaten eines Herstellungsrezepts in ein Paket, das ein Stanztisch dann verarbeiten kann.
</no_lore>
<chapter name="Das Muster"/>
Das 3x3-Raster ist das Herstellungsraster des Rezepts. Klicke mit einem Gegenstand auf einen Slot, damit dieser Slot den Gegenstand verlangt. Klicke mit leerer Hand auf einen leeren Slot, damit er einen beliebigen Gegenstand verlangt (er leuchtet dann auf), und klicke erneut, um das wieder abzuschalten. Ein Klick mit leerer Hand auf einen Slot, der einen Gegenstand verlangt, leert ihn.
Alle Slots, die „beliebig“ verlangen, erhalten denselben Gegenstand – einen, den kein anderer Slot ausdrücklich verlangt.
<recipes_usages stack="buildcraftreborn:packager"/>
<chapter name="Verpacken"/>
Lege Papier in den Slot oben rechts. Alle Viertelsekunde nimmt der Verpacker für jeden aktiven Musterslot einen Gegenstand – zuerst aus seinen neun Lagerslots, dann aus den Inventaren an seinen vier Seiten – und macht aus einem Blatt Papier ein Paket, in dem jeder Gegenstand in seinem Slot liegt. Das Paket erscheint im Slot unter dem Papier, und der Verpacker wartet, bis es entnommen wurde.
Statt Papier kann auch ein Paket wieder hineingelegt werden, um seine leeren Slots zu füllen: Der Verpacker legt nichts dorthin, wo das Paket schon etwas enthält.
<chapter name="Automatisierung"/>
Rohre führen Zutaten von den Seiten in die Lagerslots zu (nur Gegenstände, die einen vorhandenen Stapel auffüllen oder die das Muster verwenden kann), Papier oder Pakete von oben oder unten, und entnehmen Pakete an jeder Seite. Lieferant-Roboter können die Pakete zu einem Stanztisch bringen.
