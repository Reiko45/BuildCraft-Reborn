<lore>
想把某些物品取出来运到别处？只要为它供能，然后看着物品流走就行。
</lore>
<no_lore>
木质钻石管道在获得能量时，可以从容器中提取由其过滤器选定的物品。
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="管道机制"/>
获得能量时，它会提取过滤器中所选的物品，并输送到其他相连的容器/管道。
物品会从管道的实心面被提取，并输送向透明的朝向面。
如果有多个透明的朝向面，物品会随机选择一个方向移动。
如果物品无处可去，被提取的物品就会从管道中掉落到地上。

有些机器无需为管道供能，就能自动把物品直接输出到木质钻石管道中（此时不会遵循过滤器）。
使用管塞或把管道涂成不同颜色，可以阻止管道相互连接。

<chapter name="过滤"/>
打开木质钻石管道的界面即可设置过滤器。
把想要的物品放入槽位，就能过滤它所提取的物品。
你可以用白名单、黑名单或轮询选项来改变过滤器的作用：
白名单：只提取所选的物品。
黑名单：提取除所选物品以外的任何物品。
轮询：严格按照上方过滤槽中所选的顺序（从左到右）提取物品。

在过滤槽中可以使用列表，以获得更多过滤选项。
<link to="buildcraftreborn:item/list"/> 

<chapter name="供能"/>
它可以从任何 MJ 能量源获得能量，例如：引擎、能量接收器或管道脉冲。
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
每接收到一次能量脉冲，它就会从相邻的容器中提取物品。
脉冲的能量越大，每次脉冲提取的物品就越多。

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>