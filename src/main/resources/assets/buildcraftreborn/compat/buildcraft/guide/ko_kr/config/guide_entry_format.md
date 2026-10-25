가이드의 각 페이지는 "항목"입니다. 항목은 이름, 페이지가 다루는 대상, 목차에서의 위치로 이루어집니다. 리소스 팩으로 항목을 추가, 교체, 제거할 수 있습니다.

<chapter name="항목 파일"/>
항목은 리소스 팩의 각 네임스페이스에 있는 json 파일에 나열됩니다:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
이 파일에는 각 항목의 이름을 정의에 대응시키는 객체 하나가 들어 있습니다. 예시:
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
페이지 자체는 항목 이름을 딴 별도의 파일입니다:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
en_us 페이지는 항상 불러오며, 사용자의 언어로 된 페이지가 있으면 그 페이지가 대신 쓰입니다. 페이지에 들어갈 내용은 <link to="buildcraftreborn:config/guide_page_format"/>을 참고하세요.

위에 있는 리소스 팩이 우선합니다. 같은 이름의 항목은 아래 항목을 대체하고, null로 설정된 항목은 해당 항목을 제거합니다.

<chapter name="항목 유형"/>
모든 항목에는 "type"이 있습니다:
- item_stack: 아이템(또는 블록)에 관한 페이지입니다. "stack"은 아이템의 ID입니다.
- statement: 게이트 트리거나 액션에 관한 페이지입니다. "statement"는 그 ID이며, 예를 들면 "buildcraft:redstone.input.active"입니다.
- external: 이 페이지처럼 게임 속 어떤 것에도 관한 것이 아닌 페이지입니다. "title"은 "buildcraft.guide.page." 뒤에 붙는 번역 키입니다.

아이템이나 statement가 존재하지 않는 항목(예: 모드가 설치되지 않은 경우)은 제외됩니다.

<new_page/>
<chapter name="목차"/>
"book"은 항목이 들어갈 책입니다. 가이드북에는 "buildcraftreborn:main"(가이드북)과 "buildcraftreborn:meta"(이 책)가 있습니다.

"tag_type"과 "tag_subtype"은 목차 페이지에서 항목이 들어갈 위치입니다. 각각 "buildcraft.guide.chapter.type.[tag_type]"과 "buildcraft.guide.chapter.subtype.[tag_subtype]"으로 번역됩니다.

책도 같은 방식으로 다음 파일에 나열됩니다:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
예시:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title"은 번역 키이고, "all_entries"는 어떤 책에도 속하지 않은 모든 항목을 이 책의 목차에도 나열할지 여부입니다.

가이드가 열려 있을 때 F3과 T를 누르면 모든 것을 다시 불러옵니다.

<italic>BuildCraft 8.0은 자체 명령 언어로 작성된 "guide.txt" 파일에 항목을 정의했습니다. BuildCraft Reborn은 대신 일반 json을 사용합니다.</italic>
