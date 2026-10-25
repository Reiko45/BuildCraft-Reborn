<lore>
Vil du trække bestemte genstande ud og transportere dem et andet sted hen? Giv bare røret energi, og se dem flyde.
</lore>
<no_lore>
Et Trædiamant-transportrør bruges til at trække de genstande, filteret har valgt, ud af et inventar, når det får energi.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="Sådan virker røret"/>
Når røret får energi, trækker det de genstande ud, der er valgt i filteret, og transporterer dem til andre tilsluttede inventarer eller rør.
Genstandene trækkes ud gennem rørets massive side og transporteres mod de gennemsigtige sider.
Har røret flere gennemsigtige sider, bevæger genstandene sig i en tilfældig retning.
Hvis genstandene ikke har nogen steder at tage hen, falder de ud af røret og ned på jorden.

Nogle maskiner kan automatisk sende genstande direkte ind i et Trædiamant-transportrør, uden at røret skal have energi (filteret gælder så ikke).
Med Rørpropper eller ved at farve rør i forskellige farver kan du forhindre rør i at forbinde sig.

<chapter name="Filtrering"/>
Åbn brugerfladen for Trædiamant-transportrøret for at indstille et filter.
Ved at lægge de ønskede genstande i pladserne kan du filtrere, hvad røret trækker ud.
Med indstillingerne Hvidliste, Sortliste og På skift kan du ændre, hvad dit filter gør:
Hvidliste: Kun de valgte genstande trækkes ud.
Sortliste: Alle andre genstande end de valgte trækkes ud.
På skift: Trækker genstandene ud i præcis den rækkefølge, de er valgt i filterpladserne ovenfor (fra venstre mod højre).

En Liste kan lægges i en filterplads for at give dig flere filtreringsmuligheder.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Energiforsyning"/>
Røret kan få energi fra enhver MJ-energikilde, f.eks. en motor, en energiadapter eller en rørpulsar.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
For hver energipuls, det får, trækker det genstande ud af det tilstødende inventar.
Jo mere energi en puls har, desto flere genstande trækkes ud pr. puls.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>