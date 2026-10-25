Rehber sayfalarının tümü markdown'ın bir alt kümesiyle yazılır.

<italic>(Bu sayfanın büyük kısmının hâlâ doldurulması gerekiyor, ancak şimdilik temel etiketlerin bir listesini burada bulabilirsin)</italic>

# Basit tekli etiketler
Her etiket &lt;name arguments/&gt; biçimindedir; burada name etiketin adıdır, sondaki '/' ise bu etiketin tek başına durduğunu belirtir. Bazı etiketlerin "anahtar-değer çiftleri" biçiminde argümanları da vardır, örneğin:
<guide_md>
"level"="42"
</guide_md>

## Yeni Sayfa
&lt;new_page/&gt; kendisinden sonraki içeriği bir sonraki sayfaya taşır.

## Bağlantılar
Bunlar, bağlantı verilen sayfanın başlığını ve simgesini kullanarak başka bir rehber kitabı sayfasına bağlantı oluşturur. Alternatif olarak, mevcut bir rehber kitabı sayfası olmasa bile bir ItemStack'e ya da Statement'a bağlantı verebilirsin.
Örneğin:
<guide_md>
&lt;link to="buildcraftreborn:config/guide_entry_format"/&gt;
</guide_md>
şu sonucu verir:
<link to="buildcraftreborn:config/guide_entry_format"/>

ya da:
<guide_md>
&lt;link to="minecraft:iron_ingot" type="item_stack"/&gt;
</guide_md>
şu sonucu verir:
<link to="minecraft:iron_ingot" type="item_stack"/>

## Bölümler
&lt;chapter/&gt;, bölüm listesinde renkli bir başlık olarak gösterilen bir "bölüm başlığı" oluşturur. Tek bir olası argümanı vardır: "level"; bu, 0 ya da daha büyük bir tam sayı olmalıdır. Alternatif olarak, normal markdown biçiminde olduğu gibi birden çok diyez (#) işareti kullanarak da başlık oluşturabilirsin. # işaretlerinin sayısının bir eksiği, başlığın seviyesine eşittir.
Örneğin:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
şu sonucu verir:
### 3. seviye bölüm
<chapter name="Başka bir 3. seviye bölüm" level="2"/>

# Basit çift etiketler
Bunların hepsi bir içeriği sarmalamalıdır ve hem bir başlangıç etiketi &lt;italic&gt; hem de bir bitiş etiketi &lt;/italic&gt; içermelidir.
Hepsi, başlangıç ve bitiş etiketleri arasındaki metni etkiler.

- <italic>italik</italic>
- <bold>kalın</bold>
- <underline>altı çizili</underline>
- <strikethrough>üstü çizili</strikethrough>
- <red>kırmızı</red>
- <black>siyah</black>
- <dark_blue>koyu mavi</dark_blue>
- <dark_green>koyu yeşil</dark_green>
- <dark_aqua>koyu camgöbeği</dark_aqua>
- <dark_red>koyu kırmızı</dark_red>
- <dark_purple>koyu mor</dark_purple>
- <gold>altın</gold>
- <gray>gri</gray>
- <dark_gray>koyu gri</dark_gray>
- <blue>mavi</blue>
- <green>yeşil</green> (yeşil)
- <aqua>camgöbeği</aqua> (camgöbeği)
- <light_purple>açık mor</light_purple>
- <yellow>sarı</yellow> (sarı)
- <white>beyaz</white> (beyaz)

Yeşil, camgöbeği, sarı ya da beyaz kullanmak iyi bir fikir değildir, çünkü bu renklerin sayfada görülmesi genellikle çok zordur.

# Açılıp kapatılabilen etiketler
Aşağıdaki etiketlerin tümü seçeneklere bağlıdır ve oyuncu tarafından etkinleştirilip devre dışı bırakılabilir.

## Hikâye
"Hikâyeyi göster [ ]" kutusu işaretliyse &lt;no_lore&gt; etiketindeki her şey gizlenir, değilse &lt;lore&gt; etiketindeki her şey gizlenir.

lore etiketi, oyuncunun bakış açısından anlatılan "oyun içi" rehber bilgilerini göstermek için kullanılmalıdır. (Sanki oyuncu her şeyin nasıl çalıştığını kendisi araştırıyormuş gibi.) Tersi ise bilgiyi yalın ve açık bir şekilde aktaran, daha resmî, Vikipedi benzeri bir üslup içindir.

Bu, oyun içinde varsayılan olarak etkindir ve dışa aktarmalarda her zaman devre dışıdır.

## İpuçları
"İpuçlarını göster [ ]" kutusu işaretliyse &lt;no_hint&gt; etiketindeki her şey gizlenir, değilse &lt;hint&gt; etiketindeki her şey gizlenir.

hint etiketi, anlatılan şeyin kullanılabileceği ek "ipuçlarını" ya da onu diğer bloklar/itemlarla birlikte kullanmanın farklı yollarını göstermek için kullanılmalıdır. Özünde bu, farklı şeylerin "önerilen kullanımları" içindir. (Bunları kendin keşfetmek eğlencenin bir parçası olduğundan zorunlu değildir.) Bu nedenle varsayılan olarak devre dışıdır, ancak oyun içinde açılıp kapatılabilir ve dışa aktarırken de açılıp kapatılabilmesi planlanmaktadır.

## Ayrıntı
"guideBookEnableDetail" istemci yapılandırma seçeneği "true" olarak ayarlanmışsa &lt;no_detail&gt; etiketindeki her şey gizlenir, değilse &lt;detail&gt; etiketindeki her şey gizlenir.

detail etiketi, boru akış hızı, ekstraksiyon hızı, atım hızı gibi çeşitli şeylerin hesaplanmasında kullanılan tüm sayıları göstermek için kullanılmalıdır.

Bu varsayılan olarak devre dışıdır, ancak oyun içinde açılıp kapatılabilir ve dışa aktarırken de açılıp kapatılabilmesi planlanmaktadır.

# Çeşitli
## Tarif (tekil)
Bu etiket, verilen item yığını için bulunan ilk tarifi gösterir. Örneğin:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftreborn:gear_stone"/>

## Tarifler
Bu etiket, verilen item yığınının tüm tariflerini gösterir. Örneğin:
<guide_md>
&lt;recipes stack="buildcraftreborn:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftreborn:gear_wood" chapter_level="2"/>

## Kullanımlar
Tariflere benzer şekilde bu da verilen item yığınının tüm kullanımlarını gösterir. Örneğin:
<guide_md>
&lt;usages stack="buildcraftreborn:engine_wood"&gt;
</guide_md>
<usages stack="buildcraftreborn:engine_wood" chapter_level="2"/>

## Tarifler ve kullanımlar
Bu, yukarıdaki iki etiketi fiilen tek bir etikette birleştirir, örneğin:
<guide_md>
&lt;recipes_usages stack="buildcraftreborn:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcraftreborn:pipe_structure" chapter_level="2"/>

## Grup
Bu, verilen item, ifade (statement) vb. için uygun grupları gösterir.

## Görseller
Bu, bir görsel gösterir. 3 argümanı vardır:
* src: Görselin kaynak konumu.
* width: Görselin gösterileceği genişlik. Varsayılan olarak kaynak görselin genişliğidir (ancak bu istediğin şey olmayabilir).
* height: Görselin gösterileceği yükseklik.

Örneğin:
<guide_md>
&lt;image src="buildcraftreborn:item/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftreborn:item/wrench" width="64" height="64"/>
