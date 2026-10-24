/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
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
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

/** Datagen runs in a development environment, where development-only content (the test machine) is registered. The
 * generated data ships in the released jar, where that content doesn't exist, so it must never mention it: a tag
 * with a missing required entry fails to load as a whole (in 1.1.0 this broke {@code minecraft:mineable/pickaxe}). */
public class ShippedDataTest {
    private static final Path DATA = Path.of("src/generated/resources/data");
    private static final List<String> DEV_ONLY_IDS = List.of("buildcraftreborn:test_machine");

    @Test
    public void generatedDataNeverMentionsDevelopmentOnlyContent() throws IOException {
        Assert.assertTrue("Run from the project directory: " + DATA.toAbsolutePath(), Files.isDirectory(DATA));
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(DATA)) {
            for (Path file : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
                String name = file.getFileName().toString();
                if (name.equals("test_machine.json")) {
                    offenders.add(DATA.relativize(file).toString());
                    continue;
                }
                String text = Files.readString(file, StandardCharsets.UTF_8);
                for (String id : DEV_ONLY_IDS) {
                    if (text.contains(id)) {
                        offenders.add(DATA.relativize(file) + " (" + id + ")");
                    }
                }
            }
        }
        Assert.assertEquals("Generated data references development-only content", List.of(), offenders);
    }
}
