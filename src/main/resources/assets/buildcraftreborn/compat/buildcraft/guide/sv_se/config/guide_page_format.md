Alla guidesidor är skrivna i en delmängd av markdown.

<italic>(Det mesta av den här sidan behöver fyllas i, men här är en lista över grundläggande taggar)</italic>

# Enkla fristående taggar
Alla taggar har formen &lt;name arguments/&gt;, där name är taggens namn och det avslutande '/' anger att taggen står för sig själv. Vissa taggar har också argument i form av ”nyckel-värde-par”, till exempel:
<guide_md>
"level"="42"
</guide_md>

## Ny sida
&lt;new_page/&gt; flyttar innehållet efter den till nästa sida.

## Länkar
De skapar en länk till en annan sida i guideboken, med titeln och ikonen från den länkade sidan. Du kan också skapa en länk till en ItemStack eller ett Statement även om de inte har någon egen sida i guideboken.
Till exempel:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
blir:
<link to="buildcraftreborn:config/guide_entry_format"/>

eller:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
blir:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Kapitel
&lt;chapter/&gt; skapar en ”kapitelrubrik” som visas i kapitellistan som en färgad rubrik. Den har ett möjligt argument: "level", som måste vara ett heltal som är 0 eller större. Du kan också använda flera brädgårdar (#), precis som i vanlig markdown, för att skapa rubriker. Antalet # (minus ett) motsvarar rubrikens nivå.
Till exempel:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
blir:
### Kapitel på nivå 3
<chapter name="Ett annat kapitel på nivå 3" level="2"/>

# Enkla dubbla taggar
Alla dessa måste omsluta något innehåll, och de måste ha både en starttagg &lt;italic&gt; och en sluttagg &lt;/italic&gt;
De påverkar alla texten mellan start- och sluttaggen.

- <italic>italic</italic> (kursiv)
- <bold>bold</bold> (fet)
- <underline>underlined</underline> (understruken)
- <strikethrough>strikethrough</strikethrough> (genomstruken)
- <red>red</red> (röd)
- <black>black</black> (svart)
- <dark_blue>dark_blue</dark_blue> (mörkblå)
- <dark_green>dark_green</dark_green> (mörkgrön)
- <dark_aqua>dark_aqua</dark_aqua> (mörk turkos)
- <dark_red>dark_red</dark_red> (mörkröd)
- <dark_purple>dark_purple</dark_purple> (mörklila)
- <gold>gold</gold> (guld)
- <gray>gray</gray> (grå)
- <dark_gray>dark_gray</dark_gray> (mörkgrå)
- <blue>blue</blue> (blå)
- <green>green</green> (green, grön)
- <aqua>aqua</aqua> (aqua, turkos)
- <light_purple>light_purple</light_purple> (ljuslila)
- <yellow>yellow</yellow> (yellow, gul)
- <white>white</white> (white, vit)

Det är ingen bra idé att använda grön, turkos, gul eller vit, eftersom de färgerna oftast är mycket svåra att se på sidan.

# Växlingsbara taggar
Alla följande taggar styrs av inställningar och kan slås på eller av av spelaren.

## Berättelse
Om rutan ”Visa bakgrund [ ]” är ikryssad döljs allt i taggen &lt;no_lore&gt;, annars döljs allt i taggen &lt;lore&gt;.

Taggen lore ska användas för att visa guideinformation ”i spelet”, berättad ur spelarens perspektiv (som om spelaren aktivt undersökte hur allting fungerar). Motsatsen är till för en mer formell, Wikipedia-liknande ton som förmedlar informationen, helt enkelt.

Det här är aktiverat som standard i spelet och alltid inaktiverat vid export.

## Tips
Om rutan ”Visa tips [ ]” är ikryssad döljs allt i taggen &lt;no_hint&gt;, annars döljs allt i taggen &lt;hint&gt;.

Taggen hint ska användas för att visa extra ”tips” om vad det beskrivna kan användas till, eller olika sätt att använda det i kombination med andra block eller föremål. I grunden är det till för ”rekommenderade användningar” av olika saker. (Eftersom det är en del av det roliga att lista ut detta är det inte obligatoriskt.) Därför är det inaktiverat som standard, men det kan slås på och av i spelet, och det är planerat att det ska kunna slås på och av vid export.

## Detaljer
Om klientinställningen "guideBookEnableDetail" är satt till "true" döljs allt i taggen &lt;no_detail&gt;, annars döljs allt i taggen &lt;detail&gt;.

Taggen detail ska användas för att visa alla siffror som används vid beräkning av olika saker, som rörens flödeshastighet, extraheringshastighet, pulsfrekvens och så vidare.

Det här är inaktiverat som standard, men det kan slås på och av i spelet, och det är planerat att det ska kunna slås på och av vid export.

# Övrigt
## Recept (singular)
Den här taggen visar det första receptet som hittas för den angivna föremålsstapeln. Till exempel:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Recept
Den här taggen visar alla recept för den angivna föremålsstapeln. Till exempel:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Användningar
Precis som recipes visar den här taggen alla användningar för den angivna föremålsstapeln. Till exempel:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Recept och användningar
Den här taggen kombinerar i praktiken de två taggarna ovan till en, till exempel:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Grupp
Den här visar grupper som passar för det angivna föremålet, utlösaren, åtgärden och så vidare.

## Bilder
Den här visar en bild. Det finns 3 argument:
* src: bildens källplats.
* width: bredden som bilden visas med. Standard är källbildens bredd (vilket kanske inte är vad du vill).
* height: höjden som bilden visas med.

Till exempel:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
