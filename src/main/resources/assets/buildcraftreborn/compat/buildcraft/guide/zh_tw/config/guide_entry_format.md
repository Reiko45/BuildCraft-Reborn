指南的每一頁都是一個「條目」：包含名稱、頁面的主題，以及它在目錄中的位置。資源包可以新增、取代或移除條目。

<chapter name="條目檔案"/>
條目列在資源包每個命名空間中的一個 json 檔案裡：
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
檔案內含一個物件，將每個條目的名稱對應到它的定義。例如：
<json_insn>
{
  "item/wrench": {
    "type": "item_stack",
    "book": "buildcraftreborn:main",
    "tag_type": "item",
    "tag_subtype": "tool",
    "stack": "buildcraftreborn:wrench"
  }
}
</json_insn>
<new_page/>
頁面本身是另一個獨立檔案，以條目名稱命名：
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
en_us 頁面一定會載入，而你所用語言的頁面（如果有的話）會取代它。頁面的內容寫法請參閱 <link to="buildcraftreborn:config/guide_page_format"/>。

優先順序較高的資源包會勝出：同名的條目會取代較低層的條目，而設為 null 的條目則會將其移除。

<chapter name="條目類型"/>
每個條目都有一個 "type"：
- item_stack：關於某個物品（或方塊）的頁面。"stack" 是該物品的 ID。
- statement：關於閘門觸發器或動作的頁面。"statement" 是它的 ID，例如 "buildcraft:redstone.input.active"。
- external：不針對遊戲中任何事物的頁面，例如本頁。"title" 是翻譯鍵中 "buildcraft.guide.page." 之後的部分。

若條目對應的物品或觸發器／動作不存在（例如因為未安裝某個模組），該條目就會被略過。

<new_page/>
<chapter name="目錄"/>
"book" 是條目所屬的書。指南書有 "buildcraftreborn:main"（BuildCraft 指南書）與 "buildcraftreborn:meta"（本書）。

"tag_type" 與 "tag_subtype" 決定條目在目錄頁中的位置。它們會分別以 "buildcraft.guide.chapter.type.[tag_type]" 與 "buildcraft.guide.chapter.subtype.[tag_subtype]" 進行翻譯。

書籍也以相同的方式列在以下檔案中：
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
例如：
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" 是翻譯鍵，"all_entries" 則決定這本書的目錄是否也列出不屬於任何一本書的所有條目。

開啟指南時同時按下 F3 和 T，即可重新載入所有內容。

<italic>BuildCraft 8.0 以自己的指令語言，在 "guide.txt" 檔案中定義條目；BuildCraft Reborn 則改用純 json。</italic>
