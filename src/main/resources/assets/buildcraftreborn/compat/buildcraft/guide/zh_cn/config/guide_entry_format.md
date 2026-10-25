教程的每一页都是一个“条目”：包含名称、页面所介绍的对象，以及它在内容页中的位置。资源包可以添加、替换或移除条目。

<chapter name="条目文件"/>
条目列在资源包每个命名空间中的一个 json 文件里：
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
该文件包含一个对象，将每个条目的名称映射到其定义。例如：
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
页面本身是一个单独的文件，以条目名称命名：
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
en_us 页面总会被加载，如果有你所用语言的页面，就会用它替换。页面内容的写法请参阅<link to="buildcraftreborn:config/guide_page_format"/>。

优先级更高的资源包会胜出：同名条目会替换下层的条目，设为 null 的条目则会将其移除。

<chapter name="条目类型"/>
每个条目都有一个 "type"：
- item_stack：关于某个物品（或方块）的页面。"stack" 为该物品的 ID。
- statement：关于门的触发条件或动作的页面。"statement" 为其 ID，例如 "buildcraft:redstone.input.active"。
- external：不介绍游戏中任何事物的页面，比如本页。"title" 是翻译键中 "buildcraft.guide.page." 之后的部分。

如果条目对应的物品或 statement 不存在（例如因为某个 MOD 未安装），该条目会被略去。

<new_page/>
<chapter name="内容"/>
"book" 是条目所在的书。教程书有 "buildcraftreborn:main"（BuildCraft 教程书）和 "buildcraftreborn:meta"（本书）。

"tag_type" 和 "tag_subtype" 决定条目在内容页中的位置。它们分别按 "buildcraft.guide.chapter.type.[tag_type]" 和 "buildcraft.guide.chapter.subtype.[tag_subtype]" 进行翻译。

书也以同样的方式列在以下文件中：
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
"title" 是翻译键，"all_entries" 表示该书的内容页是否也列出不属于任何书的所有条目。

在教程打开时按下 F3 和 T 即可重新加载所有内容。

<italic>BuildCraft 8.0 用自己的指令语言在 "guide.txt" 文件中定义条目，而 BuildCraft Reborn 改用普通的 json。</italic>
