<lore>
想像一下，有一種導管能放置方塊、破壞方塊，還能使用方塊。這就是你想像中的那種導管。
</lore>
<no_lore>
條紋導管用來破壞方塊、放置方塊、使用送入其中的物品，並將它們輸送到相連的導管或容器中。
</no_lore>

<recipes stack="buildcraftreborn:pipe_stripes_item"/>

<chapter name="導管運作"/>
獲得能量時，它會破壞正前方的方塊，並將其傳給相鄰的導管或容器。
當方塊被送入其中時，它會將方塊放置在正前方。
當物品被送入其中時，它會嘗試對前方的方塊／實體「使用」該物品。

<chapter name="導管方向"/>
條紋導管必須「指向」某個方向，才能影響其前方的方塊。
若有多條導管或多個容器與它相連，它就會失去影響世界的能力。

<chapter name="導管機制"/>
若移動中的物品無處可去，就會從導管掉落到地上。
使用管塞或將導管塗上不同的顏色，可以阻止導管互相連接。

<chapter name="供能"/>
它可以從任何 MJ 能量來源獲得能量，例如：引擎、能量轉接器或導管脈衝器。
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
每收到一次能量脈衝，它就會使用收到的能量，嘗試破壞前方的方塊。
脈衝的能量越多，用來破壞方塊的能量就越多。

<usages stack="buildcraftreborn:pipe_stripes_item"/>