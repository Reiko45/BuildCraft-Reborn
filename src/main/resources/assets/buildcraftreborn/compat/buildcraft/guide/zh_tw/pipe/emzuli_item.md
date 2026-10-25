<lore>
用閘門來抽取特定物品，是過濾技術的最新一場技術革命。
</lore>
<no_lore>
綠寶青金石導管是一種由閘門控制的導管，在獲得能量時，會依過濾條件從容器中抽取所選的物品。
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="導管機制"/>
獲得能量時，它會抽取過濾條件中所選的物品，並將它們輸送到其他相連的容器或導管。
物品會從導管實心的面抽出，並輸送往透明的面。
若它有多個透明的面，物品會往隨機方向移動。
若物品無處可去，抽出的物品就會從導管掉落到地上。

有些機器可以自動將物品直接輸出到綠寶青金石導管中，導管不需要能量（這不會遵守過濾條件）。
使用管塞或將導管塗上不同的顏色，可以阻止導管互相連接。

<chapter name="過濾"/>
開啟綠寶青金石導管的介面，會看到四種顏色，分別對應四組獨立的過濾選項。
在欄位中放入物品，即可設定每組過濾條件要抽取的物品。
欄位旁油漆刷的顏色，決定了物品被抽出時會塗上的顏色，點擊油漆刷即可更改。
若要選擇抽取四組過濾條件中的哪一組，必須在導管上放置閘門，並使用帶有顏色的「抽取預設值」動作。例如：紅石訊號 → 紅色的抽取預設值。

過濾欄位中可以放入清單，以獲得更多過濾選項。
<link to="buildcraftreborn:item/list"/>

<chapter name="供能"/>
它可以從任何 MJ 能量來源獲得能量，例如：引擎、能量轉接器或導管脈衝器。
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
每收到一次能量脈衝，它就會從相鄰的容器抽取物品。
脈衝的能量越多，每次脈衝抽取的物品就越多。

<usages stack="buildcraftreborn:pipe_emzuli_item"/>