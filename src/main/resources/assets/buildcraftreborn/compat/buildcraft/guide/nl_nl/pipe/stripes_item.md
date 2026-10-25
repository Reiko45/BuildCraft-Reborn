<lore>
Stel je een pijp voor die blokken kan plaatsen, breken en gebruiken. Dit is precies de pijp die je je voorstelt.
</lore>
<no_lore>
Een Gestreepte transportpijp breekt blokken, plaatst blokken, gebruikt voorwerpen die erdoorheen worden gevoerd en vervoert ze naar een aangesloten pijp of inventaris.
</no_lore>

<recipes stack="buildcraftreborn:pipe_stripes_item"/>

<chapter name="Gebruik van de pijp"/>
Als de pijp energie krijgt, breekt ze het blok direct ervoor en geeft het door aan de aangrenzende pijp of inventaris.
Wordt er een blok doorheen gevoerd, dan plaatst ze dat blok direct ervoor.
Wordt er een voorwerp doorheen gevoerd, dan probeert ze het te ‘gebruiken’ op het blok of de entiteit ervoor.

<chapter name="Richting van de pijp"/>
Een Gestreepte transportpijp moet in een richting ‘wijzen’ om invloed te hebben op het blok ervoor.
Zijn er meerdere pijpen of inventarissen op aangesloten, dan kan ze de wereld niet meer beïnvloeden.

<chapter name="Werking van de pijp"/>
Kunnen de voorwerpen nergens heen, dan vallen ze uit de pijp op de grond.
Met Pijpstops of door pijpen een andere kleur te verven kun je voorkomen dat pijpen verbinden.

<chapter name="Energievoorziening"/>
De pijp kan energie krijgen van elke MJ-energiebron, zoals een motor, een energieadapter of een pijppulsar.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Bij elke energiepuls die ze ontvangt, gebruikt de pijp die energie om te proberen het blok ervoor te breken.
Hoe meer energie een puls heeft, hoe meer energie er wordt gebruikt om het blok te breken.

<usages stack="buildcraftreborn:pipe_stripes_item"/>