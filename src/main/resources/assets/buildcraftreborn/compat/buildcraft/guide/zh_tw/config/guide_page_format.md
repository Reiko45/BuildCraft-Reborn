指南頁面全都是以 markdown 的子集撰寫。

<italic>（本頁大部分內容尚待補充，不過以下是基本標籤的列表）</italic>

# 簡單的單一標籤
每個標籤的形式都是 &lt;name arguments/&gt;，其中 name 是標籤名稱，結尾的 '/' 表示這個標籤單獨存在。有些標籤也有「鍵值對」形式的參數，例如：
<guide_md>
"level"="42"
</guide_md>

## 新頁面
&lt;new_page/&gt; 會將其後的內容移到下一頁。

## 連結
這會建立一個連到另一個指南書頁面的連結，並使用該頁面的標題與圖示。你也可以建立連到物品堆疊（ItemStack）或觸發器／動作（Statement）的連結，即使它們沒有現成的指南書頁面也沒關係。
例如：
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
會產生：
<link to="buildcraftreborn:config/guide_entry_format"/>

或者：
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
會產生：
<link to="minecraft:iron_ingot" type="item_stack"/>

## 章節
&lt;chapter/&gt; 會建立一個「章節標題」，在章節列表中以彩色標題顯示。它有一個可用參數："level"，必須是 0 或以上的整數。你也可以像一般 markdown 格式一樣，使用多個井字號（#）來建立標題。# 的數量（減一）即等於標題的層級。
例如：
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
會產生：
### 第 3 層章節
<chapter name="另一個第 3 層章節" level="2"/>

# 簡單的成對標籤
以下所有標籤都必須包住某些內容：它們必須同時具有起始標籤 &lt;italic&gt; 與結束標籤 &lt;/italic&gt;
它們都會影響起始標籤與結束標籤之間所包含的文字。

- <italic>斜體</italic>（italic）
- <bold>粗體</bold>（bold）
- <underline>底線</underline>（underline）
- <strikethrough>刪除線</strikethrough>（strikethrough）
- <red>紅色</red>（red）
- <black>黑色</black>（black）
- <dark_blue>深藍色</dark_blue>（dark_blue）
- <dark_green>深綠色</dark_green>（dark_green）
- <dark_aqua>深青色</dark_aqua>（dark_aqua）
- <dark_red>深紅色</dark_red>（dark_red）
- <dark_purple>深紫色</dark_purple>（dark_purple）
- <gold>金色</gold>（gold）
- <gray>灰色</gray>（gray）
- <dark_gray>深灰色</dark_gray>（dark_gray）
- <blue>藍色</blue>（blue）
- <green>綠色</green>（green）
- <aqua>青色</aqua>（aqua）
- <light_purple>淺紫色</light_purple>（light_purple）
- <yellow>黃色</yellow>（yellow）
- <white>白色</white>（white）

不建議使用綠色、青色、黃色或白色，因為這些顏色在頁面上通常都很難看清楚。

# 可切換的標籤
以下所有標籤都依選項而定，玩家可以自行啟用或停用。

## 背景故事
若勾選「顯示背景故事 [ ]」方框，&lt;no_lore&gt; 標籤中的所有內容都會被隱藏，否則 &lt;lore&gt; 標籤中的所有內容會被隱藏。

lore 標籤應用來呈現「遊戲內」的指南資訊，以玩家的視角敘述（彷彿玩家正積極研究一切是如何運作的）。反之，另一種則以較正式、類似維基百科的語氣，單純直接地傳達資訊。

遊戲中預設為啟用，匯出時則一律停用。

## 提示
若勾選「顯示提示 [ ]」方框，&lt;no_hint&gt; 標籤中的所有內容都會被隱藏，否則 &lt;hint&gt; 標籤中的所有內容會被隱藏。

hint 標籤應用來呈現額外的「提示」，說明所描述的東西可以用來做什麼，或是與其他方塊／物品搭配的不同用法。基本上，這是各種東西的「建議用法」。（自己摸索出這些用法也是樂趣的一部分，所以這並非必要。）因此這項功能預設為停用，但可以在遊戲中切換，未來也計劃讓它在匯出時可以切換。

## 詳細資訊
若客戶端設定選項 "guideBookEnableDetail" 設為 "true"，&lt;no_detail&gt; 標籤中的所有內容都會被隱藏，否則 &lt;detail&gt; 標籤中的所有內容會被隱藏。

detail 標籤應用來呈現計算各種數值時用到的所有數字，例如導管流量、抽取速率、脈衝頻率等。

這項功能預設為停用，但可以在遊戲中切換，未來也計劃讓它在匯出時可以切換。

# 其他
## 配方（單一）
這個標籤會顯示指定物品堆疊找到的第一個配方。例如：
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## 所有配方
這個標籤會顯示指定物品堆疊的所有配方。例如：
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## 用途
與配方類似，這個標籤會顯示指定物品堆疊的所有用途。例如：
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## 配方與用途
這個標籤實際上就是將上述兩個標籤合而為一，例如：
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## 群組
這個標籤會顯示適用於指定物品、觸發器／動作等的群組。

## 圖片
這個標籤會顯示一張圖片。它有 3 個參數：
* src：圖片的來源位置。
* width：圖片顯示的寬度。預設為來源圖片的寬度（不過這可能不是你想要的）。
* height：圖片顯示的高度。

例如：
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
