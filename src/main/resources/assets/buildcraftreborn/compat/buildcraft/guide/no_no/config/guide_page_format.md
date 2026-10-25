Alle guidesider er skrevet i et utvalg av markdown.

<italic>(Det meste av denne siden gjenstår å skrive, men her er en liste over grunnleggende tagger)</italic>

# Enkle enkeltstående tagger
Alle tagger har formen &lt;name arguments/&gt;, der name er navnet på taggen, og den avsluttende '/' betyr at taggen står alene. Noen tagger har også argumenter i form av "nøkkel-verdi-par", for eksempel:
<guide_md>
"level"="42"
</guide_md>

## Ny side
&lt;new_page/&gt; flytter innholdet etter den til neste side.

## Lenker
Disse lager en lenke til en annen side i guideboken, med tittelen og ikonet fra siden det lenkes til. Du kan også lage en lenke til en ItemStack eller et Statement selv om de ikke har en egen side i guideboken.
For eksempel:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
gir:
<link to="buildcraftreborn:config/guide_entry_format"/>

eller:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
gir:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Kapitler
&lt;chapter/&gt; lager en "kapitteloverskrift" som vises i kapittellisten som en farget overskrift. Den har ett mulig argument: "level", som må være et heltall på 0 eller mer. Du kan også lage overskrifter med flere firkanttegn (#), akkurat som i vanlig markdown. Antallet # (minus én) tilsvarer nivået på overskriften.
For eksempel:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
gir:
### Kapittel på nivå 3
<chapter name="Enda et kapittel på nivå 3" level="2"/>

# Enkle doble tagger
Alle disse må omslutte noe innhold, og de må ha både en starttagg &lt;italic&gt; og en sluttagg &lt;/italic&gt;.
De påvirker alle teksten mellom start- og sluttaggen.

- <italic>italic</italic> (kursiv)
- <bold>bold</bold> (fet)
- <underline>underlined</underline> (understreket)
- <strikethrough>strikethrough</strikethrough> (gjennomstreket)
- <red>red</red> (rød)
- <black>black</black> (svart)
- <dark_blue>dark_blue</dark_blue> (mørkeblå)
- <dark_green>dark_green</dark_green> (mørkegrønn)
- <dark_aqua>dark_aqua</dark_aqua> (mørk turkis)
- <dark_red>dark_red</dark_red> (mørkerød)
- <dark_purple>dark_purple</dark_purple> (mørkelilla)
- <gold>gold</gold> (gull)
- <gray>gray</gray> (grå)
- <dark_gray>dark_gray</dark_gray> (mørkegrå)
- <blue>blue</blue> (blå)
- <green>green</green> (green, grønn)
- <aqua>aqua</aqua> (aqua, turkis)
- <light_purple>light_purple</light_purple> (lyslilla)
- <yellow>yellow</yellow> (yellow, gul)
- <white>white</white> (white, hvit)

Det er ikke lurt å bruke green, aqua, yellow eller white, fordi de fargene generelt er svært vanskelige å se på siden.

# Tagger som kan slås av og på
Alle de følgende taggene styres av innstillinger og kan slås av eller på av spilleren.

## Bakgrunnshistorie
Hvis boksen "Vis bakgrunn [ ]" er krysset av, skjules alt i taggen &lt;no_lore&gt;, ellers skjules alt i taggen &lt;lore&gt;.

Lore-taggen brukes til å vise guideinformasjon "i spillet", fortalt fra spillerens synsvinkel (som om spilleren selv holdt på å finne ut hvordan alt virker). Den motsatte taggen er for en mer formell, Wikipedia-aktig tone som formidler informasjonen rett og slett.

Dette er slått på som standard i spillet og alltid slått av i eksporter.

## Tips
Hvis boksen "Vis tips [ ]" er krysset av, skjules alt i taggen &lt;no_hint&gt;, ellers skjules alt i taggen &lt;hint&gt;.

Hint-taggen brukes til å vise ekstra "tips" om hva det beskrevne kan brukes til, eller ulike måter å bruke det på sammen med andre blokker og gjenstander. Den er egentlig for "anbefalt bruk" av ulike ting. (Siden det å finne ut av dette selv er en del av moroa, er den ikke påkrevd.) Derfor er den slått av som standard, men den kan slås av og på i spillet, og planen er at den også skal kunne slås av og på ved eksport.

## Detaljer
Hvis klientinnstillingen "guideBookEnableDetail" er satt til "true", skjules alt i taggen &lt;no_detail&gt;, ellers skjules alt i taggen &lt;detail&gt;.

Detail-taggen brukes til å vise alle tallene som brukes til å beregne ulike ting, som gjennomstrømningshastighet i rør, uttrekkshastighet, pulshastighet osv.

Dette er slått av som standard, men det kan slås av og på i spillet, og planen er at det også skal kunne slås av og på ved eksport.

# Diverse
## Oppskrift (entall)
Denne taggen viser den første oppskriften som finnes for den angitte gjenstandsstabelen. For eksempel:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Oppskrifter
Denne taggen viser alle oppskrifter for den angitte gjenstandsstabelen. For eksempel:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Bruksområder
I likhet med recipes viser denne alle bruksområder for den angitte gjenstandsstabelen. For eksempel:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Oppskrifter og bruksområder
Denne slår i praksis sammen de to taggene ovenfor til én, for eksempel:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Gruppe
Denne viser grupper som passer til den angitte gjenstanden, statement osv.

## Bilder
Denne viser et bilde. Det finnes 3 argumenter:
* src: Kildeplasseringen til bildet.
* width: Bredden bildet vises med. Standard er bredden til kildebildet (men det er kanskje ikke det du vil ha).
* height: Høyden bildet vises med.

For eksempel:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
