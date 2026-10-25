Alle guidesider er skrevet i en delmængde af markdown.

<italic>(Det meste af denne side mangler stadig at blive skrevet, men her er en liste over grundlæggende tags)</italic>

# Enkle enkeltstående tags
Alle tags har formen &lt;name arguments/&gt;, hvor name er taggets navn, og den afsluttende '/' betyder, at tagget står alene. Nogle tags har også argumenter i form af "nøgle-værdi-par", for eksempel:
<guide_md>
"level"="42"
</guide_md>

## Ny side
&lt;new_page/&gt; flytter indholdet efter det til næste side.

## Links
Disse laver et link til en anden side i guidebogen med titlen og ikonet fra den side, der linkes til. Du kan også lave et link til en ItemStack eller et Statement, selv om de ikke har en side i guidebogen.
For eksempel:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
giver:
<link to="buildcraftreborn:config/guide_entry_format"/>

eller:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
giver:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Kapitler
&lt;chapter/&gt; laver en "kapiteloverskrift", der vises i kapitellisten som en farvet overskrift. Det har ét muligt argument: "level", som skal være et heltal på 0 eller derover. Du kan også lave overskrifter med flere havelåger (#), ligesom i almindelig markdown. Antallet af # (minus én) er overskriftens niveau.
For eksempel:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
giver:
### Kapitel på niveau 3
<chapter name="Endnu et kapitel på niveau 3" level="2"/>

# Enkle dobbelte tags
Alle disse skal omslutte noget indhold, og de skal have både et starttag &lt;italic&gt; og et sluttag &lt;/italic&gt;.
De påvirker alle teksten mellem start- og sluttagget.

- <italic>italic</italic> (kursiv)
- <bold>bold</bold> (fed)
- <underline>underlined</underline> (understreget)
- <strikethrough>strikethrough</strikethrough> (gennemstreget)
- <red>red</red> (rød)
- <black>black</black> (sort)
- <dark_blue>dark_blue</dark_blue> (mørkeblå)
- <dark_green>dark_green</dark_green> (mørkegrøn)
- <dark_aqua>dark_aqua</dark_aqua> (mørk turkis)
- <dark_red>dark_red</dark_red> (mørkerød)
- <dark_purple>dark_purple</dark_purple> (mørkelilla)
- <gold>gold</gold> (guld)
- <gray>gray</gray> (grå)
- <dark_gray>dark_gray</dark_gray> (mørkegrå)
- <blue>blue</blue> (blå)
- <green>green</green> (green, grøn)
- <aqua>aqua</aqua> (aqua, turkis)
- <light_purple>light_purple</light_purple> (lyslilla)
- <yellow>yellow</yellow> (yellow, gul)
- <white>white</white> (white, hvid)

Det er ikke en god idé at bruge green, aqua, yellow eller white, fordi de farver generelt er meget svære at se på siden.

# Tags, der kan slås til og fra
Alle de følgende tags styres af indstillinger og kan slås til eller fra af spilleren.

## Baggrundshistorie
Hvis feltet "Vis historie [ ]" er afkrydset, skjules alt i tagget &lt;no_lore&gt;, ellers skjules alt i tagget &lt;lore&gt;.

Lore-tagget bruges til at vise guideoplysninger "i spillet", fortalt fra spillerens synsvinkel (som om spilleren selv var i gang med at undersøge, hvordan det hele virker). Det modsatte tag er til en mere formel, Wikipedia-agtig tone, der formidler oplysningerne rent og enkelt.

Det er slået til som standard i spillet og altid slået fra i eksporter.

## Tips
Hvis feltet "Vis tips [ ]" er afkrydset, skjules alt i tagget &lt;no_hint&gt;, ellers skjules alt i tagget &lt;hint&gt;.

Hint-tagget bruges til at vise ekstra "tips" om, hvad det beskrevne kan bruges til, eller forskellige måder at bruge det på sammen med andre blokke og genstande. Det er i bund og grund til "anbefalede anvendelser" af forskellige ting. (Da det er en del af det sjove selv at finde ud af det, er det ikke påkrævet.) Derfor er det slået fra som standard, men det kan slås til og fra i spillet, og det er planen, at det også skal kunne slås til og fra ved eksport.

## Detaljer
Hvis klientindstillingen "guideBookEnableDetail" er sat til "true", skjules alt i tagget &lt;no_detail&gt;, ellers skjules alt i tagget &lt;detail&gt;.

Detail-tagget bruges til at vise alle de tal, der bruges til at beregne forskellige ting, f.eks. rørs gennemstrømningshastighed, udtrækshastighed, pulshastighed osv.

Det er slået fra som standard, men det kan slås til og fra i spillet, og det er planen, at det også skal kunne slås til og fra ved eksport.

# Diverse
## Opskrift (ental)
Dette tag viser den første opskrift, der findes for den angivne genstandsstak. For eksempel:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Opskrifter
Dette tag viser alle opskrifter for den angivne genstandsstak. For eksempel:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Anvendelser
Ligesom recipes viser dette alle anvendelser af den angivne genstandsstak. For eksempel:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Opskrifter og anvendelser
Dette samler i praksis de to ovenstående tags i ét, for eksempel:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Gruppe
Dette viser de grupper, der passer til den angivne genstand, statement osv.

## Billeder
Dette viser et billede. Der er 3 argumenter:
* src: Billedets kildeplacering.
* width: Den bredde, billedet vises med. Standard er kildebilledets bredde (men det er måske ikke det, du ønsker).
* height: Den højde, billedet vises med.

For eksempel:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
