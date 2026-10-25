Każda strona poradnika to „wpis”: nazwa, temat strony i jej miejsce w spisie treści. Pakiety zasobów mogą dodawać wpisy, zastępować je lub usuwać.

<chapter name="Pliki wpisów"/>
Wpisy są wymienione w pliku json w każdej przestrzeni nazw pakietu zasobów:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_entries.json
</guide_md>
Zawiera on jeden obiekt, który przypisuje nazwie każdego wpisu jego definicję. Na przykład:
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
Sama strona to osobny plik, nazwany tak jak wpis:
<guide_md>
assets/[namespace]/compat/buildcraft/guide/[language]/[entry name].md
</guide_md>
Strona en_us jest zawsze wczytywana, a strona w twoim języku (jeśli istnieje) ją zastępuje. Co może się znaleźć na stronie, opisuje <link to="buildcraftreborn:config/guide_page_format"/>.

Wyżej położone pakiety zasobów mają pierwszeństwo: wpis o tej samej nazwie zastępuje wpis z niższego pakietu, a wpis ustawiony na null go usuwa.

<chapter name="Typy wpisów"/>
Każdy wpis ma pole "type":
- item_stack: strona o przedmiocie (lub bloku). "stack" to identyfikator przedmiotu.
- statement: strona o wyzwalaczu lub akcji bramki. "statement" to ich identyfikator, np. "buildcraft:redstone.input.active".
- external: strona, która nie dotyczy niczego w grze, tak jak ta. "title" to klucz tłumaczenia, dopisywany po "buildcraft.guide.page.".

Wpis, którego przedmiot, wyzwalacz lub akcja nie istnieje (np. bo jakiś mod nie jest zainstalowany), jest pomijany.

<new_page/>
<chapter name="Spis treści"/>
"book" to księga, w której znajduje się wpis. Poradnik ma księgi "buildcraftreborn:main" (Poradnik BuildCrafta) i "buildcraftreborn:meta" (tę księgę).

"tag_type" i "tag_subtype" określają, gdzie wpis pojawi się w spisie treści. Ich nazwy są tłumaczone kluczami "buildcraft.guide.chapter.type.[tag_type]" i "buildcraft.guide.chapter.subtype.[tag_subtype]".

Księgi są wymienione w ten sam sposób, w pliku:
<guide_md>
assets/[namespace]/compat/buildcraft/guide_books.json
</guide_md>
Na przykład:
<json_insn>
{
  "meta": {
    "title": "buildcraft.guide.book.meta.name",
    "all_entries": false
  }
}
</json_insn>
"title" to klucz tłumaczenia, a "all_entries" określa, czy spis treści księgi obejmuje również wszystkie wpisy, które nie należą do żadnej księgi.

Naciśnij F3 i T przy otwartym poradniku, aby wczytać wszystko ponownie.

<italic>BuildCraft 8.0 definiował wpisy w plikach "guide.txt", we własnym języku instrukcji. BuildCraft Reborn używa zamiast tego zwykłego formatu json.</italic>
