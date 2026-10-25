Alle Seiten der Anleitung sind in einer Teilmenge von Markdown geschrieben.

<italic>(Der Großteil dieser Seite muss noch ausgefüllt werden, hier ist aber schon einmal eine Liste der grundlegenden Tags)</italic>

# Einfache Einzeltags
Jedes Tag hat die Form &lt;name arguments/&gt; – dabei ist name der Name des Tags, und der abschließende '/' bedeutet, dass das Tag für sich allein steht. Manche Tags haben außerdem Argumente in Form von „Schlüssel-Wert-Paaren“, zum Beispiel:
<guide_md>
"level"="42"
</guide_md>

## Neue Seite
&lt;new_page/&gt; verschiebt den nachfolgenden Inhalt auf die nächste Seite.

## Links
Damit erstellst du einen Link zu einer anderen Seite der Anleitung, mit dem Titel und Symbol der verlinkten Seite. Alternativ kannst du auch auf einen ItemStack oder ein Statement verlinken, selbst wenn es dafür keine Seite in der Anleitung gibt.
Zum Beispiel:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
ergibt:
<link to="buildcraftreborn:config/guide_entry_format"/>

oder:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
ergibt:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Kapitel
&lt;chapter/&gt; erstellt eine „Kapitelüberschrift“, die in der Kapitelliste als farbige Überschrift angezeigt wird. Es gibt ein mögliches Argument: "level", das eine ganze Zahl von 0 oder größer sein muss. Alternativ kannst du wie in normalem Markdown mehrere Rautezeichen (#) verwenden, um Überschriften zu erstellen. Die Anzahl der # (minus eins) entspricht der Ebene der Überschrift.
Zum Beispiel:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
ergibt:
### Kapitel der Ebene 3
<chapter name="Ein weiteres Kapitel der Ebene 3" level="2"/>

# Einfache Doppeltags
Diese Tags müssen immer einen Inhalt umschließen und brauchen sowohl ein Start-Tag &lt;italic&gt; als auch ein End-Tag &lt;/italic&gt;.
Sie wirken sich alle auf den Text zwischen Start- und End-Tag aus.

- <italic>kursiv</italic>
- <bold>fett</bold>
- <underline>unterstrichen</underline>
- <strikethrough>durchgestrichen</strikethrough>
- <red>rot</red>
- <black>schwarz</black>
- <dark_blue>dunkelblau</dark_blue>
- <dark_green>dunkelgrün</dark_green>
- <dark_aqua>dunkeltürkis</dark_aqua>
- <dark_red>dunkelrot</dark_red>
- <dark_purple>dunkelviolett</dark_purple>
- <gold>goldfarben</gold>
- <gray>grau</gray>
- <dark_gray>dunkelgrau</dark_gray>
- <blue>blau</blue>
- <green>grün</green> (grün)
- <aqua>türkis</aqua> (türkis)
- <light_purple>hellviolett</light_purple>
- <yellow>gelb</yellow> (gelb)
- <white>weiß</white> (weiß)

Grün, Türkis, Gelb und Weiß solltest du besser nicht verwenden, da diese Farben auf der Seite meist nur sehr schwer zu erkennen sind.

# Umschaltbare Tags
Alle folgenden Tags hängen von Optionen ab und können vom Spieler aktiviert oder deaktiviert werden.

## Lore
Ist das Kästchen "Hintergrund anzeigen [ ]" angehakt, wird alles im Tag &lt;no_lore&gt; ausgeblendet, andernfalls alles im Tag &lt;lore&gt;.

Das Lore-Tag ist für Informationen gedacht, die „im Spiel“ aus der Sicht des Spielers erzählt werden (als würde er gerade selbst erforschen, wie alles funktioniert). Das Gegenstück dient einem sachlicheren, Wikipedia-ähnlichen Ton, der die Informationen schlicht und einfach vermittelt.

Im Spiel ist dies standardmäßig aktiviert, in Exporten immer deaktiviert.

## Hinweise
Ist das Kästchen "Hinweise anzeigen [ ]" angehakt, wird alles im Tag &lt;no_hint&gt; ausgeblendet, andernfalls alles im Tag &lt;hint&gt;.

Das Hint-Tag ist für zusätzliche „Hinweise“ gedacht, wofür sich das beschriebene Ding verwenden lässt, oder für verschiedene Möglichkeiten, es mit anderen Blöcken oder Gegenständen zu kombinieren. Im Grunde geht es um „empfohlene Verwendungen“ verschiedener Dinge. (Da das Herausfinden Teil des Spaßes ist, ist es nicht erforderlich.) Deshalb ist dies standardmäßig deaktiviert, lässt sich aber im Spiel umschalten und soll künftig auch beim Export umschaltbar sein.

## Details
Ist die Client-Konfigurationsoption "guideBookEnableDetail" auf "true" gesetzt, wird alles im Tag &lt;no_detail&gt; ausgeblendet, andernfalls alles im Tag &lt;detail&gt;.

Das Detail-Tag ist dafür gedacht, alle Zahlen anzuzeigen, die bei verschiedenen Berechnungen verwendet werden, etwa Durchflussrate von Rohren, Extraktionsrate, Pulsrate usw.

Dies ist standardmäßig deaktiviert, lässt sich aber im Spiel umschalten und soll künftig auch beim Export umschaltbar sein.

# Sonstiges
## Rezept (Einzahl)
Dieses Tag zeigt das erste gefundene Rezept für den angegebenen Gegenstandsstapel. Zum Beispiel:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Rezepte
Dieses Tag zeigt alle Rezepte für den angegebenen Gegenstandsstapel. Zum Beispiel:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Verwendungen
Ähnlich wie bei Rezepten zeigt dies alle Verwendungen für den angegebenen Gegenstandsstapel. Zum Beispiel:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Rezepte und Verwendungen
Dies fasst die beiden obigen Tags praktisch zu einem zusammen, zum Beispiel:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Gruppe
Dies zeigt die passenden Gruppen für den angegebenen Gegenstand, das Statement usw.

## Bilder
Dies zeigt ein Bild an. Es gibt 3 Argumente:
* src: Der Quellort des Bildes.
* width: Die Breite, mit der das Bild angezeigt wird. Standardmäßig die Breite des Quellbildes (was aber vielleicht nicht das ist, was du willst).
* height: Die Höhe, mit der das Bild angezeigt wird.

Zum Beispiel:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
