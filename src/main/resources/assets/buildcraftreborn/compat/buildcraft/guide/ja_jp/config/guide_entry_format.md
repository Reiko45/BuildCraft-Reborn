ガイドの各ページは「項目」です。項目は、名前、ページの対象、目次での位置からなります。リソースパックで項目を追加、置き換え、削除できます。

<chapter name="項目ファイル"/>
項目は、リソースパックの各名前空間にあるjsonファイルに記載します：
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
このファイルには1つのオブジェクトがあり、各項目の名前をその定義に対応させます。例：
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
ページ自体は、項目名を付けた別のファイルです：
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
en_usのページは常に読み込まれ、あなたの言語のページがあれば、そちらで置き換えられます。ページに書く内容については<link to="buildcraftreborn:config/guide_page_format"/>を参照してください。

上位のリソースパックが優先されます。同じ名前の項目は下位のものを置き換え、nullに設定した項目はそれを削除します。

<chapter name="項目の種類"/>
すべての項目には"type"があります：
- item_stack：アイテム（またはブロック）についてのページ。"stack"はそのアイテムのIDです。
- statement：ゲート入力またはゲート出力についてのページ。"statement"はそのIDで、たとえば"buildcraft:redstone.input.active"のようになります。
- external：このページのように、ゲーム内の何かを扱うのではないページ。"title"は翻訳キーのうち、"buildcraft.guide.page."に続く部分です。

アイテムやステートメントが存在しない項目（たとえばModが導入されていない場合）は除外されます。

<new_page/>
<chapter name="目次"/>
"book"は、項目が属する本です。ガイドブックには"buildcraftreborn:main"（BuildCraft ガイドブック）と"buildcraftreborn:meta"（この本）があります。

"tag_type"と"tag_subtype"は、目次ページで項目が置かれる位置です。これらは"buildcraft.guide.chapter.type.[tag_type]"と"buildcraft.guide.chapter.subtype.[tag_subtype]"として翻訳されます。

本も同じように、次のファイルに記載します：
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
例：
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title"は翻訳キーで、"all_entries"は、どの本にも属さないものもすべてその本の目次に載せるかどうかです。

ガイドを開いた状態でF3とTを押すと、すべてを再読み込みできます。

<italic>BuildCraft 8.0では、項目を独自の命令言語で"guide.txt"ファイルに定義していました。BuildCraft Rebornでは代わりに普通のjsonを使います。</italic>
