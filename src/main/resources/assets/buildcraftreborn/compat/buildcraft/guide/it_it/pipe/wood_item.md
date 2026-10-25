<lore>
Vuoi estrarre degli oggetti e portarli altrove? Basta dare energia al tubo e guardarli scorrere.
</lore>
<no_lore>
Il Tubo di legno per trasporto serve a estrarre oggetti da un inventario quando riceve energia.
</no_lore>

<recipes stack="buildcraftreborn:pipe_wood_item"/>

<chapter name="Funzionamento del tubo"/>
Quando è alimentato, estrae gli oggetti e li trasporta verso gli altri inventari o tubi collegati.
L'estrazione avviene dal lato pieno del tubo, e gli oggetti vengono trasportati verso i lati trasparenti.
Se ha più lati trasparenti, gli oggetti si distribuiscono a caso tra di essi.
Se gli oggetti estratti non hanno dove andare, escono dal tubo e cadono a terra.

Alcune macchine possono immettere automaticamente oggetti direttamente in un Tubo di legno per trasporto senza che il tubo sia alimentato.
Usando i Tappi per tubi o dipingendo i tubi di colori diversi si può impedire che si colleghino.

<chapter name="Alimentazione"/>
Può ricevere energia da qualsiasi fonte di MJ, come un motore, un adattatore di corrente o un pulsar per tubi.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
A ogni impulso di energia ricevuto, estrae oggetti dall'inventario adiacente.
Più energia ha un impulso, più oggetti vengono estratti per impulso.

<usages stack="buildcraftreborn:pipe_wood_item"/>