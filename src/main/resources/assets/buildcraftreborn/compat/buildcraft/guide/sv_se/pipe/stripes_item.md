<lore>
Tänk dig ett rör som kan placera block, ha sönder block och använda block. Det här är röret du föreställer dig.
</lore>
<no_lore>
Ett transportrör av ränder används för att ha sönder block, placera block och använda föremål som matas genom det, och för att transportera dem till ett anslutet rör/förråd.
</no_lore>

<recipes stack="buildcraftreborn:pipe_stripes_item"/>

<chapter name="Rörets drift"/>
När det får energi har det sönder blocket direkt framför sig och skickar det vidare till det intilliggande röret eller förrådet.
När ett block matas genom det placerar det blocket direkt framför sig.
När ett föremål matas genom det försöker det ”använda” föremålet på blocket/entiteten framför sig.

<chapter name="Rörets riktning"/>
Ett transportrör av ränder måste ”peka” åt ett håll för att kunna påverka blocket framför sig.
Med flera rör/förråd anslutna förlorar det förmågan att påverka världen.

<chapter name="Rörets funktion"/>
Om föremålen inte har någonstans att ta vägen släpps de ut ur röret på marken.
Med rörpluggar, eller genom att måla rören i olika färger, kan du hindra rör från att ansluta.

<chapter name="Energiförsörjning"/>
Det kan få energi från alla MJ-energikällor, till exempel en motor, en energiadapter eller en rörpulserare.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
För varje energipuls det får använder det den mottagna energin för att försöka ha sönder blocket framför sig.
Ju mer energi en puls har, desto mer energi används för att ha sönder blocket.

<usages stack="buildcraftreborn:pipe_stripes_item"/>