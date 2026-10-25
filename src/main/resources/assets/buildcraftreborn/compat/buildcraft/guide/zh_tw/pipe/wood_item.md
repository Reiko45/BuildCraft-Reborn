<lore>
想把物品抽出來送到別處嗎？只要供給能量，然後看著它們流動就好。
</lore>
<no_lore>
木導管在獲得能量時，會從容器中抽取物品。
</no_lore>

<recipes stack="buildcraftreborn:pipe_wood_item"/>

<chapter name="導管機制"/>
獲得能量時，它會抽出物品，並將它們輸送到其他相連的容器或導管。
物品會從導管實心的面抽出，並輸送往透明的面。
若它有多個透明的面，物品會在這些面之間隨機移動。
若物品無處可去，抽出的物品就會從導管掉落到地上。

有些機器可以自動將物品直接輸出到木導管中，導管不需要能量。
使用管塞或將導管塗上不同的顏色，可以阻止導管互相連接。

<chapter name="供能"/>
它可以從任何 MJ 能量來源獲得能量，例如：引擎、能量轉接器或導管脈衝器。
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
每收到一次能量脈衝，它就會從相鄰的容器抽取物品。
脈衝的能量越多，每次脈衝抽取的物品就越多。

<usages stack="buildcraftreborn:pipe_wood_item"/>