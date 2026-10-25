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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

/** Every language file translates every English entry (so no language falls back to English for some of the text),
 * and uses the same format arguments as the English: a missing or extra {@code %s} shows the wrong text or none. The
 * files are written one entry per line, which is what this reads. */
public class LanguageFilesTest {
    private static final Path EN = Path.of("src/generated/resources/assets/buildcraftreborn/lang/en_us.json");
    private static final Path LANG = Path.of("src/main/resources/assets/buildcraftreborn/lang");
    private static final Pattern ENTRY = Pattern.compile("^\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\",?\\s*$");
    private static final Pattern ARG = Pattern.compile("%(?:(\\d+)\\$)?([sdfx])");

    private static Map<String, String> read(Path file) throws IOException {
        Map<String, String> entries = new LinkedHashMap<>();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            Matcher m = ENTRY.matcher(line);
            if (m.matches()) {
                entries.put(m.group(1), m.group(2));
            }
        }
        return entries;
    }

    /** Argument number to type, as {@code String.format} numbers them. */
    private static Map<Integer, String> args(String text) {
        Map<Integer, String> args = new TreeMap<>();
        Matcher m = ARG.matcher(text.replace("%%", ""));
        int next = 0;
        while (m.find()) {
            args.put(m.group(1) != null ? Integer.parseInt(m.group(1)) : ++next, m.group(2));
        }
        return args;
    }

    @Test
    public void everyLanguageTranslatesEverything() throws IOException {
        Map<String, String> en = read(EN);
        Assert.assertTrue("Couldn't read " + EN.toAbsolutePath(), en.size() > 500);
        List<String> problems = new ArrayList<>();
        int files = 0;
        try (Stream<Path> list = Files.list(LANG)) {
            for (Path file : (Iterable<Path>) list.filter(p -> p.toString().endsWith(".json")).sorted()::iterator) {
                files++;
                String lang = file.getFileName().toString();
                Map<String, String> tr = read(file);
                for (String key : en.keySet()) {
                    if (!tr.containsKey(key)) {
                        problems.add(lang + ": no " + key);
                    } else if (!args(en.get(key)).equals(args(tr.get(key)))) {
                        problems.add(lang + ": " + key + " has other arguments than the English");
                    }
                }
                for (String key : tr.keySet()) {
                    if (!en.containsKey(key)) {
                        problems.add(lang + ": " + key + " isn't in en_us");
                    }
                }
            }
        }
        Assert.assertTrue("Only " + files + " language files", files >= 31);
        Assert.assertTrue(String.join("\n", problems), problems.isEmpty());
    }
}
