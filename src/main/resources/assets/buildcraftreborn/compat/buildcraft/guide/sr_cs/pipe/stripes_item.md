<lore>
Zamislite cev koja može da postavlja, razbija i koristi blokove. Ovo je upravo ta cev koju zamišljate.
</lore>
<no_lore>
Prugasta transportna cev razbija blokove, postavlja blokove, koristi predmete koji kroz nju prolaze i prenosi ih u povezanu cev ili inventar.
</no_lore>

<recipes stack="buildcraftreborn:pipe_stripes_item"/>

<chapter name="Rad cevi"/>
Kada se napaja, razbija blok direktno ispred sebe i prosleđuje ga u susednu cev ili inventar.
Kada kroz nju prođe blok, postavlja ga direktno ispred sebe.
Kada kroz nju prođe predmet, pokušava da ga „upotrebi“ na bloku ili entitetu ispred sebe.

<chapter name="Smer cevi"/>
Prugasta transportna cev mora biti „usmerena“ u nekom smeru da bi delovala na blok ispred sebe.
Ako je na nju povezano više cevi ili inventara, gubi sposobnost da deluje na svet.

<chapter name="Mehanika cevi"/>
Ako predmeti u prolazu nemaju kuda da idu, ispadaju iz cevi na zemlju.
Cevni čepovi ili bojenje cevi različitim bojama mogu da spreče povezivanje cevi.

<chapter name="Napajanje"/>
Može da prima energiju iz bilo kog izvora MJ, kao što su motor, Strujni adapter ili Cevni pulsar.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
Sa svakim impulsom energije koji primi, troši primljenu energiju i pokušava da razbije blok ispred sebe.
Što impuls nosi više energije, to se više energije troši na razbijanje bloka.

<usages stack="buildcraftreborn:pipe_stripes_item"/>