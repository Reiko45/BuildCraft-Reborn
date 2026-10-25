<lore>
想把流体抽出来运到别处？只要为它供能，然后看着流体流走就行。
</lore>
<no_lore>
木质流体管道在获得能量时，可以从储罐中提取流体。
</no_lore>

<recipes stack="buildcraftreborn:pipe_wood_fluid"/>

<chapter name="管道机制"/>
获得能量时，它会提取流体，并把流体输送到其他相连的储罐/管道。
有些机器无需能量就能直接把流体输出到木质流体管道中。
流体会从管道的实心面被提取，并流向透明的朝向面。
如果有多个透明的朝向面，流体会平均分流到各个方向。

有些机器无需能量就能直接把流体输出到木质流体管道中。
使用管塞或把管道涂成不同颜色，可以阻止管道相互连接。

<chapter name="供能"/>
它可以从任何 MJ 能量源获得能量，例如：引擎、能量接收器或管道脉冲。
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
每接收到一次能量脉冲，它就会从相邻的储罐中提取流体。
脉冲的能量越大，每次脉冲提取的流体就越多。

<no_detail>
木质流体管道以每秒 0.2 桶的速度向相邻的管道和储罐输送流体。
流体管道内部有储液空间，每一面可容纳 1 桶，中心也可容纳 1 桶。
</no_detail>

<usages stack="buildcraftreborn:pipe_wood_fluid"/>