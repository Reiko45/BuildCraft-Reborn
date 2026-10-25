Rehberin her sayfası bir "girdi"dir: bir ad, sayfanın konusu ve içindekilerde nereye yerleştirileceği. Kaynak paketleri girdi ekleyebilir, girdileri değiştirebilir ya da kaldırabilir.

<chapter name="Girdi dosyaları"/>
Girdiler, bir kaynak paketinin her ad alanındaki bir json dosyasında listelenir:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Bu dosya, her girdinin adını tanımına eşleyen tek bir nesne içerir. Örneğin:
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
Sayfanın kendisi, girdinin adını taşıyan ayrı bir dosyadır:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
en_us sayfası her zaman yüklenir ve (varsa) kendi dilindeki sayfa onun yerini alır. Bir sayfaya neler yazılacağı için <link to="buildcraftreborn:config/guide_page_format"/> sayfasına bak.

Daha üstteki kaynak paketleri önceliklidir: aynı ada sahip bir girdi alttakinin yerini alır ve null olarak ayarlanan bir girdi onu kaldırır.

<chapter name="Girdi türleri"/>
Her girdinin bir "type" değeri vardır:
- item_stack: bir item (ya da blok) hakkında bir sayfa. "stack", itemın kimliğidir.
- statement: bir devre tetikleyicisi ya da eylemi hakkında bir sayfa. "statement", onun kimliğidir; örneğin "buildcraft:redstone.input.active".
- external: bunun gibi, oyundaki herhangi bir şeyle ilgili olmayan bir sayfa. "title", "buildcraft.guide.page." ifadesinden sonra gelen bir çeviri anahtarıdır.

Itemı ya da statement değeri mevcut olmayan bir girdi (örneğin bir mod yüklü olmadığı için) dışarıda bırakılır.

<new_page/>
<chapter name="İçindekiler"/>
"book", girdinin bulunduğu kitaptır. Rehber kitabında "buildcraftreborn:main" (BuildCraft Rehber Kitabı) ve "buildcraftreborn:meta" (bu kitap) bulunur.

"tag_type" ve "tag_subtype", girdinin içindekiler sayfasında nereye yerleştirileceğini belirler. Bunlar "buildcraft.guide.chapter.type.[tag_type]" ve "buildcraft.guide.chapter.subtype.[tag_subtype]" olarak çevrilir.

Kitaplar da aynı şekilde şu dosyada listelenir:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Örneğin:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" bir çeviri anahtarıdır; "all_entries" ise kitabın içindekiler listesinde hiçbir kitapta yer almayan her şeyin de listelenip listelenmeyeceğini belirtir.

Her şeyi yeniden yüklemek için rehber açıkken F3 ve T tuşlarına bas.

<italic>BuildCraft 8.0, girdileri kendi talimat dilinde yazılmış "guide.txt" dosyalarında tanımlıyordu. BuildCraft Reborn bunun yerine düz json kullanır.</italic>
