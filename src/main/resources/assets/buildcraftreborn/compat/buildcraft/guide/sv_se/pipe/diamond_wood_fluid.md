<lore>
Vill du ta ut en viss vätska och transportera den någon annanstans? Ge röret bara energi och se vätskan flöda.
</lore>
<no_lore>
Ett vätskerör av trädiamant används för att extrahera utvalda vätskor ur en tank när det får energi.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_fluid"/>

<chapter name="Rörets funktion"/>
När det får energi extraherar det den vätska som filtret har valt till alla andra anslutna tankar/rör.
Vätskorna extraheras från rörets solida sida och vidare mot de genomskinliga sidorna.
Om det har flera genomskinliga sidor delas vätskorna upp jämnt åt varje håll.

Vissa maskiner kan mata ut vätskor direkt i det utan att det behöver energi (då tas ingen hänsyn till filtret).
Med rörpluggar, eller genom att måla rören i olika färger, kan du hindra rör från att ansluta.

<chapter name="Filtrering"/>
Om du öppnar gränssnittet för vätskeröret av trädiamant kan du ställa in ett filter.
Genom att lägga en hink (eller annan vätskebehållare) med den vätska du vill ha i platserna kan du filtrera vad det extraherar.
Du kan använda alternativen Vitlista och Svartlista för att ändra vad filtret gör:
Vitlista: bara de valda vätskorna extraheras.
Svartlista: alla vätskor utom den valda vätskan extraheras.

<chapter name="Energiförsörjning"/>
Det kan få energi från alla MJ-energikällor, till exempel en motor, en energiadapter eller en rörpulserare.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
För varje energipuls det får extraherar det från den intilliggande tanken.
Ju mer energi en puls har, desto mer vätska extraheras per puls.

<no_detail>
Vätskeröret av trädiamant transporterar vätskor till intilliggande rör och tankar med 1,6 hinkar per sekund.
Vätskerör har en intern vätsketank som rymmer 1 hink per sida och 1 hink i mitten.
</no_detail>

<usages stack="buildcraftreborn:pipe_diamond_wood_fluid"/>