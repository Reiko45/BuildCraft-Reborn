<lore>
Vuoi estrarre certi oggetti e portarli altrove? Basta dare energia al tubo e guardarli scorrere.
</lore>
<no_lore>
Il Tubo di legno e diamante per trasporto serve a estrarre da un inventario gli oggetti selezionati dal suo filtro quando riceve energia.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="Funzionamento del tubo"/>
Quando è alimentato, estrae gli oggetti selezionati nel filtro e li trasporta verso gli altri inventari o tubi collegati.
L'estrazione avviene dal lato pieno del tubo, e gli oggetti vengono trasportati verso i lati trasparenti.
Se ha più lati trasparenti, gli oggetti si muovono in una direzione casuale.
Se gli oggetti estratti non hanno dove andare, escono dal tubo e cadono a terra.

Alcune macchine possono immettere automaticamente oggetti direttamente in un Tubo di legno e diamante per trasporto senza che il tubo sia alimentato (in questo caso il filtro non viene rispettato).
Usando i Tappi per tubi o dipingendo i tubi di colori diversi si può impedire che si colleghino.

<chapter name="Filtraggio"/>
Aprendo l'interfaccia del Tubo di legno e diamante per trasporto puoi impostare un filtro.
Mettendo negli slot gli oggetti che vuoi, puoi filtrare ciò che estrae.
Puoi usare le opzioni Lista bianca, Lista nera e A rotazione per cambiare il comportamento del filtro:
Lista bianca: vengono estratti solo gli oggetti selezionati.
Lista nera: vengono estratti tutti gli oggetti tranne quelli selezionati.
A rotazione: estrae gli oggetti nell'ordine esatto degli slot di filtro qui sopra (da sinistra a destra).

Una Lista può essere usata in uno slot di filtro per ampliare le opzioni di filtraggio.
<link to="buildcraftreborn:item/list"/>

<chapter name="Alimentazione"/>
Può ricevere energia da qualsiasi fonte di MJ, come un motore, un adattatore di corrente o un pulsar per tubi.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
A ogni impulso di energia ricevuto, estrae oggetti dall'inventario adiacente.
Più energia ha un impulso, più oggetti vengono estratti per impulso.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>