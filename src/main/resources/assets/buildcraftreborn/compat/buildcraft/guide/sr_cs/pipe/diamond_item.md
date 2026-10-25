<lore>
Biranje koji predmeti mogu kuda da idu može biti korisno za vaš sistem.
</lore>
<no_lore>
Dijamantna transportna cev prenosi predmete iz cevi u smeru zadatom filterom, u susednu cev ili inventar.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_item"/>

<chapter name="Podešavanje filtera"/>
Svaka strana Dijamantne transportne cevi označena je drugom bojom.
Kada otvorite interfejs Dijamantne transportne cevi, možete da podesite filter.
Ako željene predmete stavite u slotove određene boje, određujete koji predmeti mogu da idu u kom smeru.
Ako u jednoj boji imate više istih predmeta, dajete „težinu“ smeru u kom će predmeti ići.

Lista može da se stavi u slot za filtriranje i tako pruži proširene mogućnosti filtriranja.
<link to="buildcraftreborn:item/list"/>

<chapter name="Mehanika cevi"/>
Ako predmeti mogu da idu u više smerova, raspoređuju se među njima (u zavisnosti od težine koju ste im dali).
Svaki dozvoljeni smer bez podešenog filtera propušta bilo koji predmet, ali smerovi sa filterom imaju prednost.
Predmeti se ne vraćaju u smeru iz kog su došli, već idu samo u drugim smerovima.
Ako nema povezane cevi ili je susedni inventar pun, a predmet nema kuda drugde da ide, ispada iz cevi na zemlju.

Neke mašine mogu automatski da izbacuju predmete direktno u Dijamantnu transportnu cev, bez cevi za izvlačenje.
Cevni čepovi ili bojenje cevi različitim bojama mogu da spreče povezivanje cevi.

<usages stack="buildcraftreborn:pipe_diamond_item"/>