<lore>
Å bruke en port til å trekke ut bestemte gjenstander er den siste tekniske revolusjonen innen filtreringsteknologi.
</lore>
<no_lore>
Et Emzuli-transportrør er et portstyrt rør som brukes til å trekke ut gjenstandene filteret har valgt, fra et inventar når det får energi.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="Slik virker røret"/>
Når røret får energi, trekker det ut gjenstandene som er valgt i filteret, og transporterer dem til andre tilkoblede inventarer eller rør.
Gjenstandene trekkes ut gjennom den massive siden av røret og transporteres mot de gjennomsiktige sidene.
Har røret flere gjennomsiktige sider, beveger gjenstandene seg i en tilfeldig retning.
Hvis gjenstandene ikke har noe sted å dra, faller de ut av røret og ned på bakken.

Noen maskiner kan automatisk sende gjenstander rett inn i et Emzuli-rør uten at røret trenger energi (filtrene gjelder da ikke).
Med Rørplugger eller ved å farge rør i ulike farger kan du hindre rør i å koble seg sammen.

<chapter name="Filtrering"/>
Grensesnittet til Emzuli-transportrøret har fire farger for fire separate filtre.
Ved å legge gjenstander i plassene bestemmer du hva hvert filter trekker ut.
Fargen på penslene ved siden av plassene bestemmer hvilken farge gjenstandene blir farget i når de trekkes ut, og den kan endres ved å klikke på penslene.
For å velge hvilket av de fire filtrene det trekkes ut fra, må det sitte en port på røret med en farget uttrekksinnstilling som handling. F.eks. Redstonesignal - > Rød uttrekksinnstilling.

En Liste kan legges i en filterplass for å gi deg flere filtreringsmuligheter.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Energiforsyning"/>
Røret kan få energi fra enhver MJ-energikilde, for eksempel en motor, en energiadapter eller en rørpulsar.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
For hver energipuls det får, trekker det gjenstander ut av det tilstøtende inventaret.
Jo mer energi en puls har, desto flere gjenstander trekkes ut per puls.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>