<lore>
Usare un gate per estrarre certi oggetti è l'ultima rivoluzione tecnica nel campo del filtraggio.
</lore>
<no_lore>
Il Tubo di emzuli per trasporto è un tubo controllato da un gate che serve a estrarre da un inventario gli oggetti selezionati dal suo filtro quando riceve energia.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="Funzionamento del tubo"/>
Quando è alimentato, estrae gli oggetti selezionati nel filtro e li trasporta verso gli altri inventari o tubi collegati.
L'estrazione avviene dal lato pieno del tubo, e gli oggetti vengono trasportati verso i lati trasparenti.
Se ha più lati trasparenti, gli oggetti si muovono in una direzione casuale.
Se gli oggetti estratti non hanno dove andare, escono dal tubo e cadono a terra.

Alcune macchine possono immettere automaticamente oggetti direttamente in un tubo di emzuli senza che il tubo sia alimentato (in questo caso i filtri non vengono rispettati).
Usando i Tappi per tubi o dipingendo i tubi di colori diversi si può impedire che si colleghino.

<chapter name="Filtraggio"/>
L'interfaccia del Tubo di emzuli per trasporto mostra quattro colori, per quattro filtri separati.
Mettendo oggetti negli slot, definisci cosa estrae ciascun filtro.
Il colore dei pennelli accanto agli slot determina di che colore verranno colorati gli oggetti all'estrazione, e si può cambiare cliccandoci sopra.
Per scegliere quale dei quattro filtri usare, serve un gate posizionato sul tubo con un'azione «Programma per estrazione» del colore voluto. Ad esempio: segnale di redstone -> Programma per estrazione rosso.

Una Lista può essere usata in uno slot di filtro per ampliare le opzioni di filtraggio.
<link to="buildcraftreborn:item/list"/>

<chapter name="Alimentazione"/>
Può ricevere energia da qualsiasi fonte di MJ, come un motore, un adattatore di corrente o un pulsar per tubi.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
A ogni impulso di energia ricevuto, estrae oggetti dall'inventario adiacente.
Più energia ha un impulso, più oggetti vengono estratti per impulso.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>