<lore>
借助光线感应器，你可以利用光的力量来控制门。
</lore>
<no_lore>
光线感应器是一种管道插件，用于检测其前方方块的亮度，并把数据提供给同一管道相邻面上的门。
</no_lore>

<recipes stack="buildcraftreborn:plug_light_sensor"/>

<chapter name="插件机制"/>
借助这些数据，相邻的门可以通过“明亮”和“黑暗”触发条件检测周围是亮还是暗。
这些“明亮”和“黑暗”触发条件可以通过门来触发动作。

<link to="buildcraftreborn:trigger/light_high"/> 
<link to="buildcraftreborn:trigger/light_low"/> 

<chapter name="管道连接"/>
管道插件只能安装在管道上，可以安装在管道的任意一面。
安装在两根管道之间时，它会阻断管道和管线的连接。

<usages stack="buildcraftreborn:plug_light_sensor"/>