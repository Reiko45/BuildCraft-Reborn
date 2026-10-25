<lore>
Zamislite cijev koja može postavljati, razbijati i koristiti blokove. Ovo je upravo cijev koju zamišljate.
</lore>
<no_lore>
Prugasta transportna cijev služi za razbijanje i postavljanje blokova, korištenje predmeta koji kroz nju prolaze te njihov prijenos u spojenu cijev ili inventar.
</no_lore>

<recipes stack="buildcraftreborn:pipe_stripes_item"/>

<chapter name="Rad cijevi"/>
Kada je napajana, razbija blok izravno ispred sebe i predaje ga susjednoj cijevi ili inventaru.
Kada kroz nju prolazi blok, postavlja ga izravno ispred sebe.
Kada kroz nju prolazi predmet, pokušava ga „upotrijebiti“ na bloku ili entitetu ispred sebe.

<chapter name="Smjer cijevi"/>
Prugasta transportna cijev mora biti „usmjerena“ u nekom smjeru da bi mogla djelovati na blok ispred sebe.
Ako je na nju spojeno više cijevi ili inventara, gubi sposobnost djelovanja na svijet.

<chapter name="Mehanika cijevi"/>
Ako predmeti nemaju kamo otići, ispadaju iz cijevi na tlo.
Spajanje cijevi možete spriječiti Cijevnim čepovima ili bojanjem cijevi u različite boje.

<chapter name="Napajanje"/>
Energiju može primati iz bilo kojeg izvora MJ, primjerice iz motora, Strujnog adaptera ili Cijevnog pulsara.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Pri svakom primljenom energetskom pulsu iskorištava primljenu energiju i pokušava razbiti blok ispred sebe.
Što puls nosi više energije, to se više energije koristi za razbijanje bloka.

<usages stack="buildcraftreborn:pipe_stripes_item"/>