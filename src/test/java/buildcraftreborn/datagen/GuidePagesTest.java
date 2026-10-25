/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.datagen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

/** Every language has every guide page, laid out as the English one: the same lines (one paragraph each), the same tags
 * in each line (links, recipes, chapters, formatting), and the code examples unchanged. A translated page that lost a
 * tag would show raw markup, a broken link or a missing recipe, and a missing page falls back to English. */
public class GuidePagesTest {
    private static final Path GUIDE = Path.of("src/main/resources/assets/buildcraftreborn/compat/buildcraft/guide");
    private static final Path LANG = Path.of("src/main/resources/assets/buildcraftreborn/lang");
    private static final String ENGLISH = "en_us";
    private static final Pattern TAG = Pattern.compile("<[^<>]+>");
    private static final Pattern ENTITY = Pattern.compile("&lt;.*?&gt;");
    /** A "<" that doesn't start a tag. */
    private static final Pattern STRAY_LT = Pattern.compile("<(?![A-Za-z_/][^<>]*>)");
    private static final Pattern PREFIX = Pattern.compile("^(\\s*(?:#+\\s+|-\\s+)?)");
    private static final Pattern CHAPTER = Pattern.compile("(<chapter\\b[^>]*?\\bname=)\"([^\"]*)\"");
    /** A chapter name that is a translation key, which every language keeps as it is. */
    private static final Pattern KEY = Pattern.compile("^[a-z0-9_]+(\\.[a-z0-9_]+)+$");

    private static Set<String> pages(Path lang) throws IOException {
        try (Stream<Path> files = Files.walk(lang)) {
            return files.filter(p -> p.toString().endsWith(".md"))
                .map(p -> lang.relativize(p).toString().replace('\\', '/'))
                .collect(Collectors.toCollection(TreeSet::new));
        }
    }

    private static List<String> lines(Path file) throws IOException {
        List<String> lines = new ArrayList<>(List.of(Files.readString(file, StandardCharsets.UTF_8).split("\n", -1)));
        while (!lines.isEmpty() && lines.get(lines.size() - 1).isBlank()) {
            lines.remove(lines.size() - 1);
        }
        return lines;
    }

    /** What a line keeps in every language: how it starts (heading, list item), its tags (chapter names only when they
     * are translation keys) and its &amp;lt;...&amp;gt; examples. */
    static String skeleton(String line) {
        StringBuilder sb = new StringBuilder();
        Matcher prefix = PREFIX.matcher(line);
        sb.append(prefix.find() ? prefix.group(1) : "").append('|');
        Matcher tag = TAG.matcher(line);
        while (tag.find()) {
            Matcher chapter = CHAPTER.matcher(tag.group());
            sb.append(chapter.find() && !KEY.matcher(chapter.group(2)).matches()
                ? chapter.replaceFirst(Matcher.quoteReplacement(chapter.group(1)) + "\"*\"") : tag.group());
        }
        sb.append('|');
        Matcher entity = ENTITY.matcher(line);
        while (entity.find()) {
            sb.append(entity.group());
        }
        return sb.toString();
    }

    @Test
    public void everyLanguageHasEveryPageLaidOutAsTheEnglish() throws IOException {
        Set<String> english = pages(GUIDE.resolve(ENGLISH));
        Assert.assertTrue("Only " + english.size() + " English pages", english.size() >= 118);
        List<String> problems = new ArrayList<>();
        Set<String> languages;
        try (Stream<Path> files = Files.list(LANG)) {
            languages = files.map(p -> p.getFileName().toString()).filter(n -> n.endsWith(".json"))
                .map(n -> n.substring(0, n.length() - 5)).collect(Collectors.toCollection(TreeSet::new));
        }
        Assert.assertTrue("Only " + languages.size() + " languages", languages.size() >= 31);
        for (String lang : languages) {
            Path dir = GUIDE.resolve(lang);
            if (!Files.isDirectory(dir)) {
                problems.add(lang + ": no guide pages");
                continue;
            }
            Set<String> translated = pages(dir);
            for (String page : translated) {
                if (!english.contains(page)) {
                    problems.add(lang + "/" + page + " isn't an English page");
                }
            }
            for (String page : english) {
                if (!translated.contains(page)) {
                    problems.add(lang + ": no " + page);
                    continue;
                }
                compare(lang + "/" + page, lines(GUIDE.resolve(ENGLISH).resolve(page)), lines(dir.resolve(page)),
                    problems);
            }
        }
        Assert.assertTrue(problems.size() + " problems:\n" + String.join("\n", problems.subList(0,
            Math.min(problems.size(), 50))), problems.isEmpty());
    }

    private static void compare(String name, List<String> en, List<String> tr, List<String> problems) {
        if (en.size() != tr.size()) {
            problems.add(name + ": " + tr.size() + " lines, English has " + en.size());
            return;
        }
        boolean code = false;
        for (int i = 0; i < en.size(); i++) {
            String e = en.get(i);
            String t = tr.get(i);
            String where = name + " line " + (i + 1);
            if (code) {
                if (!e.equals(t)) {
                    problems.add(where + ": a code example line changed");
                }
                code = !(e.strip().equals("</guide_md>") || e.strip().equals("</json_insn>"));
                continue;
            }
            code = e.strip().equals("<guide_md>") || e.strip().equals("<json_insn>");
            if (e.isBlank() != t.isBlank()) {
                problems.add(where + ": blank in one language and not the other");
            } else if (STRAY_LT.matcher(t).find()) {
                // the page loader reads any "<" as the start of a tag: a less-than sign is "&lt;"
                problems.add(where + ": a \"<\" that isn't a tag");
            } else if (!skeleton(e).equals(skeleton(t))) {
                problems.add(where + ": " + skeleton(t) + " instead of " + skeleton(e));
            }
        }
    }
}
