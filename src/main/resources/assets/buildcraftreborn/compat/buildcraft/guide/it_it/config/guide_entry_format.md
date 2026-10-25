Ogni pagina della guida è una «voce»: un nome, l'argomento della pagina e la sua posizione nell'indice. I pacchetti di risorse possono aggiungere, sostituire o rimuovere voci.

<chapter name="File delle voci"/>
Le voci sono elencate in un file json in ciascun namespace di un pacchetto di risorse:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Contiene un unico oggetto, che associa il nome di ciascuna voce alla sua definizione. Ad esempio:
<json_insn>
{
  "item/wrench": {
    "type": "item_stack",
    "book": "buildcraftreborn:main",
    "tag_type": "item",
    "tag_subtype": "tool",
    "stack": "buildcraftreborn:wrench"
  }
}
</json_insn>
<new_page/>
La pagina vera e propria è un file separato, che prende il nome dalla voce:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
La pagina en_us viene sempre caricata, e la pagina nella tua lingua (se esiste) la sostituisce. Consulta <link to="buildcraftreborn:config/guide_page_format"/> per sapere cosa può contenere una pagina.

Vincono i pacchetti di risorse più in alto: una voce con lo stesso nome sostituisce quella sottostante, e una voce impostata a null la rimuove.

<chapter name="Tipi di voci"/>
Ogni voce ha un "type":
- item_stack: una pagina su un oggetto (o un blocco). "stack" è l'id dell'oggetto.
- statement: una pagina su un'attivazione o un'azione di gate. "statement" è il suo id, ad esempio "buildcraft:redstone.input.active".
- external: una pagina che non riguarda nulla del gioco, come questa. "title" è una chiave di traduzione, dopo "buildcraft.guide.page.".

Una voce il cui oggetto, attivazione o azione non esiste (ad esempio perché una mod non è installata) viene ignorata.

<new_page/>
<chapter name="Indice"/>
"book" è il libro in cui si trova la voce. Il libro guida comprende "buildcraftreborn:main" (il Libro guida di BuildCraft) e "buildcraftreborn:meta" (questo libro).

"tag_type" e "tag_subtype" indicano dove si trova la voce nella pagina dell'indice. Vengono tradotti come "buildcraft.guide.chapter.type.[tag_type]" e "buildcraft.guide.chapter.subtype.[tag_subtype]".

I libri sono elencati allo stesso modo, in:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Ad esempio:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" è una chiave di traduzione, e "all_entries" indica se l'indice del libro elenca anche tutto ciò che non si trova in nessun libro.

Premi F3 e T mentre la guida è aperta per ricaricare tutto.

<italic>BuildCraft 8.0 definiva le voci in file "guide.txt", con un proprio linguaggio di istruzioni. BuildCraft Reborn usa invece semplice json.</italic>
