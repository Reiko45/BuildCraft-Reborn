Every page of the guide is an "entry": a name, what the page is about, and where it goes in the contents. Resource packs can add entries, replace them, or remove them.

<chapter name="Entry files"/>
Entries are listed in a json file in each namespace of a resource pack:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
It holds one object, mapping each entry's name to its definition. For example:
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
The page itself is a separate file, named after the entry:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
The en_us page is always loaded, and the page in your own language (if there is one) replaces it. See <link to="buildcraftreborn:config/guide_page_format"/> for what goes in a page.

Higher resource packs win: an entry with the same name replaces the one below it, and an entry set to null removes it.

<chapter name="Entry types"/>
Every entry has a "type":
- item_stack: a page about an item (or block). "stack" is the item's id.
- statement: a page about a gate trigger or action. "statement" is its id, such as "buildcraft:redstone.input.active".
- external: a page that isn't about anything in the game, like this one. "title" is a translation key, after "buildcraft.guide.page.".

An entry whose item or statement doesn't exist (for example because a mod isn't installed) is left out.

<new_page/>
<chapter name="Contents"/>
"book" is the book that the entry is in. The guide book has "buildcraftreborn:main" (the BuildCraft Guide Book) and "buildcraftreborn:meta" (this book).

"tag_type" and "tag_subtype" are where the entry goes in the contents page. They are translated as "buildcraft.guide.chapter.type.[tag_type]" and "buildcraft.guide.chapter.subtype.[tag_subtype]".

Books are listed in the same way, in:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
For example:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" is a translation key, and "all_entries" is whether the book's contents also list everything that isn't in any book.

Press F3 and T while the guide is open to load everything again.

<italic>BuildCraft 8.0 defined entries in "guide.txt" files, in its own instruction language. BuildCraft Reborn uses plain json instead.</italic>
