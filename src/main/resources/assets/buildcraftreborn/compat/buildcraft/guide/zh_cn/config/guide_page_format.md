教程页面全部使用 markdown 的一个子集编写。

<italic>（本页大部分内容尚待补充，不过这里先列出一些基本标签）</italic>

# 简单单标签
每个标签的形式都是 &lt;name arguments/&gt;，其中 name 是标签名，结尾的“/”表示这个标签是单独使用的。有些标签还带有“键值对”形式的参数，例如：
<guide_md>
"level"="42"
</guide_md>

## 新页面
&lt;new_page/&gt; 会把它之后的内容移到下一页。

## 链接
这会创建一个指向教程书其他页面的链接，并使用被链接页面的标题和图标。你也可以链接到 ItemStack 或 Statement，即使它们在教程书中没有对应的页面。
例如：
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
效果如下：
<link to="buildcraftreborn:config/guide_entry_format"/>

或者：
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
效果如下：
<link to="minecraft:iron_ingot" type="item_stack"/>

## 章节
&lt;chapter/&gt; 会创建一个“章节标题”，在章节列表中显示为彩色标题。它只有一个可用参数："level"，必须是大于或等于 0 的整数。你也可以像普通 markdown 格式那样，使用多个井号（#）来创建标题。# 的数量减一即为标题的级别。
例如：
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
效果如下：
### 3 级章节
<chapter name="另一个 3 级章节" level="2"/>

# 简单双标签
这些标签都必须包裹一些内容，并且必须同时有起始标签 &lt;italic&gt; 和结束标签 &lt;/italic&gt;。
它们都会作用于起始标签和结束标签之间的文本。

- <italic>斜体</italic> (italic)
- <bold>粗体</bold> (bold)
- <underline>下划线</underline> (underline)
- <strikethrough>删除线</strikethrough> (strikethrough)
- <red>红色</red> (red)
- <black>黑色</black> (black)
- <dark_blue>深蓝色</dark_blue> (dark_blue)
- <dark_green>深绿色</dark_green> (dark_green)
- <dark_aqua>湖蓝色</dark_aqua> (dark_aqua)
- <dark_red>深红色</dark_red> (dark_red)
- <dark_purple>紫色</dark_purple> (dark_purple)
- <gold>金色</gold> (gold)
- <gray>灰色</gray> (gray)
- <dark_gray>深灰色</dark_gray> (dark_gray)
- <blue>蓝色</blue> (blue)
- <green>绿色</green> (green)
- <aqua>天蓝色</aqua> (aqua)
- <light_purple>粉红色</light_purple> (light_purple)
- <yellow>黄色</yellow> (yellow)
- <white>白色</white> (white)

不建议使用 green、aqua、yellow 或 white，因为这些颜色在页面上通常很难看清。

# 可切换标签
以下标签都基于选项，玩家可以启用或禁用它们。

## 背景描述
如果勾选了“显示背景故事 [ ]”复选框，&lt;no_lore&gt; 标签中的所有内容都会被隐藏，否则 &lt;lore&gt; 标签中的所有内容会被隐藏。

lore 标签应用于显示从玩家视角叙述的“游戏内”教程信息（就好像玩家正在亲自研究一切是如何运作的）。与之相反的标签则使用更正式、类似维基百科的语气，单纯直接地传达信息。

该选项在游戏中默认启用，在导出时始终禁用。

## 提示
如果勾选了“显示提示 [ ]”复选框，&lt;no_hint&gt; 标签中的所有内容都会被隐藏，否则 &lt;hint&gt; 标签中的所有内容会被隐藏。

hint 标签应用于显示额外的“提示”，例如所介绍的东西可以用来做什么，或者与其他方块/物品组合使用的不同方式。本质上，它用于介绍各种东西的“推荐用法”。（由于自己摸索出这些也是乐趣的一部分，所以它并非必需。）因此该选项默认禁用，但可以在游戏中切换，并计划在导出时也支持切换。

## 详细信息
如果客户端配置选项 "guideBookEnableDetail" 设为 "true"，&lt;no_detail&gt; 标签中的所有内容都会被隐藏，否则 &lt;detail&gt; 标签中的所有内容会被隐藏。

detail 标签应用于显示计算各种数值时用到的所有数字，例如管道流速、提取速率、脉冲频率等。

该选项默认禁用，但可以在游戏中切换，并计划在导出时也支持切换。

# 其他
## 配方（单个）
该标签会显示为指定物品堆叠找到的第一个配方。例如：
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## 配方（全部）
该标签会显示指定物品堆叠的所有配方。例如：
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## 用途
与 recipes 类似，它会显示指定物品堆叠的所有用途。例如：
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## 配方与用途
它实际上是把上面两个标签合二为一，例如：
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## 分组
显示与指定物品、statement 等相关的分组。

## 图片
显示一张图片。共有 3 个参数：
* src：图片的来源位置。
* width：图片显示的宽度。默认为源图片的宽度（不过这可能不是你想要的）
* height：图片显示的高度。

例如：
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
