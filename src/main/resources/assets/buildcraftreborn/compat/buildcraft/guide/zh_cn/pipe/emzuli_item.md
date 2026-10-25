<lore>
用门来提取特定物品，是过滤技术领域的最新革命。
</lore>
<no_lore>
靛翡翠管道是一种由门控制的管道，在获得能量时，可以从容器中提取由其过滤器选定的物品。
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="管道机制"/>
获得能量时，它会提取过滤器中所选的物品，并输送到其他相连的容器/管道。
物品会从管道的实心面被提取，并输送向透明的朝向面。
如果有多个透明的朝向面，物品会随机选择一个方向移动。
如果物品无处可去，被提取的物品就会从管道中掉落到地上。

有些机器无需为管道供能，就能自动把物品直接输出到靛翡翠管道中（此时不会遵循过滤器）。
使用管塞或把管道涂成不同颜色，可以阻止管道相互连接。

<chapter name="过滤"/>
打开靛翡翠管道的界面后，会看到对应四个独立过滤选项的四种颜色。
把物品放入槽位，即可设定每个过滤器提取什么物品。
槽位旁颜料刷的颜色决定了物品被提取时会被染成什么颜色，点击颜料刷即可更改颜色。
要选择使用四个过滤器中的哪一个进行提取，需要在管道上安装门，并设置带颜色的“提取预设”动作。例如：“红石信号开” → “红色提取预设”。

在过滤槽中可以使用列表，以获得更多过滤选项。
<link to="buildcraftreborn:item/list"/> 

<chapter name="供能"/>
它可以从任何 MJ 能量源获得能量，例如：引擎、能量接收器或管道脉冲。
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
每接收到一次能量脉冲，它就会从相邻的容器中提取物品。
脉冲的能量越大，每次脉冲提取的物品就越多。

<usages stack="buildcraftreborn:pipe_emzuli_item"/>