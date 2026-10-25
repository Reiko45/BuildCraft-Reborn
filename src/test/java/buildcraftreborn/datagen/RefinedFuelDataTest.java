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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

/** The combustion engine values for other mods' refined fuels, as agreed for 1.2.2, in the shipped fuel data map. A
 * change to them should be a decision, not an accident. */
public class RefinedFuelDataTest {
    private static final Path FUELS = Path.of("src/generated/resources/data/buildcraftreborn/data_maps/fluid/fuels.json");

    /** Tag: MJ a tick, ticks a bucket. */
    private static final Map<String, double[]> AGREED = new LinkedHashMap<>();

    static {
        AGREED.put("#c:gasoline", new double[] { 7, 12_000 });
        AGREED.put("#c:kerosene", new double[] { 6, 15_000 });
        AGREED.put("#c:diesel", new double[] { 5, 19_200 });
        AGREED.put("#c:biodiesel", new double[] { 5, 16_000 });
        AGREED.put("#c:ethanol", new double[] { 4, 12_500 });
        AGREED.put("#c:bioethanol", new double[] { 4, 12_500 });
        AGREED.put("#c:lpg", new double[] { 8, 10_000 });
        AGREED.put("#c:high_power_biodiesel", new double[] { 6, 20_000 });
        AGREED.put("#c:boosted_diesel", new double[] { 6, 20_000 });
        // and c:fuel stays BuildCraft's light fuel
        AGREED.put("#c:fuel", new double[] { 6, 15_000 });
    }

    @Test
    public void refinedFuelsHaveTheAgreedValues() throws IOException {
        Assert.assertTrue("Run from the project directory: " + FUELS.toAbsolutePath(), Files.isRegularFile(FUELS));
        String json = Files.readString(FUELS, StandardCharsets.UTF_8);
        for (Map.Entry<String, double[]> entry : AGREED.entrySet()) {
            Matcher m = Pattern.compile(Pattern.quote("\"" + entry.getKey() + "\"") + "\\s*:\\s*\\{([^}]*)\\}")
                .matcher(json);
            Assert.assertTrue(entry.getKey() + " isn't in fuels.json", m.find());
            String body = m.group(1);
            Assert.assertEquals(entry.getKey() + " power", entry.getValue()[0], number(body, "power_per_tick"), 0);
            Assert.assertEquals(entry.getKey() + " burn time", entry.getValue()[1], number(body, "burn_time"), 0);
            Assert.assertFalse(entry.getKey() + " leaves a residue", body.contains("residue"));
        }
    }

    private static double number(String body, String field) {
        Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*([0-9.]+)").matcher(body);
        Assert.assertTrue("No " + field + " in " + body, m.find());
        return Double.parseDouble(m.group(1));
    }
}
