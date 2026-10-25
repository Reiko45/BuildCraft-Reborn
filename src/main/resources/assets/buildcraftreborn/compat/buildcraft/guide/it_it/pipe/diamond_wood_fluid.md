<lore>
Vuoi estrarre un certo fluido e portarlo altrove? Basta dare energia al tubo e guardarlo scorrere.
</lore>
<no_lore>
Il Tubo di legno e diamante per fluidi serve a estrarre fluidi selezionati da un serbatoio quando riceve energia.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_fluid"/>

<chapter name="Funzionamento del tubo"/>
Quando è alimentato, estrae il fluido selezionato dal filtro verso gli altri serbatoi o tubi collegati.
L'estrazione avviene dal lato pieno del tubo verso i lati trasparenti.
Se ha più lati trasparenti, i fluidi si dividono equamente in ogni direzione.

Alcune macchine possono immettervi direttamente fluidi senza bisogno di energia (in questo caso il filtro non viene rispettato).
Usando i Tappi per tubi o dipingendo i tubi di colori diversi si può impedire che si colleghino.

<chapter name="Filtraggio"/>
Aprendo l'interfaccia del Tubo di legno e diamante per fluidi puoi impostare un filtro.
Mettendo negli slot un secchio (o un altro contenitore) del fluido che vuoi, puoi filtrare ciò che estrae.
Puoi usare le opzioni Lista bianca e Lista nera per cambiare il comportamento del filtro:
Lista bianca: vengono estratti solo i fluidi selezionati.
Lista nera: vengono estratti tutti i fluidi tranne quello selezionato.

<chapter name="Alimentazione"/>
Può ricevere energia da qualsiasi fonte di MJ, come un motore, un adattatore di corrente o un pulsar per tubi.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
A ogni impulso di energia ricevuto, estrae fluido dal serbatoio adiacente.
Più energia ha un impulso, più fluido viene estratto per impulso.

<no_detail>
Il tubo di legno e diamante per fluidi trasporta i fluidi ai tubi e ai serbatoi adiacenti a 1,6 secchi al secondo.
I tubi per fluidi hanno un serbatoio interno che può contenere 1 secchio per lato e 1 secchio al centro.
</no_detail>

<usages stack="buildcraftreborn:pipe_diamond_wood_fluid"/>