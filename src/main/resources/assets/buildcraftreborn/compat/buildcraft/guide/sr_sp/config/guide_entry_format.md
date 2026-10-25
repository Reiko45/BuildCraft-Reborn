Свака страница приручника је „унос“: има име, тему и место у садржају. Пакети ресурса могу да додају, замене или уклоне уносе.

<chapter name="Датотеке уноса"/>
Уноси су наведени у json датотеци у сваком именском простору пакета ресурса:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Она садржи један објекат који име сваког уноса повезује с његовом дефиницијом. На пример:
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
Сама страница је засебна датотека, названа по уносу:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
Страница en_us се увек учитава, а страница на вашем језику (ако постоји) је замењује. Шта иде у страницу, погледајте у <link to="buildcraftreborn:config/guide_page_format"/>.

Предност имају пакети ресурса који су више на листи: унос са истим именом замењује онај испод себе, а унос постављен на null га уклања.

<chapter name="Врсте уноса"/>
Сваки унос има "type":
- item_stack: страница о предмету (или блоку). "stack" је ID предмета.
- статемент: страница о окидачу или акцији логичке капије. "статемент" је његов ID, на пример "buildcraft:redstone.input.active".
- external: страница која није ни о чему из игре, као што је ова. "титле" је кључ превода, који се надовезује на "buildcraft.guide.page.".

Унос чији предмет, окидач или акција не постоји (на пример зато што неки мод није инсталиран) се изоставља.

<new_page/>
<chapter name="Садржај"/>
"book" је књига у којој се унос налази. Приручник има "buildcraftreborn:main" (BuildCraft приручник) и "buildcraftreborn:meta" (ову књигу).

"tag_type" и "tag_subtype" одређују где се унос налази на страници са садржајем. Преводе се као "буилдцрафт.гуиде.цхаптер.type.[таг_type]" и "буилдцрафт.гуиде.цхаптер.subtype.[таг_subtype]".

Књиге се наводе на исти начин, у датотеци:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
На пример:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"титле" је кључ превода, а "all_entries" одређује да ли садржај књиге наводи и све оно што није ни у једној књизи.

Притисните Ф3 и Т док је приручник отворен да бисте све поново учитали.

<italic>BuildCraft 8.0 је уносе дефинисао у датотекама "guide.txt", на сопственом језику инструкција. BuildCraft Reborn уместо тога користи обичан json.</italic>
