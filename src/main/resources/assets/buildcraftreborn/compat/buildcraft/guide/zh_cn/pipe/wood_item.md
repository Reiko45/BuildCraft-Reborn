<lore>
想把物品取出来运到别处？只要为它供能，然后看着物品流走就行。
</lore>
<no_lore>
木质管道在获得能量时，可以从容器中提取物品。
</no_lore>

<recipes stack="buildcraftreborn:pipe_wood_item"/>

<chapter name="管道机制"/>
获得能量时，它会提取物品，并输送到其他相连的容器/管道。
物品会从管道的实心面被提取，并输送向透明的朝向面。
如果有多个透明的朝向面，物品会在它们之间随机移动。
如果物品无处可去，被提取的物品就会从管道中掉落到地上。

有些机器无需为管道供能，就能自动把物品直接输出到木质管道中。
使用管塞或把管道涂成不同颜色，可以阻止管道相互连接。

<chapter name="供能"/>
它可以从任何 MJ 能量源获得能量，例如：引擎、能量接收器或管道脉冲。
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
每接收到一次能量脉冲，它就会从相邻的容器中提取物品。
脉冲的能量越大，每次脉冲提取的物品就越多。

<usages stack="buildcraftreborn:pipe_wood_item"/>