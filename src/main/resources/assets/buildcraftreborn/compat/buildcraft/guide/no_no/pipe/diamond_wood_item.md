<lore>
Vil du trekke ut bestemte gjenstander og transportere dem et annet sted? Bare gi røret energi og se dem flyte.
</lore>
<no_lore>
Et Tre-diamant-transportrør brukes til å trekke ut gjenstandene som filteret har valgt, fra et inventar når det får energi.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="Slik virker røret"/>
Når røret får energi, trekker det ut gjenstandene som er valgt i filteret, og transporterer dem til andre tilkoblede inventarer eller rør.
Gjenstandene trekkes ut gjennom den massive siden av røret og transporteres mot de gjennomsiktige sidene.
Har røret flere gjennomsiktige sider, beveger gjenstandene seg i en tilfeldig retning.
Hvis gjenstandene ikke har noe sted å dra, faller de ut av røret og ned på bakken.

Noen maskiner kan automatisk sende gjenstander rett inn i et Tre-diamant-transportrør uten at røret trenger energi (filteret gjelder da ikke).
Med Rørplugger eller ved å farge rør i ulike farger kan du hindre rør i å koble seg sammen.

<chapter name="Filtrering"/>
Åpne grensesnittet til Tre-diamant-transportrøret for å sette opp et filter.
Ved å legge gjenstandene du vil ha, i plassene kan du filtrere hva røret trekker ut.
Med innstillingene Hviteliste, Svarteliste og Etter tur kan du endre hva filteret ditt gjør:
Hviteliste: Bare de valgte gjenstandene trekkes ut.
Svarteliste: Alle andre gjenstander enn de valgte trekkes ut.
Etter tur: Trekker ut gjenstandene i nøyaktig den rekkefølgen de er valgt i filterplassene ovenfor (fra venstre mot høyre).

En Liste kan legges i en filterplass for å gi deg flere filtreringsmuligheter.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Energiforsyning"/>
Røret kan få energi fra enhver MJ-energikilde, for eksempel en motor, en energiadapter eller en rørpulsar.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
For hver energipuls det får, trekker det gjenstander ut av det tilstøtende inventaret.
Jo mer energi en puls har, desto flere gjenstander trekkes ut per puls.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>