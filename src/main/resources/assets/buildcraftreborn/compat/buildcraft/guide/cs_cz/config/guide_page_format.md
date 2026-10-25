Stránky příručky se píší v podmnožině jazyka markdown.

<italic>(Většinu této stránky je ještě potřeba doplnit, zde je ale alespoň seznam základních tagů)</italic>

# Jednoduché samostatné tagy
Každý tag má tvar &lt;name arguments/&gt; – kde name je název tagu a koncové '/' značí, že tag stojí samostatně. Některé tagy mají také argumenty ve formě „párů klíč-hodnota“, například:
<guide_md>
"level"="42"
</guide_md>

## Nová stránka
&lt;new_page/&gt; přesune obsah, který následuje za ním, na další stránku.

## Odkazy
Vytvoří odkaz na jinou stránku příručky s názvem a ikonou odkazované stránky. Můžeš také vytvořit odkaz na předmět (ItemStack) nebo na spouštěč či akci (Statement), i když pro ně v příručce žádná stránka neexistuje.
Například:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
vytvoří:
<link to="buildcraftreborn:config/guide_entry_format"/>

nebo:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
vytvoří:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Kapitoly
&lt;chapter/&gt; vytvoří „nadpis kapitoly“, který se v seznamu kapitol zobrazí jako barevný nadpis. Má jeden možný argument: "level", což musí být celé číslo 0 nebo větší. Nadpisy můžeš vytvářet i několika znaky mřížky (#) jako v běžném markdownu. Počet znaků # (mínus jedna) odpovídá úrovni nadpisu.
Například:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
vytvoří:
### Kapitola úrovně 3
<chapter name="Další kapitola úrovně 3" level="2"/>

# Jednoduché párové tagy
Všechny tyto tagy musí obklopovat nějaký obsah: musí mít jak počáteční tag &lt;italic&gt;, tak koncový tag &lt;/italic&gt;
Všechny ovlivňují text mezi počátečním a koncovým tagem.

- <italic>kurzíva</italic>
- <bold>tučné</bold>
- <underline>podtržené</underline>
- <strikethrough>přeškrtnuté</strikethrough>
- <red>červená</red>
- <black>černá</black>
- <dark_blue>tmavě modrá</dark_blue>
- <dark_green>tmavě zelená</dark_green>
- <dark_aqua>tmavě tyrkysová</dark_aqua>
- <dark_red>tmavě červená</dark_red>
- <dark_purple>tmavě fialová</dark_purple>
- <gold>zlatá</gold>
- <gray>šedá</gray>
- <dark_gray>tmavě šedá</dark_gray>
- <blue>modrá</blue>
- <green>zelená</green> (zelená)
- <aqua>tyrkysová</aqua> (tyrkysová)
- <light_purple>světle fialová</light_purple>
- <yellow>žlutá</yellow> (žlutá)
- <white>bílá</white> (bílá)

Zelenou, tyrkysovou, žlutou ani bílou raději nepoužívej, protože tyto barvy jsou na stránce obvykle velmi špatně vidět.

# Přepínatelné tagy
Všechny následující tagy závisí na volbách, které si hráč může zapnout nebo vypnout.

## Příběh
Pokud je zaškrtnuté políčko „Zobrazit příběh [ ]“, skryje se vše v tagu &lt;no_lore&gt;, jinak se skryje vše v tagu &lt;lore&gt;.

Tag lore slouží k zobrazení informací „ze hry“, vyprávěných z pohledu hráče (jako by sám zkoumal, jak všechno funguje). Opačný tag slouží k předání informací formálnějším, encyklopedickým tónem – stručně a věcně.

Ve hře je to ve výchozím stavu zapnuté a v exportech vždy vypnuté.

## Tipy
Pokud je zaškrtnuté políčko „Zobrazit tipy [ ]“, skryje se vše v tagu &lt;no_hint&gt;, jinak se skryje vše v tagu &lt;hint&gt;.

Tag hint slouží k zobrazení dalších „tipů“: k čemu lze popisovanou věc využít nebo jak ji kombinovat s jinými bloky či předměty. V podstatě jde o „doporučené použití“ různých věcí. (Protože přicházet na to je součástí zábavy, není to povinné.) Proto je to ve výchozím stavu vypnuté, ale dá se to přepnout ve hře a v plánu je i přepínání při exportu.

## Podrobnosti
Pokud je klientská konfigurační volba "guideBookEnableDetail" nastavená na "true", skryje se vše v tagu &lt;no_detail&gt;, jinak se skryje vše v tagu &lt;detail&gt;.

Tag detail slouží k zobrazení všech čísel, která se používají při výpočtech různých hodnot, jako je průtok trubek, rychlost vytahování, frekvence pulzů atd.

Ve výchozím stavu je to vypnuté, ale dá se to přepnout ve hře a v plánu je i přepínání při exportu.

# Různé
## Recept (jednotné číslo)
Tento tag zobrazí první nalezený recept pro daný předmět. Například:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Recepty
Tento tag zobrazí všechny recepty pro daný předmět. Například:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Použití
Podobně jako u receptů zobrazí tento tag všechna použití daného předmětu. Například:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Recepty a použití
Tento tag v podstatě spojuje dva výše uvedené tagy do jednoho, například:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Skupina
Zobrazí skupiny odpovídající danému předmětu, spouštěči, akci atd.

## Obrázky
Zobrazí obrázek. Má 3 argumenty:
* src: zdrojové umístění obrázku.
* width: šířka, se kterou se obrázek zobrazí. Ve výchozím stavu jde o šířku zdrojového obrázku (to ale nemusí být to, co chceš)
* height: výška, se kterou se obrázek zobrazí.

Například:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
