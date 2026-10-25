Le pagine della guida sono tutte scritte in un sottoinsieme del markdown.

<italic>(Gran parte di questa pagina è ancora da completare, ma ecco un elenco dei tag di base.)</italic>

# Tag singoli semplici
Ogni tag ha la forma &lt;name arguments/&gt;, dove name è il nome del tag e la '/' finale indica che il tag è a sé stante. Alcuni tag hanno anche argomenti sotto forma di «coppie chiave-valore», ad esempio:
<guide_md>
"level"="42"
</guide_md>

## Nuova pagina
&lt;new_page/&gt; sposta il contenuto che lo segue nella pagina successiva.

## Collegamenti
Questi tag creano un collegamento a un'altra pagina del libro guida, usando il titolo e l'icona della pagina collegata. In alternativa puoi creare un collegamento a un ItemStack o a uno Statement anche se non hanno una pagina nel libro guida.
Ad esempio:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
produce:
<link to="buildcraftreborn:config/guide_entry_format"/>

oppure:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
produce:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Capitoli
&lt;chapter/&gt; crea un «titolo di capitolo», mostrato nell'elenco dei capitoli come titolo colorato. Accetta un solo argomento, "level", che deve essere un numero intero maggiore o uguale a 0. In alternativa puoi usare più cancelletti (#), come nel normale formato markdown, per creare titoli. Il numero di # (meno uno) corrisponde al livello del titolo.
Ad esempio:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
produce:
### Capitolo di livello 3
<chapter name="Un altro capitolo di livello 3" level="2"/>

# Tag doppi semplici
Tutti questi tag devono racchiudere del contenuto: devono avere sia un tag di apertura &lt;italic&gt; sia un tag di chiusura &lt;/italic&gt;.
Agiscono tutti sul testo compreso tra il tag di apertura e quello di chiusura.

- <italic>corsivo</italic> (italic)
- <bold>grassetto</bold> (bold)
- <underline>sottolineato</underline> (underline)
- <strikethrough>barrato</strikethrough> (strikethrough)
- <red>rosso</red> (red)
- <black>nero</black> (black)
- <dark_blue>blu scuro</dark_blue> (dark_blue)
- <dark_green>verde scuro</dark_green> (dark_green)
- <dark_aqua>ciano scuro</dark_aqua> (dark_aqua)
- <dark_red>rosso scuro</dark_red> (dark_red)
- <dark_purple>viola scuro</dark_purple> (dark_purple)
- <gold>oro</gold> (gold)
- <gray>grigio</gray> (gray)
- <dark_gray>grigio scuro</dark_gray> (dark_gray)
- <blue>blu</blue> (blue)
- <green>verde</green> (green)
- <aqua>ciano</aqua> (aqua)
- <light_purple>viola chiaro</light_purple> (light_purple)
- <yellow>giallo</yellow> (yellow)
- <white>bianco</white> (white)

Non è una buona idea usare verde, ciano, giallo o bianco, perché in genere questi colori sono molto difficili da leggere sulla pagina.

# Tag attivabili
Tutti i tag seguenti dipendono da opzioni che il giocatore può attivare o disattivare.

## Narrazione
Se la casella "Mostra storia [ ]" è spuntata, tutto ciò che si trova nel tag &lt;no_lore&gt; viene nascosto; altrimenti viene nascosto tutto ciò che si trova nel tag &lt;lore&gt;.

Il tag lore serve a mostrare le informazioni della guida «all'interno del gioco», narrate dal punto di vista del giocatore (come se stesse studiando attivamente come funziona ogni cosa). L'opposto serve per un tono più formale, in stile Wikipedia, che trasmette le informazioni in modo puro e semplice.

Questa opzione è attiva per impostazione predefinita nel gioco, e sempre disattivata nelle esportazioni.

## Suggerimenti
Se la casella "Mostra suggerimenti [ ]" è spuntata, tutto ciò che si trova nel tag &lt;no_hint&gt; viene nascosto; altrimenti viene nascosto tutto ciò che si trova nel tag &lt;hint&gt;.

Il tag hint serve a mostrare «suggerimenti» aggiuntivi sugli usi possibili dell'elemento descritto, o su diversi modi di usarlo in combinazione con altri blocchi o oggetti. In sostanza riguarda gli «usi consigliati». (Scoprirli da soli fa parte del divertimento, quindi non sono indispensabili.) Per questo l'opzione è disattivata per impostazione predefinita, ma si può attivare nel gioco, ed è previsto che si possa attivare anche nelle esportazioni.

## Dettagli
Se l'opzione di configurazione del client "guideBookEnableDetail" è impostata su "true", tutto ciò che si trova nel tag &lt;no_detail&gt; viene nascosto; altrimenti viene nascosto tutto ciò che si trova nel tag &lt;detail&gt;.

Il tag detail serve a mostrare tutti i numeri usati nel calcolo di vari valori, come la portata dei tubi, la velocità di estrazione, la frequenza degli impulsi, ecc.

Questa opzione è disattivata per impostazione predefinita, ma si può attivare nel gioco, ed è previsto che si possa attivare anche nelle esportazioni.

# Varie
## Ricetta (singolare)
Questo tag mostra la prima ricetta trovata per la pila di oggetti indicata. Ad esempio:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Ricette
Questo tag mostra tutte le ricette della pila di oggetti indicata. Ad esempio:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Utilizzi
Come per le ricette, questo tag mostra tutti gli utilizzi della pila di oggetti indicata. Ad esempio:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Ricette e utilizzi
Questo tag unisce di fatto i due tag precedenti in uno solo, ad esempio:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Gruppo
Questo tag mostra i gruppi relativi all'oggetto, all'attivazione, all'azione, ecc. indicati.

## Immagini
Questo tag mostra un'immagine. Ha 3 argomenti:
* src: la posizione di origine dell'immagine.
* width: la larghezza con cui verrà mostrata l'immagine. Per impostazione predefinita è la larghezza dell'immagine di origine (ma potrebbe non essere ciò che vuoi).
* height: l'altezza con cui verrà mostrata l'immagine.

Ad esempio:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
