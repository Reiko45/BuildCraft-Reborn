ガイドのページはすべて、Markdownのサブセットで書かれています。

<italic>（このページの大部分はまだ書きかけですが、基本的なタグの一覧を載せておきます）</italic>

# 単独で使うシンプルなタグ
すべてのタグは &lt;name arguments/&gt; という形をしています。nameはタグ名で、末尾の'/'はこのタグが単独で使われることを示します。一部のタグには「キーと値のペア」の形で引数もあります。例：
<guide_md>
"level"="42"
</guide_md>

## 改ページ
&lt;new_page/&gt; は、それより後の内容を次のページに送ります。

## リンク
別のガイドブックのページへのリンクを作ります。リンクには、リンク先ページのタイトルとアイコンが使われます。また、ガイドブックにページがないItemStackやStatementへのリンクを作ることもできます。
例：
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
結果は次のようになります：
<link to="buildcraftreborn:config/guide_entry_format"/>

または：
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
結果は次のようになります：
<link to="minecraft:iron_ingot" type="item_stack"/>

## 章
&lt;chapter/&gt; は「章見出し」を作り、章一覧に色付きの見出しとして表示されます。指定できる引数は"level"の1つだけで、0以上の整数でなければなりません。また、通常のMarkdownと同じように、複数のシャープ記号（#）を使って見出しを作ることもできます。#の数から1を引いた値が、その見出しのレベルになります。
例：
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
結果は次のようになります：
### レベル3の章
<chapter name="もう1つのレベル3の章" level="2"/>

# 対で使うシンプルなタグ
これらのタグはすべて何らかの内容を囲む必要があり、開始タグ &lt;italic&gt; と終了タグ &lt;/italic&gt; の両方が必要です。
いずれも、開始タグと終了タグの間にある文字に効果を与えます。

- <italic>斜体</italic> (italic)
- <bold>太字</bold> (bold)
- <underline>下線</underline> (underline)
- <strikethrough>取り消し線</strikethrough> (strikethrough)
- <red>赤</red> (red)
- <black>黒</black> (black)
- <dark_blue>濃い青</dark_blue> (dark_blue)
- <dark_green>濃い緑</dark_green> (dark_green)
- <dark_aqua>濃い水色</dark_aqua> (dark_aqua)
- <dark_red>濃い赤</dark_red> (dark_red)
- <dark_purple>濃い紫</dark_purple> (dark_purple)
- <gold>金色</gold> (gold)
- <gray>灰色</gray> (gray)
- <dark_gray>濃い灰色</dark_gray> (dark_gray)
- <blue>青</blue> (blue)
- <green>緑</green> (green)
- <aqua>水色</aqua> (aqua)
- <light_purple>薄紫</light_purple> (light_purple)
- <yellow>黄色</yellow> (yellow)
- <white>白</white> (white)

green、aqua、yellow、whiteは、どれもページ上でとても見えにくい色なので、使わないほうがよいでしょう。

# 切り替え式のタグ
以下のタグはすべて設定によるもので、プレイヤーが有効・無効を切り替えられます。

## ロア
「背景を表示 [ ]」のチェックボックスにチェックが入っていると &lt;no_lore&gt; タグの中身がすべて非表示になり、入っていなければ &lt;lore&gt; タグの中身がすべて非表示になります。

loreタグは、プレイヤーの視点で語られる「ゲーム内の」ガイド情報を表示するために使います（まるでプレイヤー自身が、あらゆるものの仕組みを研究しているかのように）。その逆のタグは、情報をそのまま簡潔に伝える、よりフォーマルでWikipediaのような語り口のためのものです。

ゲーム内ではデフォルトで有効になっており、エクスポート時は常に無効です。

## ヒント
「ヒントを表示 [ ]」のチェックボックスにチェックが入っていると &lt;no_hint&gt; タグの中身がすべて非表示になり、入っていなければ &lt;hint&gt; タグの中身がすべて非表示になります。

hintタグは、説明している物の使い道や、他のブロック・アイテムと組み合わせたさまざまな使い方など、追加の「ヒント」を表示するために使います。要するに、いろいろな物の「おすすめの使い方」のためのものです。（これを自分で見つけ出すのも楽しみのうちなので、必須ではありません。）そのため、デフォルトでは無効ですが、ゲーム内で切り替えられ、エクスポート時にも切り替えられるようにする予定です。

## 詳細
クライアント設定の"guideBookEnableDetail"が"true"になっていると &lt;no_detail&gt; タグの中身がすべて非表示になり、そうでなければ &lt;detail&gt; タグの中身がすべて非表示になります。

detailタグは、パイプの流量、搬出速度、パルスの頻度など、さまざまな値の計算に使われる数値をすべて表示するために使います。

デフォルトでは無効ですが、ゲーム内で切り替えられ、エクスポート時にも切り替えられるようにする予定です。

# その他
## レシピ（単体）
このタグは、指定したアイテムスタックについて最初に見つかったレシピを表示します。例：
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## レシピ（すべて）
このタグは、指定したアイテムスタックのレシピをすべて表示します。例：
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## 用途
recipesと同様に、指定したアイテムスタックの用途をすべて表示します。例：
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## レシピと用途
上の2つのタグを実質的に1つにまとめたものです。例：
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## グループ
指定したアイテムやステートメントなどに該当するグループを表示します。

## 画像
画像を表示します。引数は3つあります：
* src：画像の場所。
* width：画像を表示する幅。デフォルトは元画像の幅です（ただし、望みどおりにならないこともあります）
* height：画像を表示する高さ。

例：
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
