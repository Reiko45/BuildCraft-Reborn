/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.locale.Language;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.transport.BCTransportItems;
import buildcraftreborn.transport.BCTransportPipes;

/** Phase 6 tests for the translations and advancements: every translation file is valid, only has keys English has,
 * and keeps English's placeholders (a wrong one would break the text); every advancement has its text. */
@GameTestHolder(BuildCraftReborn.MODID)
@PrefixGameTestTemplate(false)
public final class BCLocalisationTests {
    private BCLocalisationTests() {}

    private static final String LANG = "assets/" + BuildCraftReborn.MODID + "/lang/";
    private static final Pattern PLACEHOLDER = Pattern.compile("%(\\d+\\$)?[-#+ 0,(]*\\d*(?:\\.\\d+)?([a-zA-Z%])");

    private static Path modFile(String path) {
        return ModList.get().getModFileById(BuildCraftReborn.MODID).getFile().findResource(path.split("/"));
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private static List<String> placeholders(String text) {
        List<String> list = new ArrayList<>();
        Matcher m = PLACEHOLDER.matcher(text);
        while (m.find()) {
            if (!m.group(2).equals("%")) {
                list.add((m.group(1) == null ? "" : m.group(1)) + m.group(2));
            }
        }
        list.sort(null);
        return list;
    }

    @GameTest(template = "empty")
    public static void translationsMatchEnglish(GameTestHelper helper) throws IOException {
        JsonObject english = readJson(modFile(LANG + "en_us.json"));
        List<String> problems = new ArrayList<>();
        int files = 0;
        try (Stream<Path> list = Files.list(modFile(LANG))) {
            for (Path path : list.toList()) {
                String name = path.getFileName().toString();
                if (name.equals("en_us.json")) {
                    continue;
                }
                files++;
                for (Map.Entry<String, JsonElement> e : readJson(path).entrySet()) {
                    if (!english.has(e.getKey())) {
                        problems.add(name + ": " + e.getKey() + " isn't an English key");
                        continue;
                    }
                    // (indexed and plain %s can be mixed in a translation, so only the types are compared)
                    List<String> want = placeholders(english.get(e.getKey()).getAsString()).stream()
                        .map(p -> p.substring(p.length() - 1)).sorted().toList();
                    List<String> got = placeholders(e.getValue().getAsString()).stream()
                        .map(p -> p.substring(p.length() - 1)).sorted().toList();
                    if (!want.equals(got)) {
                        problems.add(name + ": " + e.getKey() + " has " + got + ", English has " + want);
                    }
                }
            }
        }
        helper.assertTrue(files >= 25, "Only " + files + " translations");
        helper.assertTrue(problems.isEmpty(), "Problems: " + problems);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void advancementsHaveText(GameTestHelper helper) {
        ServerAdvancementManager advancements = helper.getLevel().getServer().getAdvancements();
        Language language = Language.getInstance();
        List<String> problems = new ArrayList<>();
        int count = 0;
        for (AdvancementHolder holder : advancements.getAllAdvancements()) {
            if (!holder.id().getNamespace().equals(BuildCraftReborn.MODID) || holder.id().getPath().startsWith("recipes/")) {
                continue;
            }
            count++;
            holder.value().display().ifPresent(display -> {
                for (var text : List.of(display.getTitle(), display.getDescription())) {
                    if (text.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t
                        && !language.has(t.getKey())) {
                        problems.add(holder.id() + ": no text for " + t.getKey());
                    }
                }
            });
        }
        AdvancementHolder filters = advancements.get(BuildCraftReborn.id("too_many_pipe_filters"));
        helper.assertTrue(filters != null, "No too_many_pipe_filters advancement");
        helper.assertTrue(filters.value().rewards().recipes().contains(BuildCraftReborn.id("list")),
            "too_many_pipe_filters doesn't unlock the list recipe");
        helper.assertTrue(advancements.get(BuildCraftReborn.id("sticky_dipping")) != null, "No sticky_dipping");
        helper.assertTrue(count >= 30, "Only " + count + " advancements");
        helper.assertTrue(problems.isEmpty(), "Problems: " + problems);
        helper.succeed();
    }

    /** Coloured items are named through item.buildcraftreborn.coloured (so other languages can order or bracket the
     * colour), which reads as before in English. */
    @GameTest(template = "empty")
    public static void colouredItemNames(GameTestHelper helper) {
        Map<String, ItemStack> want = new LinkedHashMap<>();
        want.put("Red Pipe Wire", new ItemStack(BCTransportItems.WIRES.get(DyeColor.RED).get()));
        want.put("Light Blue Lens", BCSiliconItems.PLUG_LENS.get().getStack(DyeColor.LIGHT_BLUE, false));
        want.put("Black Filter", BCSiliconItems.PLUG_LENS.get().getStack(DyeColor.BLACK, true));
        want.put("Clear Lens", BCSiliconItems.PLUG_LENS.get().getStack(null, false));
        ItemStack pipe = new ItemStack(BCTransportItems.PIPES.get(BCTransportPipes.woodItem).get());
        want.put("Wooden Transport Pipe", pipe.copy());
        pipe.set(BCItemComponents.COLOUR.get(), DyeColor.GREEN);
        want.put("Green Wooden Transport Pipe", pipe);
        ItemStack brush = new ItemStack(BCCoreItems.PAINTBRUSH.get());
        want.put("Paintbrush", brush.copy());
        brush.set(BCItemComponents.COLOUR.get(), DyeColor.ORANGE);
        want.put("Orange Paintbrush", brush);
        for (Map.Entry<String, ItemStack> e : want.entrySet()) {
            String name = ChatFormatting.stripFormatting(e.getValue().getHoverName().getString());
            helper.assertTrue(e.getKey().equals(name), "Named \"" + name + "\", not \"" + e.getKey() + "\"");
        }
        helper.succeed();
    }
}
