<lore>
At bruge en port til at trække bestemte genstande ud er den seneste tekniske revolution inden for filtreringsteknologi.
</lore>
<no_lore>
Et Emzuli-transportrør er et portstyret rør, der bruges til at trække de genstande, filteret har valgt, ud af et inventar, når det får energi.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="Sådan virker røret"/>
Når røret får energi, trækker det de genstande ud, der er valgt i filteret, og transporterer dem til andre tilsluttede inventarer eller rør.
Genstandene trækkes ud gennem rørets massive side og transporteres mod de gennemsigtige sider.
Har røret flere gennemsigtige sider, bevæger genstandene sig i en tilfældig retning.
Hvis genstandene ikke har nogen steder at tage hen, falder de ud af røret og ned på jorden.

Nogle maskiner kan automatisk sende genstande direkte ind i et Emzuli-rør, uden at røret skal have energi (filtrene gælder så ikke).
Med Rørpropper eller ved at farve rør i forskellige farver kan du forhindre rør i at forbinde sig.

<chapter name="Filtrering"/>
Brugerfladen for Emzuli-transportrøret har fire farver til fire separate filtre.
Ved at lægge genstande i pladserne bestemmer du, hvad hvert filter trækker ud.
Farven på penslerne ved siden af pladserne bestemmer, hvilken farve genstandene bliver farvet, når de trækkes ud, og den kan ændres ved at klikke på penslerne.
For at vælge, hvilket af de fire filtre der trækkes ud fra, skal der sidde en port på røret med en farvet udtræksindstilling som handling. F.eks. Redstone-signal - > Rød udtræksindstilling.

En Liste kan lægges i en filterplads for at give dig flere filtreringsmuligheder.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Energiforsyning"/>
Røret kan få energi fra enhver MJ-energikilde, f.eks. en motor, en energiadapter eller en rørpulsar.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
For hver energipuls, det får, trækker det genstande ud af det tilstødende inventar.
Jo mere energi en puls har, desto flere genstande trækkes ud pr. puls.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>