<lore>
想象一根能放置方块、破坏方块、使用方块的管道。你想象的正是这根管道。
</lore>
<no_lore>
条纹管道用于破坏方块、放置方块、使用送入其中的物品，并把它们输送到相连的管道/容器中。
</no_lore>

<recipes stack="buildcraftreborn:pipe_stripes_item"/>

<chapter name="管道运作"/>
获得能量时，它会破坏正前方的方块，并将其传给相邻的管道或容器。
当方块被送入其中时，它会把该方块放置在正前方。
当物品被送入其中时，它会尝试对前方的方块/实体“使用”该物品。

<chapter name="管道方向"/>
条纹管道必须“指向”某个方向，才能作用于其前方的方块。
如果有多个管道/容器与它相连，它就会失去作用于世界的能力。

<chapter name="管道机制"/>
如果移动中的物品无处可去，它们就会从管道中掉落到地上。
使用管塞或把管道涂成不同颜色，可以阻止管道相互连接。

<chapter name="供能"/>
它可以从任何 MJ 能量源获得能量，例如：引擎、能量接收器或管道脉冲。
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
每接收到一次能量脉冲，它就会用收到的能量尝试破坏前方的方块。
脉冲的能量越大，用于破坏方块的能量就越多。

<usages stack="buildcraftreborn:pipe_stripes_item"/>