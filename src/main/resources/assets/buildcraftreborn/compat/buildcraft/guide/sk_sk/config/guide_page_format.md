Všetky stránky príručky sú napísané v podmnožine jazyka markdown.

<italic>(Väčšinu tejto stránky ešte treba doplniť, tu je však zoznam základných tagov)</italic>

# Jednoduché samostatné tagy
Každý tag má tvar &lt;name arguments/&gt; – kde name je názov tagu a koncové '/' znamená, že tag stojí samostatne. Niektoré tagy majú aj argumenty v podobe „dvojíc kľúč – hodnota“, napríklad:
<guide_md>
"level"="42"
</guide_md>

## Nová stránka
&lt;new_page/&gt; presunie obsah, ktorý za ním nasleduje, na ďalšiu stránku.

## Odkazy
Tieto tagy vytvoria odkaz na inú stránku príručky s názvom a ikonou odkazovanej stránky. Môžete tiež vytvoriť odkaz na ItemStack alebo Statement, aj keď nemajú vlastnú stránku v príručke.
Napríklad:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
vytvorí:
<link to="buildcraftreborn:config/guide_entry_format"/>

alebo:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
vytvorí:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Kapitoly
&lt;chapter/&gt; vytvorí „nadpis kapitoly“, ktorý sa v zozname kapitol zobrazí ako farebný nadpis. Má jeden možný argument: "level", čo musí byť celé číslo 0 alebo väčšie. Nadpisy môžete vytvárať aj pomocou viacerých znakov mriežky (#), rovnako ako v bežnom markdowne. Počet znakov # (mínus jeden) udáva úroveň nadpisu.
Napríklad:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
vytvorí:
### Kapitola úrovne 3
<chapter name="Ďalšia kapitola úrovne 3" level="2"/>

# Jednoduché párové tagy
Všetky tieto tagy musia obklopovať nejaký obsah a musia mať počiatočný tag &lt;italic&gt; aj koncový tag &lt;/italic&gt;
Všetky ovplyvňujú text medzi počiatočným a koncovým tagom.

- <italic>kurzíva</italic>
- <bold>tučné</bold>
- <underline>podčiarknuté</underline>
- <strikethrough>prečiarknuté</strikethrough>
- <red>červená</red>
- <black>čierna</black>
- <dark_blue>tmavomodrá</dark_blue>
- <dark_green>tmavozelená</dark_green>
- <dark_aqua>tmavotyrkysová</dark_aqua>
- <dark_red>tmavočervená</dark_red>
- <dark_purple>tmavofialová</dark_purple>
- <gold>zlatá</gold>
- <gray>sivá</gray>
- <dark_gray>tmavosivá</dark_gray>
- <blue>modrá</blue>
- <green>zelená</green> (zelená)
- <aqua>tyrkysová</aqua> (tyrkysová)
- <light_purple>svetlofialová</light_purple>
- <yellow>žltá</yellow> (žltá)
- <white>biela</white> (biela)

Nie je dobrý nápad používať zelenú, tyrkysovú, žltú ani bielu, pretože tieto farby sú na stránke zvyčajne veľmi zle viditeľné.

# Prepínateľné tagy
Všetky nasledujúce tagy závisia od nastavení a hráč ich môže zapnúť alebo vypnúť.

## Príbeh
Ak je začiarknuté políčko „Zobraziť príbeh [ ]“, skryje sa všetko v tagu &lt;no_lore&gt;, inak sa skryje všetko v tagu &lt;lore&gt;.

Tag lore slúži na informácie podané „v rámci hry“, rozprávané z pohľadu hráča (akoby práve skúmal, ako všetko funguje). Opak slúži na formálnejší tón v štýle Wikipédie, ktorý informácie podáva jednoducho a priamo.

V hre je táto možnosť predvolene zapnutá a pri exporte vždy vypnutá.

## Tipy
Ak je začiarknuté políčko „Zobraziť tipy [ ]“, skryje sa všetko v tagu &lt;no_hint&gt;, inak sa skryje všetko v tagu &lt;hint&gt;.

Tag hint slúži na ďalšie „tipy“, na čo sa dá opisovaná vec použiť, alebo ako ju rôzne kombinovať s inými blokmi či predmetmi. V podstate ide o „odporúčané použitia“ rôznych vecí. (Keďže prísť na to sám je súčasťou zábavy, nie je to povinné.) Preto je táto možnosť predvolene vypnutá, no v hre sa dá zapnúť a plánuje sa aj možnosť prepínať ju pri exporte.

## Podrobnosti
Ak je klientska konfiguračná možnosť "guideBookEnableDetail" nastavená na "true", skryje sa všetko v tagu &lt;no_detail&gt;, inak sa skryje všetko v tagu &lt;detail&gt;.

Tag detail slúži na zobrazenie všetkých čísel použitých pri výpočte rôznych hodnôt, napríklad prietoku rúr, rýchlosti vyťahovania, frekvencie pulzov a pod.

Táto možnosť je predvolene vypnutá, no v hre sa dá zapnúť a plánuje sa aj možnosť prepínať ju pri exporte.

# Rôzne
## Recept (jednotné číslo)
Tento tag zobrazí prvý nájdený recept pre daný stoh predmetov. Napríklad:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Recepty
Tento tag zobrazí všetky recepty pre daný stoh predmetov. Napríklad:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Použitia
Podobne ako recepty zobrazí tento tag všetky použitia daného stohu predmetov. Napríklad:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Recepty a použitia
Tento tag v podstate spája dva predchádzajúce tagy do jedného, napríklad:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Skupina
Zobrazí skupiny prislúchajúce danému predmetu, statementu a pod.

## Obrázky
Zobrazí obrázok. Má 3 argumenty:
* src: Umiestnenie zdrojového obrázka.
* width: Šírka, s akou sa obrázok zobrazí. Predvolene je to šírka zdrojového obrázka (čo však nemusí byť to, čo chcete).
* height: Výška, s akou sa obrázok zobrazí.

Napríklad:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
