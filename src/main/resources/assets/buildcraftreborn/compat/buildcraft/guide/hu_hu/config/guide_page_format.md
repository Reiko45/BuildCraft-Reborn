A kézikönyv összes oldala a markdown egy részhalmazával készül.

<italic>(Az oldal nagy része még kidolgozásra vár, de addig is itt van az alapvető címkék listája)</italic>

# Egyszerű önálló címkék
Minden címke &lt;name arguments/&gt; alakú, ahol a name a címke neve, a záró '/' pedig azt jelzi, hogy a címke önálló. Egyes címkéknek „kulcs-érték párok” formájában argumentumaik is vannak, például:
<guide_md>
"level"="42"
</guide_md>

## Új oldal
A &lt;new_page/&gt; az utána következő tartalmat a következő oldalra helyezi át.

## Hivatkozások
Ezek egy másik kézikönyvoldalra mutató hivatkozást hoznak létre, a hivatkozott oldal címével és ikonjával. Hivatkozhatsz egy ItemStackre vagy Statementre is, akkor is, ha nincs hozzájuk kézikönyvoldal.
Például:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
eredménye:
<link to="buildcraftreborn:config/guide_entry_format"/>

vagy:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
eredménye:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Fejezetek
A &lt;chapter/&gt; egy „fejezetcímet” hoz létre, amely színes címsorként jelenik meg a fejezetlistában. Egyetlen lehetséges argumentuma a "level", amelynek 0 vagy annál nagyobb egész számnak kell lennie. A címsorokat a szokásos markdownhoz hasonlóan több kettőskereszttel (#) is létrehozhatod. A # jelek száma mínusz egy adja meg a címsor szintjét.
Például:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
eredménye:
### 3. szintű fejezet
<chapter name="Egy másik 3. szintű fejezet" level="2"/>

# Egyszerű páros címkék
Ezeknek a címkéknek valamilyen tartalmat kell közrefogniuk, és kell, hogy legyen nyitócímkéjük (&lt;italic&gt;) és zárócímkéjük (&lt;/italic&gt;) is.
Mindegyik a nyitó- és a zárócímke közötti szövegre hat.

- <italic>dőlt</italic> (italic)
- <bold>félkövér</bold> (bold)
- <underline>aláhúzott</underline> (underline)
- <strikethrough>áthúzott</strikethrough> (strikethrough)
- <red>piros</red> (red)
- <black>fekete</black> (black)
- <dark_blue>sötétkék</dark_blue> (dark_blue)
- <dark_green>sötétzöld</dark_green> (dark_green)
- <dark_aqua>sötét türkiz</dark_aqua> (dark_aqua)
- <dark_red>sötétvörös</dark_red> (dark_red)
- <dark_purple>sötétlila</dark_purple> (dark_purple)
- <gold>arany</gold> (gold)
- <gray>szürke</gray> (gray)
- <dark_gray>sötétszürke</dark_gray> (dark_gray)
- <blue>kék</blue> (blue)
- <green>zöld</green> (green)
- <aqua>türkiz</aqua> (aqua)
- <light_purple>világoslila</light_purple> (light_purple)
- <yellow>sárga</yellow> (yellow)
- <white>fehér</white> (white)

Nem jó ötlet a zöld, a türkiz, a sárga vagy a fehér használata, mert ezeket a színeket általában nagyon nehéz kivenni az oldalon.

# Kapcsolható címkék
Az alábbi címkék mind beállításfüggők: a játékos be- és kikapcsolhatja őket.

## Háttértörténet
Ha a „Háttértörténet mutatása [ ]” jelölőnégyzet be van pipálva, a &lt;no_lore&gt; címkében lévő tartalom rejtve marad, ellenkező esetben a &lt;lore&gt; címkében lévő.

A lore címke a „játékon belüli” kézikönyv-információk megjelenítésére való, a játékos szemszögéből elmesélve (mintha épp most derítené ki, hogyan működik minden). Az ellentéte formálisabb, a Wikipédiához hasonló hangvételű, és tisztán, egyszerűen közli az információt.

A játékban ez alapértelmezetten be van kapcsolva, az exportokban viszont mindig ki van kapcsolva.

## Tippek
Ha a „Tippek mutatása [ ]” jelölőnégyzet be van pipálva, a &lt;no_hint&gt; címkében lévő tartalom rejtve marad, ellenkező esetben a &lt;hint&gt; címkében lévő.

A hint címke további „tippek” megjelenítésére való: mire használható a leírt dolog, vagy hogyan kombinálható más blokkokkal és tárgyakkal. Lényegében a különféle dolgok „ajánlott felhasználási módjairól” szól. (Mivel ennek kiderítése a móka része, nem kötelező. Ezért alapértelmezetten ki van kapcsolva, de a játékban bekapcsolható, és a tervek szerint exportáláskor is az lesz.)

## Részletek
Ha a "guideBookEnableDetail" kliensoldali konfigurációs beállítás értéke "true", a &lt;no_detail&gt; címkében lévő tartalom rejtve marad, ellenkező esetben a &lt;detail&gt; címkében lévő.

A detail címke a különféle számításokhoz használt összes szám megjelenítésére való, például a csövek áteresztőképességéhez, a kinyerési sebességhez, az impulzusok gyakoriságához stb.

Ez alapértelmezetten ki van kapcsolva, de a játékban be- és kikapcsolható, és a tervek szerint exportáláskor is az lesz.

# Egyéb
## Recept (egyes szám)
Ez a címke az adott tárgyhoz talált első receptet jeleníti meg. Például:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Receptek
Ez a címke az adott tárgy összes receptjét megjeleníti. Például:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Felhasználások
A receptekhez hasonlóan ez az adott tárgy összes felhasználását megjeleníti. Például:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Receptek és felhasználások
Ez gyakorlatilag egyesíti a fenti két címkét, például:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Csoport
Az adott tárgyhoz, statementhez stb. tartozó csoportokat jeleníti meg.

## Képek
Egy képet jelenít meg. 3 argumentuma van:
* src: a kép forrásának helye.
* width: a megjelenített kép szélessége. Alapértelmezetten a forráskép szélessége (ez azonban nem biztos, hogy megfelel neked).
* height: a megjelenített kép magassága.

Például:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
