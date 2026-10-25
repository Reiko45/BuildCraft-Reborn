가이드 페이지는 모두 마크다운의 일부 문법으로 작성됩니다.

<italic>(이 페이지의 대부분은 아직 채워야 하지만, 우선 기본 태그 목록을 소개합니다)</italic>

# 단순 단일 태그
모든 태그는 &lt;name arguments/&gt; 형식입니다. 여기서 name은 태그 이름이고, 끝의 '/'는 이 태그가 단독으로 쓰인다는 뜻입니다. 일부 태그에는 "키-값 쌍" 형식의 인수도 있습니다. 예시:
<guide_md>
"level"="42"
</guide_md>

## 새 페이지
&lt;new_page/&gt;는 그 뒤의 내용을 다음 페이지로 넘깁니다.

## 링크
다른 가이드북 페이지로 가는 링크를 만들며, 링크된 페이지의 제목과 아이콘을 사용합니다. 또는 가이드북 페이지가 없는 ItemStack이나 Statement로도 링크를 만들 수 있습니다.
예시:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
결과:
<link to="buildcraftreborn:config/guide_entry_format"/>

또는:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
결과:
<link to="minecraft:iron_ingot" type="item_stack"/>

## 장
&lt;chapter/&gt;는 장 목록에 색이 있는 제목으로 표시되는 "장 제목"을 만듭니다. 사용할 수 있는 인수는 "level" 하나뿐이며, 0 이상의 정수여야 합니다. 또는 일반 마크다운처럼 샵 기호(#)를 여러 개 써서 제목을 만들 수도 있습니다. #의 개수에서 1을 뺀 값이 제목의 레벨입니다.
예시:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
결과:
### 레벨 3 장
<chapter name="또 다른 레벨 3 장" level="2"/>

# 단순 이중 태그
이 태그들은 모두 내용을 감싸야 하며, 시작 태그 &lt;italic&gt;와 종료 태그 &lt;/italic&gt;가 모두 있어야 합니다.
모두 시작 태그와 종료 태그 사이의 텍스트에 적용됩니다.

- <italic>기울임</italic>
- <bold>굵게</bold>
- <underline>밑줄</underline>
- <strikethrough>취소선</strikethrough>
- <red>빨간색</red>
- <black>검은색</black>
- <dark_blue>짙은 파란색</dark_blue>
- <dark_green>짙은 초록색</dark_green>
- <dark_aqua>짙은 청록색</dark_aqua>
- <dark_red>짙은 빨간색</dark_red>
- <dark_purple>짙은 보라색</dark_purple>
- <gold>금색</gold>
- <gray>회색</gray>
- <dark_gray>짙은 회색</dark_gray>
- <blue>파란색</blue>
- <green>초록색</green> (초록색)
- <aqua>청록색</aqua> (청록색)
- <light_purple>연보라색</light_purple>
- <yellow>노란색</yellow> (노란색)
- <white>흰색</white> (흰색)

초록색, 청록색, 노란색, 흰색은 페이지에서 알아보기가 매우 어렵기 때문에 사용하지 않는 것이 좋습니다.

# 전환 태그
다음 태그들은 모두 옵션에 따라 동작하며, 플레이어가 켜거나 끌 수 있습니다.

## 이야기
"배경 이야기 표시 [ ]" 상자에 체크하면 &lt;no_lore&gt; 태그 안의 모든 내용이 숨겨지고, 그렇지 않으면 &lt;lore&gt; 태그 안의 모든 내용이 숨겨집니다.

lore 태그는 플레이어의 시점에서 서술하는 "게임 속" 가이드 정보를 보여 줄 때 사용합니다. (마치 플레이어가 모든 것이 어떻게 작동하는지 직접 연구하고 있는 것처럼요.) 반대쪽 태그는 정보를 있는 그대로 간결하게 전달하는, 더 격식 있는 위키백과 같은 어조에 사용합니다.

게임 내에서는 기본적으로 켜져 있으며, 내보내기에서는 항상 꺼져 있습니다.

## 힌트
"힌트 표시 [ ]" 상자에 체크하면 &lt;no_hint&gt; 태그 안의 모든 내용이 숨겨지고, 그렇지 않으면 &lt;hint&gt; 태그 안의 모든 내용이 숨겨집니다.

hint 태그는 설명하는 대상의 활용법에 대한 추가 "힌트"나, 다른 블록/아이템과 조합해 사용하는 다양한 방법을 보여 줄 때 사용합니다. 본질적으로 여러 대상의 "추천 활용법"을 위한 것입니다. (이것을 알아내는 것도 재미의 일부이므로 필수는 아닙니다.) 그래서 기본적으로 꺼져 있지만, 게임 내에서 켜고 끌 수 있으며 내보낼 때도 켜고 끌 수 있도록 할 계획입니다.

## 세부 정보
"guideBookEnableDetail" 클라이언트 설정 옵션이 "true"로 설정되어 있으면 &lt;no_detail&gt; 태그 안의 모든 내용이 숨겨지고, 그렇지 않으면 &lt;detail&gt; 태그 안의 모든 내용이 숨겨집니다.

detail 태그는 파이프 유량, 추출 속도, 펄스 속도 등 여러 가지를 계산하는 데 쓰이는 모든 수치를 보여 줄 때 사용합니다.

기본적으로 꺼져 있지만, 게임 내에서 켜고 끌 수 있으며 내보낼 때도 켜고 끌 수 있도록 할 계획입니다.

# 기타
## 제작법 (단수)
이 태그는 주어진 아이템 스택에 대해 처음 발견된 제작법을 보여 줍니다. 예시:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## 모든 제작법
이 태그는 주어진 아이템 스택의 모든 제작법을 보여 줍니다. 예시:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## 용도
제작법과 비슷하게, 주어진 아이템 스택의 모든 용도를 보여 줍니다. 예시:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## 제작법과 용도
위의 두 태그를 사실상 하나로 합친 것입니다. 예시:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## 그룹
주어진 아이템, statement 등에 맞는 그룹을 보여 줍니다.

## 이미지
이미지를 보여 줍니다. 인수는 3개입니다:
* src: 이미지의 원본 위치입니다.
* width: 이미지를 표시할 너비입니다. 기본값은 원본 이미지의 너비입니다(하지만 원하는 결과가 아닐 수도 있습니다).
* height: 이미지를 표시할 높이입니다.

예시:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
