Alle pagina's van de handleiding zijn geschreven in een subset van markdown.

<italic>(Het grootste deel van deze pagina moet nog worden ingevuld, maar hier is alvast een lijst met basistags)</italic>

# Eenvoudige enkele tags
Elke tag heeft de vorm &lt;name arguments/&gt;, waarbij name de naam van de tag is en de afsluitende '/' aangeeft dat deze tag op zichzelf staat. Sommige tags hebben ook argumenten in de vorm van ‘sleutel-waardeparen’, bijvoorbeeld:
<guide_md>
"level"="42"
</guide_md>

## Nieuwe pagina
&lt;new_page/&gt; verplaatst de inhoud erna naar de volgende pagina.

## Links
Hiermee maak je een link naar een andere pagina van de handleiding, met de titel en het pictogram van die pagina. Je kunt ook een link maken naar een ItemStack of Statement, zelfs als daar geen pagina in de handleiding voor bestaat.
Bijvoorbeeld:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
geeft:
<link to="buildcraftreborn:config/guide_entry_format"/>

of:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
geeft:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Hoofdstukken
&lt;chapter/&gt; maakt een ‘hoofdstuktitel’, die in de hoofdstukkenlijst als gekleurde titel wordt getoond. Er is één mogelijk argument: "level", dat een geheel getal van 0 of hoger moet zijn. Je kunt ook, net als in gewone markdown, meerdere hekjes (#) gebruiken om titels te maken. Het aantal #'s (min één) is gelijk aan het niveau van de titel.
Bijvoorbeeld:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
geeft:
### Hoofdstuk van niveau 3
<chapter name="Nog een hoofdstuk van niveau 3" level="2"/>

# Eenvoudige dubbele tags
Deze tags moeten altijd inhoud omsluiten en hebben zowel een starttag &lt;italic&gt; als een eindtag &lt;/italic&gt; nodig.
Ze hebben allemaal invloed op de tekst tussen de start- en eindtag.

- <italic>cursief</italic>
- <bold>vet</bold>
- <underline>onderstreept</underline>
- <strikethrough>doorgehaald</strikethrough>
- <red>rood</red>
- <black>zwart</black>
- <dark_blue>donkerblauw</dark_blue>
- <dark_green>donkergroen</dark_green>
- <dark_aqua>donkeraqua</dark_aqua>
- <dark_red>donkerrood</dark_red>
- <dark_purple>donkerpaars</dark_purple>
- <gold>goud</gold>
- <gray>grijs</gray>
- <dark_gray>donkergrijs</dark_gray>
- <blue>blauw</blue>
- <green>groen</green> (groen)
- <aqua>aqua</aqua> (aqua)
- <light_purple>lichtpaars</light_purple>
- <yellow>geel</yellow> (geel)
- <white>wit</white> (wit)

Groen, aqua, geel en wit kun je beter niet gebruiken, omdat die kleuren op de pagina meestal erg moeilijk te zien zijn.

# Schakelbare tags
Alle volgende tags hangen af van een optie en kunnen door de speler worden in- of uitgeschakeld.

## Lore
Als het vakje "Achtergrond tonen [ ]" is aangevinkt, wordt alles in de tag &lt;no_lore&gt; verborgen; anders wordt alles in de tag &lt;lore&gt; verborgen.

De lore-tag is bedoeld voor informatie ‘in het spel’, verteld vanuit het perspectief van de speler (alsof die zelf actief uitzoekt hoe alles werkt). Het tegenovergestelde is bedoeld voor een formelere, Wikipedia-achtige toon, die de informatie puur en simpel overbrengt.

In het spel staat dit standaard aan, in exports altijd uit.

## Hints
Als het vakje "Tips tonen [ ]" is aangevinkt, wordt alles in de tag &lt;no_hint&gt; verborgen; anders wordt alles in de tag &lt;hint&gt; verborgen.

De hint-tag is bedoeld voor extra ‘hints’ over waarvoor het beschreven ding gebruikt kan worden, of voor verschillende manieren om het te combineren met andere blokken of voorwerpen. In feite gaat het om ‘aanbevolen toepassingen’ van verschillende dingen. (Omdat zelf uitzoeken deel van de lol is, is dit niet verplicht.) Daarom staat dit standaard uit, maar je kunt het in het spel in- en uitschakelen, en het is de bedoeling dat dat later ook bij exports kan.

## Details
Als de clientconfiguratie-optie "guideBookEnableDetail" op "true" staat, wordt alles in de tag &lt;no_detail&gt; verborgen; anders wordt alles in de tag &lt;detail&gt; verborgen.

De detail-tag is bedoeld om alle getallen te tonen die bij verschillende berekeningen worden gebruikt, zoals de doorstroomsnelheid van pijpen, de extractiesnelheid, de pulsfrequentie enz.

Dit staat standaard uit, maar je kunt het in het spel in- en uitschakelen, en het is de bedoeling dat dat later ook bij exports kan.

# Overig
## Recept (enkelvoud)
Deze tag toont het eerste gevonden recept voor de opgegeven voorwerpstapel. Bijvoorbeeld:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Recepten
Deze tag toont alle recepten voor de opgegeven voorwerpstapel. Bijvoorbeeld:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Toepassingen
Net als bij recepten toont dit alle toepassingen voor de opgegeven voorwerpstapel. Bijvoorbeeld:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Recepten en toepassingen
Dit combineert de twee tags hierboven in feite tot één, bijvoorbeeld:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Groep
Dit toont de groepen die passen bij het opgegeven voorwerp, statement enz.

## Afbeeldingen
Dit toont een afbeelding. Er zijn 3 argumenten:
* src: de bronlocatie van de afbeelding.
* width: de breedte waarmee de afbeelding wordt getoond. Standaard is dat de breedte van de bronafbeelding (maar dat is misschien niet wat je wilt).
* height: de hoogte waarmee de afbeelding wordt getoond.

Bijvoorbeeld:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
