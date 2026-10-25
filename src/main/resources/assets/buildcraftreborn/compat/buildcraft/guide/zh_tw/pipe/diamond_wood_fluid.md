<lore>
想把某種流體抽出來送到別處嗎？只要供給能量，然後看著它流動就好。
</lore>
<no_lore>
木製鑽石流體導管在獲得能量時，會從流體容器中抽取所選的流體。
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_fluid"/>

<chapter name="導管機制"/>
獲得能量時，它會將過濾條件所選的流體抽出，送往其他相連的流體容器或導管。
流體會從導管實心的面抽出，並流向透明的面。
若它有多個透明的面，流體就會平均分流到各個方向。

有些機器不需要能量就能直接將流體輸出到這條導管中（這不會遵守過濾條件）。
使用管塞或將導管塗上不同的顏色，可以阻止導管互相連接。

<chapter name="過濾"/>
開啟木製鑽石流體導管的介面即可設定過濾條件。
在欄位中放入裝有所需流體的鐵桶（或其他可裝流體的容器），就能過濾它要抽取的流體。
你可以使用白名單與黑名單項目來改變過濾條件的作用：
白名單：只抽取所選的流體。
黑名單：抽取所選流體以外的任何流體。

<chapter name="供能"/>
它可以從任何 MJ 能量來源獲得能量，例如：引擎、能量轉接器或導管脈衝器。
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/>
每收到一次能量脈衝，它就會從相鄰的流體容器抽取流體。
脈衝的能量越多，每次脈衝抽取的流體就越多。

<no_detail>
木製鑽石流體導管以每秒 1.6 桶的速度，將流體輸送到相鄰的導管與流體容器。
流體導管內部有流體儲槽，每一面可容納 1 桶，中央也可容納 1 桶。
</no_detail>

<usages stack="buildcraftreborn:pipe_diamond_wood_fluid"/>