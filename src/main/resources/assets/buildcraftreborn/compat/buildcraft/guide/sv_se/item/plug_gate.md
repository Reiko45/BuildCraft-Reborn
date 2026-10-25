<lore>
Redstone tar upp mycket plats nuförtiden. Varför inte få bättre kontroll över systemet på mindre yta med hjälp av rör? Sätt bara en grind på ett rör och börja tänka.
</lore>
<no_lore>
Grinden är en uppgraderbar anslutningsbar komponent som ger ett logiskt ramverk för att hjälpa dig att styra ditt rörsystem.
</no_lore>

<recipes stack="buildcraftreborn:plug_gate"/>

<chapter name="Placering av grindar"/>
En grind är en anslutningsbar komponent, så den kan bara placeras på rör, men den kan placeras på vilken sida av röret som helst.
Inget rör som placeras intill den sidan ansluter.

<chapter name="Grindnivåer"/>
Grindar finns i olika nivåer som ger olika många platser:
Enkel grind - Ger 1 uppsättning platser (kan inte modifieras).
Järngrind - Ger 2 uppsättningar platser.
Nethertegelgrind - Ger 4 uppsättningar platser.
Guldgrind - Ger 8 uppsättningar platser.

<chapter name="Grindmodifierare"/>
Grindar kan modifieras för att ge parameterplatser:
Lapis lazuli-expansion - Ger 1 parameterplats till varje utlösare.
Kvartsexpansion - Ger 1 parameterplats till varje uppsättning (halverar antalet platser som grinden har).
Diamantexpansion - Ger 3 parameterplatser till varje uppsättning (halverar antalet platser som grinden har).

<chapter name="Grindvarianter"/>
Med en OCH-grind måste <bold>alla</bold> anslutna utlösare vara aktiva innan anslutningen till åtgärderna aktiveras.
Med en ELLER-grind måste <bold>en eller flera</bold> anslutna utlösare vara aktiva innan anslutningen till åtgärderna aktiveras.

<chapter name="Utlösare"/>
Ikonerna till vänster kallas utlösare. De dras till platserna på gränssnittets vänstra sida.
Olika utlösare känner av olika saker, var och en med sina egna villkor. När en utlösares villkor uppfylls blir den intilliggande anslutningen aktiv.

<chapter name="Åtgärder"/>
Ikonerna till höger kallas åtgärder. De dras till platserna på gränssnittets högra sida.
Olika åtgärder utför olika uppgifter. När den intilliggande anslutningen är aktiv utförs de anslutna åtgärderna.

<chapter name="Parametrar"/>
När parameterplatser är installerade blir extra platser tillgängliga bredvid vissa utlösare/åtgärder. 
De här extra platserna kan ge extra funktioner som gör dina utlösare/åtgärder mer specifika.
I de här extra platserna klickar du antingen på platsen för att komma åt fler alternativ, eller lägger in föremål som filter.

<chapter name="Anslutningar"/>
Linjerna i mitten används för att koppla utlösarna till åtgärderna så att de kan samarbeta.
Du kan klicka på dem för att koppla ihop eller koppla isär dem, så att flera utlösare/åtgärder krävs osv.
När en utlösare uppfylls tänds linjen i mitten (bara delvis om flera utlösare krävs).
När en linje är tänd utförs de åtgärder som den är kopplad till.

<usages stack="buildcraftreborn:plug_gate"/>