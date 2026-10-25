<lore>
Vil du trække en bestemt væske ud og transportere den et andet sted hen? Giv bare røret energi, og se væsken flyde.
</lore>
<no_lore>
Et Vandtæt trædiamantrør bruges til at trække udvalgte væsker ud af en tank, når det får energi.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_fluid"/>

<chapter name="Sådan virker røret"/>
Når røret får energi, trækker det den væske, filteret har valgt, ud til andre tilsluttede tanke eller rør.
Væskerne trækkes ud gennem rørets massive side og sendes mod de gennemsigtige sider.
Har røret flere gennemsigtige sider, fordeles væskerne ligeligt i alle retninger.

Nogle maskiner kan sende væsker direkte ind i røret, uden at det skal have energi (filteret gælder så ikke).
Med Rørpropper eller ved at farve rør i forskellige farver kan du forhindre rør i at forbinde sig.

<chapter name="Filtrering"/>
Åbn brugerfladen for Vandtæt trædiamantrør for at indstille et filter.
Ved at lægge en spand (eller en anden væskebeholder) med den ønskede væske i pladserne kan du filtrere, hvad røret trækker ud.
Med Hvidliste og Sortliste kan du ændre, hvad dit filter gør:
Hvidliste: Kun de valgte væsker trækkes ud.
Sortliste: Alle andre væsker end den valgte trækkes ud.

<chapter name="Energiforsyning"/>
Røret kan få energi fra enhver MJ-energikilde, f.eks. en motor, en energiadapter eller en rørpulsar.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
For hver energipuls, det får, trækker det væske ud af den tilstødende tank.
Jo mere energi en puls har, desto mere væske trækkes ud pr. puls.

<no_detail>
Et Vandtæt trædiamantrør transporterer væsker til tilstødende rør og tanke med 1,6 spande i sekundet.
Væskerør har en indbygget væsketank, der kan rumme 1 spand pr. side og 1 spand i midten.
</no_detail>

<usages stack="buildcraftreborn:pipe_diamond_wood_fluid"/>