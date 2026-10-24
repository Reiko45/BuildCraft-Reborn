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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.statements.StatementManager;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.lib.item.ItemGuide;

/** Phase 6 tests for the guide book. The guide itself only runs on the client, so these check its files: every
 * entry's item or statement exists and has a page, and everything a page links to or shows exists. */
@GameTestHolder(BuildCraftReborn.MODID)
@PrefixGameTestTemplate(false)
public final class BCGuideTests {
    private BCGuideTests() {}

    private static final String BASE = "assets/" + BuildCraftReborn.MODID + "/compat/buildcraft/";
    private static final Pattern ATTRIBUTE = Pattern.compile("<(\\w+)([^>]*)/?>");
    private static final Pattern KEY_VALUE = Pattern.compile("(\\w+)=\"([^\"]*)\"");

    private static Path modFile(String path) {
        return ModList.get().getModFileById(BuildCraftReborn.MODID).getFile().findResource(path.split("/"));
    }

    private static JsonObject readJson(String path) throws IOException {
        try (InputStream in = Files.newInputStream(modFile(path))) {
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    @GameTest(template = "empty")
    public static void guideEntriesAndPagesAreValid(GameTestHelper helper) throws IOException {
        JsonObject books = readJson(BASE + "guide_books.json");
        JsonObject entries = readJson(BASE + "guide_entries.json");
        List<String> problems = new ArrayList<>();
        for (Map.Entry<String, JsonElement> e : entries.entrySet()) {
            String name = e.getKey();
            JsonObject entry = e.getValue().getAsJsonObject();
            String book = entry.get("book").getAsString();
            if (!books.has(ResourceLocation.parse(book).getPath())) {
                problems.add(name + ": unknown book " + book);
            }
            switch (entry.get("type").getAsString()) {
                case "item_stack" -> {
                    if (!BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(entry.get("stack").getAsString()))) {
                        problems.add(name + ": unknown item " + entry.get("stack"));
                    }
                }
                case "statement" -> {
                    if (!StatementManager.statements.containsKey(entry.get("statement").getAsString())) {
                        problems.add(name + ": unknown statement " + entry.get("statement"));
                    }
                }
                case "external" -> {}
                default -> problems.add(name + ": unknown type " + entry.get("type"));
            }
            Path page = modFile(BASE + "guide/en_us/" + name + ".md");
            if (!Files.exists(page)) {
                problems.add(name + ": no page");
                continue;
            }
            String text = Files.readString(page);
            Matcher tags = ATTRIBUTE.matcher(text);
            while (tags.find()) {
                Matcher kv = KEY_VALUE.matcher(tags.group(2));
                while (kv.find()) {
                    String key = kv.group(1);
                    String value = kv.group(2);
                    if (key.equals("stack") && !BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(value))) {
                        problems.add(name + ": shows unknown item " + value);
                    } else if (key.equals("to") && !tags.group(2).contains("type=")) {
                        ResourceLocation to = ResourceLocation.parse(value);
                        if (!entries.has(to.getPath()) || !to.getNamespace().equals(BuildCraftReborn.MODID)) {
                            problems.add(name + ": links to unknown page " + value);
                        }
                    } else if (key.equals("src") && value.contains("textures/")
                        && !Files.exists(modFile("assets/" + value.replace(':', '/')))) {
                        problems.add(name + ": unknown image " + value);
                    }
                }
            }
        }
        helper.assertTrue(entries.size() >= 100, "Only " + entries.size() + " guide entries");
        helper.assertTrue(problems.isEmpty(), "Problems: " + problems);
        helper.succeed();
    }

    /** The guide item remembers its book (no book = the main book), and using one unlocks the guide advancement. */
    @GameTest(template = "empty")
    public static void guideItemOpensItsBook(GameTestHelper helper) {
        ItemStack main = new ItemStack(BCCoreItems.GUIDE.get());
        helper.assertTrue(ItemGuide.getBookName(main).equals(ItemGuide.DEFAULT_BOOK), "Default book is wrong");
        ItemStack meta = main.copy();
        ItemGuide.setBookName(meta, ItemGuide.META_BOOK);
        helper.assertTrue(ItemGuide.getBookName(meta).equals(ItemGuide.META_BOOK), "Meta book not stored");
        ItemGuide.setBookName(meta, ItemGuide.DEFAULT_BOOK);
        helper.assertTrue(ItemStack.isSameItemSameComponents(meta, main), "Setting the main book left a component");

        // (a real player, as fake players don't get advancements; not logged in, so nothing is sent to it)
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
            new GameProfile(UUID.nameUUIDFromBytes("bc_guide".getBytes()), "bc_guide"), ClientInformation.createDefault());
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, main);
            main.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            var advancement = helper.getLevel().getServer().getAdvancements().get(BuildCraftReborn.id("guide"));
            helper.assertTrue(advancement != null, "No guide advancement");
            helper.assertTrue(player.getAdvancements().getOrStartProgress(advancement).isDone(),
                "Using the guide didn't unlock the advancement");
        } finally {
            player.getAdvancements().stopListening();
        }
        helper.succeed();
    }
}
