<lore>
Vill du ta ut vissa föremål och transportera dem någon annanstans? Ge röret bara energi och se föremålen flöda.
</lore>
<no_lore>
Ett transportrör av trädiamant används för att extrahera föremål som dess filter har valt ut ur ett förråd när det får energi.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="Rörets funktion"/>
När det får energi extraherar det de föremål som valts i filtret och transporterar dem till alla andra anslutna förråd/rör.
Föremålen extraheras från rörets solida sida och transporteras mot de genomskinliga sidorna.
Om det har flera genomskinliga sidor färdas föremålen åt ett slumpmässigt håll.
Om föremålen inte har någonstans att ta vägen släpps de extraherade föremålen ut ur röret på marken.

Vissa maskiner kan automatiskt mata ut föremål direkt i ett transportrör av trädiamant utan att röret behöver energi (då tas ingen hänsyn till filtret).
Med rörpluggar, eller genom att måla rören i olika färger, kan du hindra rör från att ansluta.

<chapter name="Filtrering"/>
Om du öppnar gränssnittet för transportröret av trädiamant kan du ställa in ett filter.
Genom att lägga föremålen du vill ha i platserna kan du filtrera vad det extraherar.
Du kan använda alternativen Vitlista, Svartlista eller Turordning för att ändra vad filtret gör:
Vitlista: bara de valda föremålen extraheras.
Svartlista: alla föremål utom de valda föremålen extraheras.
Turordning: extraherar föremålen i exakt den ordning som valts i filterplatserna ovan (från vänster till höger)

En lista kan användas i en filterplats för att ge dig utökade filtreringsmöjligheter.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Energiförsörjning"/>
Det kan få energi från alla MJ-energikällor, till exempel en motor, en energiadapter eller en rörpulserare.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
För varje energipuls det får extraherar det föremål från det intilliggande förrådet.
Ju mer energi en puls har, desto fler föremål extraheras per puls.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>