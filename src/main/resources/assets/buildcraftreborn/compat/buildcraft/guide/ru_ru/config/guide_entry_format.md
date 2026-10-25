Каждая страница руководства — это «запись»: имя, тема страницы и её место в оглавлении. Наборы ресурсов могут добавлять, заменять и удалять записи.

<chapter name="Файлы записей"/>
Записи перечисляются в json-файле в каждом пространстве имён набора ресурсов:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Он содержит один объект, который сопоставляет имени каждой записи её определение. Например:
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
Сама страница — это отдельный файл, названный по имени записи:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
Страница en_us загружается всегда, а страница на вашем языке (если она есть) заменяет её. О том, что пишется на странице, см. <link to="buildcraftreborn:config/guide_page_format"/>.

Приоритет у наборов ресурсов, расположенных выше: запись с тем же именем заменяет нижележащую, а запись со значением null удаляет её.

<chapter name="Типы записей"/>
У каждой записи есть поле "type":
- item_stack: страница о предмете (или блоке). "stack" — идентификатор предмета.
- statement: страница о триггере или действии гейта. "statement" — его идентификатор, например "buildcraft:redstone.input.active".
- external: страница, не посвящённая ничему конкретному в игре, — как эта. "title" — ключ перевода без префикса "buildcraft.guide.page.".

Запись, чей предмет или statement не существует (например, потому что не установлен нужный мод), пропускается.

<new_page/>
<chapter name="Содержимое"/>
"book" — книга, в которой находится запись. В руководстве есть "buildcraftreborn:main" (Руководство по BuildCraft) и "buildcraftreborn:meta" (эта книга).

"tag_type" и "tag_subtype" определяют, где запись появится в оглавлении. Их названия берутся из ключей перевода "buildcraft.guide.chapter.type.[tag_type]" и "buildcraft.guide.chapter.subtype.[tag_subtype]".

Книги перечисляются так же, в файле:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Например:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" — ключ перевода, а "all_entries" определяет, будут ли в оглавлении книги также перечислены все записи, которые не входят ни в одну книгу.

Нажмите F3 и T при открытом руководстве, чтобы всё перезагрузить.

<italic>В BuildCraft 8.0 записи определялись в файлах "guide.txt" на собственном языке инструкций. BuildCraft Reborn вместо этого использует обычный json.</italic>
