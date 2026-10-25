<lore>
Vyberať, ktoré predmety môžu ísť kam, sa vo vašom systéme môže hodiť.
</lore>
<no_lore>
Diamantová transportačná rúra prepravuje predmety z rúry smerom určeným jej filtrom do susednej rúry alebo inventára.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_item"/>

<chapter name="Nastavenie filtra"/>
Každá strana Diamantovej transportačnej rúry je označená inou farbou.
V rozhraní Diamantovej transportačnej rúry môžete nastaviť filter.
Vložením požadovaných predmetov do slotov určitej farby určíte, ktoré predmety môžu ísť ktorým smerom.
Ak do jednej farby vložíte viac kusov toho istého predmetu, pridáte tomuto smeru „váhu“.

Do filtračných slotov môžete vložiť aj Zoznam, ktorý rozšíri možnosti filtrovania.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Mechanika rúry"/>
Ak môžu predmety pokračovať viacerými smermi, rozdelia sa medzi ne (podľa váhy, ktorú im priradíte).
Každý platný smer bez nastaveného filtra prepustí akýkoľvek predmet, smery s filtrom však majú prednosť.
Predmety sa nevracajú smerom, odkiaľ prišli – pokračujú iba iným smerom.
Ak nie je pripojená žiadna rúra alebo je susedný inventár plný a predmet nemá kam inam ísť, vypadne z rúry na zem.

Niektoré stroje dokážu automaticky vydávať predmety priamo do Diamantovej transportačnej rúry, bez extrakčnej rúry.
Spájaniu rúr zabránite pomocou Napojení rúr alebo zafarbením rúr inou farbou.

<usages stack="buildcraftreborn:pipe_diamond_item"/>