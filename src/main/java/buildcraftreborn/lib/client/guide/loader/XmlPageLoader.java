/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.BCLibConfig;
import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.GuideManager;
import buildcraftreborn.lib.client.guide.GuidePageRegistry;
import buildcraftreborn.lib.client.guide.PageLine;
import buildcraftreborn.lib.client.guide.entry.PageEntry;
import buildcraftreborn.lib.client.guide.entry.PageValueType;
import buildcraftreborn.lib.client.guide.parts.GuideChapterWithin;
import buildcraftreborn.lib.client.guide.parts.GuideImageFactory;
import buildcraftreborn.lib.client.guide.parts.GuidePageEntry;
import buildcraftreborn.lib.client.guide.parts.GuidePageFactory;
import buildcraftreborn.lib.client.guide.parts.GuidePart;
import buildcraftreborn.lib.client.guide.parts.GuidePartCodeBlock;
import buildcraftreborn.lib.client.guide.parts.GuidePartFactory;
import buildcraftreborn.lib.client.guide.parts.GuidePartGroup;
import buildcraftreborn.lib.client.guide.parts.GuidePartLink;
import buildcraftreborn.lib.client.guide.parts.GuidePartMulti;
import buildcraftreborn.lib.client.guide.parts.GuidePartNewPage;
import buildcraftreborn.lib.client.guide.parts.GuideText;
import buildcraftreborn.lib.client.guide.parts.contents.PageLink;
import buildcraftreborn.lib.client.guide.parts.contents.PageLinkNormal;
import buildcraftreborn.lib.client.guide.parts.recipe.IStackRecipes;
import buildcraftreborn.lib.client.guide.parts.recipe.RecipeLookupHelper;
import buildcraftreborn.lib.client.guide.ref.GuideGroupManager;
import buildcraftreborn.lib.client.guide.ref.GuideGroupSet.GroupDirection;
import buildcraftreborn.lib.client.guide.ref.GuideGroupSet;
import buildcraftreborn.lib.gui.ISimpleDrawable;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.StringUtilBC;
import buildcraftreborn.lib.misc.data.OptionallyDisabled;

/** Loads the body of a guide page. This isn't a proper XML loader - there isn't a root tag. Instead it just assumes
 * everything is a paragraph, unless more specific tags are given:
 * <ul>
 * <li>nesting: {@code <lore>}, {@code <no_lore>}, {@code <hint>}, {@code <no_hint>}, {@code <detail>},
 * {@code <no_detail>}, {@code <json_insn>} and {@code <guide_md>} (code blocks)</li>
 * <li>formatting, within a line: {@code <bold>}, {@code <italic>}, {@code <underline>}, {@code <strikethrough>} and
 * the colours ({@code <red>}, {@code <dark_green>}...)</li>
 * <li>single: {@code <new_page/>}, {@code <chapter name="" level=""/>}, {@code <recipe stack=""/>},
 * {@code <recipes stack=""/>}, {@code <usages stack=""/>}, {@code <recipes_usages stack=""/>},
 * {@code <image src="" width="" height=""/>}, {@code <link to="" type=""/>}, {@code <group domain="" group=""/>}</li>
 * </ul>
 * 1.21: the recipe tags look up recipes when the page is opened (recipes come from the world now), and an item's
 * {@code data} (damage) and {@code nbt} attributes are gone. */
public enum XmlPageLoader {
    INSTANCE;

    public static final Map<String, SpecialParser> TAG_FACTORIES = new HashMap<>();
    public static final Map<String, MultiPartJoiner> GUIDE_PART_MULTIS = new HashMap<>();

    /** Used to show "in-game" guide information, narrated from the perspective of the player. (As if they were actively
     * researching how everything worked). The inverse is for a more formal, wikipedia-like tone used to convey the
     * information, pure and simple. */
    public static boolean SHOW_LORE = true;

    /** Used to show extra "hints" that the described thing can be used for, or different ways of using it in
     * combinations with other blocks/items. */
    public static boolean SHOW_HINTS = false;

    /** Used to show all of the numbers used when calculating various things, like pipe flow rate, extraction rate,
     * pulse rate, etc. */
    public static boolean shouldShowDetail() {
        return BCLibConfig.guideShowDetail();
    }

    /** A plain line of text. */
    public record GuideTextFactory(String text) implements GuidePartFactory {
        @Override
        public GuidePart createNew(GuiGuide gui) {
            return new GuideText(gui, text);
        }
    }

    @FunctionalInterface
    public interface SpecialParser {
        @Nullable
        List<GuidePartFactory> parse(XmlTag tag);
    }

    @FunctionalInterface
    public interface SpecialParserSingle extends SpecialParser {
        @Override
        default List<GuidePartFactory> parse(XmlTag tag) {
            GuidePartFactory single = parseSingle(tag);
            return single == null ? null : ImmutableList.of(single);
        }

        @Nullable
        GuidePartFactory parseSingle(XmlTag tag);
    }

    @FunctionalInterface
    public interface MultiPartJoiner {
        @Nullable
        GuidePartFactory join(XmlTag tag, List<GuidePartFactory> factories);
    }

    static {
        // Note that text is done separately, so its not registered here
        putDuelMultiPartType("lore", () -> SHOW_LORE);
        putDuelMultiPartType("detail", XmlPageLoader::shouldShowDetail);
        putDuelMultiPartType("hint", () -> SHOW_HINTS);
        putSingle("new_page", tag -> GuidePartNewPage::new);
        putSingle("chapter", XmlPageLoader::loadChapter);
        putSingle("recipe", XmlPageLoader::loadRecipe);
        putSingle("group", XmlPageLoader::loadGroup);
        putSingle("link", XmlPageLoader::loadLink);
        putSingle("recipes", tag -> deferred(tag, RecipeLookupHelper::getAllRecipes));
        putSingle("usages", tag -> deferred(tag, RecipeLookupHelper::getAllUsages));
        putSingle("recipes_usages", XmlPageLoader::loadAllRecipesAndUsages);
        putSingle("image", XmlPageLoader::loadImage);
        putCode("json_insn");
        putCode("guide_md");
    }

    public static void putDuelMultiPartType(String name, BooleanSupplier isVisible) {
        putSimpleMultiPartType(name, isVisible);
        putSimpleMultiPartType("no_" + name, () -> !isVisible.getAsBoolean());
    }

    public static void putSimpleMultiPartType(String name, BooleanSupplier isVisible) {
        putMultiPartType(name, (tag, factories) -> gui -> {
            List<GuidePart> subParts = new ArrayList<>(factories.size());
            for (GuidePartFactory factory : factories) {
                factory.createAll(gui, subParts);
            }
            return new GuidePartMulti(gui, subParts, isVisible);
        });
    }

    /** A code block: its text, with a few colours added. */
    public static void putCode(String name) {
        putMultiPartType(name, (tag, factories) -> {
            List<String> lines = new ArrayList<>();
            for (GuidePartFactory factory : factories) {
                if (factory instanceof GuideTextFactory text) {
                    lines.add(text.text());
                }
            }
            for (int i = 0; i < lines.size(); i++) {
                String str = lines.get(i);
                if (str.startsWith("~{") && str.endsWith("}") && str.indexOf('{', 2) == -1
                    && str.indexOf('}') == str.length() - 1) {
                    lines.set(i, ChatFormatting.DARK_PURPLE + str);
                    continue;
                }
                str = str.replace("{", ChatFormatting.DARK_GREEN + "{" + ChatFormatting.RESET);
                str = str.replace("}", ChatFormatting.DARK_GREEN + "}" + ChatFormatting.RESET);
                str = str.replaceAll("\"(.+)\"", ChatFormatting.DARK_BLUE + "$0" + ChatFormatting.RESET);
                str = str.replaceAll("%[0-9]+", ChatFormatting.DARK_PURPLE + "$0" + ChatFormatting.RESET);
                str = str.replaceAll("//", ChatFormatting.DARK_GREEN + "//");
                lines.set(i, str);
            }
            return gui -> new GuidePartCodeBlock(gui, lines);
        });
    }

    public static void putMultiPartType(String name, MultiPartJoiner joiner) {
        GUIDE_PART_MULTIS.put(name, joiner);
    }

    public static void putSingle(String string, SpecialParserSingle parser) {
        putMulti(string, parser);
    }

    public static void putMulti(String string, SpecialParser parser) {
        TAG_FACTORIES.put(string, parser);
    }

    public GuidePageFactory loadPage(BufferedReader reader, ResourceLocation name, PageEntry<?> entry)
        throws IOException {
        Deque<List<GuidePartFactory>> nestedParts = new ArrayDeque<>();
        Deque<XmlTag> nestedTags = new ArrayDeque<>();
        nestedParts.push(new ArrayList<>());
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("//")) {
                // Ignore comments
                continue;
            }
            if (line.startsWith("\\/\\/")) {
                line = "//" + line.substring(4);
            }
            XmlTag tag = parseTag(line);
            if (tag != null) {
                if (tag.state == XmlTagState.COMPLETE) {
                    SpecialParser parser = TAG_FACTORIES.get(tag.name);
                    if (parser != null) {
                        List<GuidePartFactory> factories = parser.parse(tag);
                        if (factories != null) {
                            nestedParts.peek().addAll(factories);
                            line = line.substring(tag.originalString.length());
                        } else {
                            line = markError(line, tag);
                        }
                    }
                } else if (tag.state == XmlTagState.START) {
                    MultiPartJoiner joiner = GUIDE_PART_MULTIS.get(tag.name);
                    if (joiner != null) {
                        nestedTags.push(tag);
                        nestedParts.push(new ArrayList<>());
                        line = line.substring(tag.originalString.length());
                    } else if (ChatFormatting.getByName(tag.name) == null) {
                        line = markError(line, tag);
                    }
                } else {
                    MultiPartJoiner joiner = GUIDE_PART_MULTIS.get(tag.name);
                    if (joiner != null) {
                        if (nestedTags.isEmpty()) {
                            throw new IOException("Tried to close " + tag.name + " before opening it!");
                        }
                        XmlTag nameTag = nestedTags.pop();
                        if (!tag.name.equals(nameTag.name)) {
                            throw new IOException("Tried to close " + tag.name + " instead of " + nameTag.name + "!");
                        }
                        List<GuidePartFactory> subParts = nestedParts.pop();
                        GuidePartFactory joined = joiner.join(nameTag, subParts);
                        if (joined == null) {
                            nestedParts.peek().addAll(subParts);
                            line = markError(line, tag);
                        } else {
                            nestedParts.peek().add(joined);
                            line = line.substring(tag.originalString.length());
                        }
                    }
                }
                if (line.isEmpty()) {
                    continue;
                }
            }
            // Last: add remaining elements as text
            nestedParts.peek().add(new GuideTextFactory(formatLine(line.isEmpty() ? " " : line)));
        }
        List<GuidePartFactory> factories = nestedParts.pop();
        if (!nestedParts.isEmpty()) {
            throw new IOException("We haven't closed " + nestedTags);
        }
        return gui -> {
            List<GuidePart> parts = new ArrayList<>();
            for (GuidePartFactory factory : factories) {
                factory.createAll(gui, parts);
            }
            return new GuidePageEntry(gui, parts, entry, name);
        };
    }

    /** Shows a tag that couldn't be used in red. */
    private static String markError(String line, XmlTag tag) {
        int len = tag.originalString.length();
        return "<red>" + line.substring(0, len).replace("<", "&lt;").replace(">", "&gt;") + "</red>"
            + line.substring(len);
    }

    /** Replaces the formatting tags in a line of text with formatting codes. */
    private static String formatLine(String line) throws IOException {
        Set<ChatFormatting> formattingElements = EnumSet.noneOf(ChatFormatting.class);
        Deque<ChatFormatting> formatColours = new ArrayDeque<>();
        StringBuilder completeLine = new StringBuilder();
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c == '<') {
                XmlTag currentTag = parseTag(line.substring(i));
                ChatFormatting formatting = currentTag == null ? null : ChatFormatting.getByName(currentTag.name);
                if (formatting != null) {
                    if (currentTag.state == XmlTagState.END) {
                        formattingElements.remove(formatting);
                        if (formatColours.peek() == formatting) {
                            formatColours.remove();
                        }
                    } else if (currentTag.state == XmlTagState.START) {
                        if (formatting.isColor()) {
                            formatColours.push(formatting);
                        } else {
                            formattingElements.add(formatting);
                        }
                    }
                    completeLine.append(ChatFormatting.RESET);
                    if (formatColours.peek() != null) {
                        completeLine.append(formatColours.peek());
                    }
                    for (ChatFormatting format : formattingElements) {
                        completeLine.append(format);
                    }
                    i += currentTag.originalString.length();
                    continue;
                }
            } else if (line.startsWith("&lt;", i)) {
                c = '<';
                i += 3;
            } else if (line.startsWith("&gt;", i)) {
                c = '>';
                i += 3;
            }
            completeLine.append(c);
            i++;
        }
        return completeLine.toString();
    }

    /** Parses a single tag. Note that the tag might not be the length of the whole string. */
    @Nullable
    public static XmlTag parseTag(String string) throws IOException {
        if (!string.startsWith("<")) {
            return null;
        }
        // Its a tag, hopefully its complete
        int end = string.indexOf('>');
        if (end < 0) {
            throw new IOException("Didn't find an end tag for " + string);
        }
        String tagContents = string.substring(1, end);
        boolean hasStart = tagContents.startsWith("/");
        if (hasStart) {
            tagContents = tagContents.substring(1);
        }
        boolean hasEnd = tagContents.endsWith("/");
        if (hasEnd) {
            tagContents = tagContents.substring(0, tagContents.length() - 1);
        }
        int paramStart = tagContents.indexOf(' ');
        String tag;
        Map<String, String> attributes;
        if (paramStart < 0) {
            tag = tagContents;
            attributes = ImmutableMap.of();
        } else {
            tag = tagContents.substring(0, paramStart);
            attributes = parseAttributes(tagContents.substring(paramStart + 1));
        }
        XmlTagState state = hasEnd ? XmlTagState.COMPLETE : hasStart ? XmlTagState.END : XmlTagState.START;
        return new XmlTag(tag, attributes, state, string.substring(0, end + 1));
    }

    /** Parses {@code key="value" key2=word}. (1.12 used its expression tokenizer for this.) */
    private static Map<String, String> parseAttributes(String attribs) throws IOException {
        Map<String, String> attributes = new HashMap<>();
        int i = 0;
        while (i < attribs.length()) {
            while (i < attribs.length() && attribs.charAt(i) == ' ') {
                i++;
            }
            int eq = attribs.indexOf('=', i);
            if (eq < 0) {
                break;
            }
            String key = attribs.substring(i, eq).trim();
            i = eq + 1;
            String value;
            if (i < attribs.length() && attribs.charAt(i) == '"') {
                int close = attribs.indexOf('"', i + 1);
                if (close < 0) {
                    throw new IOException("Not a valid tag value " + attribs.substring(eq + 1));
                }
                value = attribs.substring(i + 1, close);
                i = close + 1;
            } else {
                int space = attribs.indexOf(' ', i);
                int stop = space < 0 ? attribs.length() : space;
                value = attribs.substring(i, stop);
                i = stop;
            }
            attributes.put(key, value);
        }
        return attributes;
    }

    public enum XmlTagState {
        /** {@code <tag>} */
        START,
        /** {@code <tag/>} */
        COMPLETE,
        /** {@code </tag>} */
        END;
    }

    public static class XmlTag {
        public final String name;
        public final Map<String, String> attributes;
        public final XmlTagState state;
        public final String originalString;

        public XmlTag(String name, Map<String, String> attributes, XmlTagState state, String originalString) {
            this.name = name;
            this.attributes = attributes;
            this.state = state;
            this.originalString = originalString;
        }

        @Nullable
        public String get(String key) {
            return attributes.get(key);
        }

        @Override
        public String toString() {
            return originalString;
        }
    }

    @Nullable
    private static GuidePartFactory loadChapter(XmlTag tag) {
        String name = tag.get("name");
        String level = tag.get("level");
        if (name == null) {
            BuildCraftReborn.LOGGER.warn("[lib.guide.loader.xml] Found a chapter tag without a name! {}", tag);
            return null;
        }
        try {
            return chapter(name, level == null ? 0 : Integer.parseInt(level));
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    @Nullable
    private static GuidePartFactory loadLink(XmlTag tag) {
        String to = tag.get("to");
        String type = tag.get("type");
        if (to == null) {
            BuildCraftReborn.LOGGER.warn("[lib.guide.loader.xml] Found a link tag without a 'to' tag! {}", tag);
            return null;
        }
        if (type == null) {
            ResourceLocation location = ResourceLocation.tryParse(to);
            // The entries may not all be loaded yet, so look this one up when the page is made
            return gui -> {
                PageEntry<?> entry = location == null ? null
                    : GuidePageRegistry.INSTANCE.getReloadableEntryMap().get(location);
                if (entry == null) {
                    return new GuideText(gui, ChatFormatting.RED + "Unknown page " + to);
                }
                ISimpleDrawable icon = entry.createDrawable();
                PageLine line = new PageLine(icon, icon, 2, StringUtilBC.formatStringForWhite(entry.title), true);
                PageLink link = new PageLinkNormal(line, true, entry.getTooltip(), g -> {
                    GuidePageFactory factory = GuideManager.INSTANCE.getFactoryFor(location);
                    return factory == null ? null : factory.createNew(g);
                });
                return new GuidePartLink(gui, link);
            };
        }
        PageValueType<?> valueType = GuidePageRegistry.INSTANCE.types.get(type);
        if (valueType == null) {
            BuildCraftReborn.LOGGER.warn("[lib.guide.loader.xml] Found a link tag with an unknown 'type'! (valid ones "
                + "are {}) {}", GuidePageRegistry.INSTANCE.types.keySet(), tag);
            return null;
        }
        OptionallyDisabled<PageLink> link = valueType.createLink(to);
        if (!link.isPresent()) {
            BuildCraftReborn.LOGGER.warn("[lib.guide.loader.xml] Found a link tag that didn't link to anything valid: "
                + "{} {}", link.getDisabledReason(), tag);
            return null;
        }
        return gui -> new GuidePartLink(gui, link.get());
    }

    @Nullable
    private static GuidePartFactory loadImage(XmlTag tag) {
        String src = tag.get("src");
        if (src == null) {
            BuildCraftReborn.LOGGER.warn("[lib.guide.loader.xml] Found an image tag without an src! {}", tag);
            return null;
        }
        return new GuideImageFactory(src, parseInt("width", -1, tag), parseInt("height", -1, tag));
    }

    private static int parseInt(String name, int defaultValue, XmlTag tag) {
        String value = tag.get(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            BuildCraftReborn.LOGGER.warn("[lib.guide.loader.xml] Found an invalid number for {} in {}", name, tag);
            return defaultValue;
        }
    }

    /** The first recipe of a stack ({@code type} picks the kind of recipe: smelting, crafting or assembling). */
    @Nullable
    private static GuidePartFactory loadRecipe(XmlTag tag) {
        ItemStack stack = loadItemStack(tag);
        if (stack == null) {
            return null;
        }
        String type = tag.get("type");
        IStackRecipes recipes = type == null ? null : RecipeLookupHelper.handlerTypes.get(type);
        if (type != null && recipes == null) {
            BuildCraftReborn.LOGGER.warn("[lib.guide.loader.xml] Unknown recipe type {} - must be one of {}", type,
                RecipeLookupHelper.handlerTypes.keySet());
        }
        return deferred(tag, s -> {
            List<GuidePartFactory> list = recipes == null ? RecipeLookupHelper.getAllRecipes(s) : recipes.getRecipes(s);
            return list == null || list.isEmpty() ? List.of() : List.of(list.get(0));
        });
    }

    @Nullable
    private static GuidePartFactory loadAllRecipesAndUsages(XmlTag tag) {
        String chapterLevelStr = tag.get("chapter_level");
        int chapterLevel = 0;
        if (chapterLevelStr != null) {
            try {
                chapterLevel = Integer.parseInt(chapterLevelStr);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        int level = chapterLevel;
        return deferred(tag, stack -> loadAllCrafting(stack, level));
    }

    /** A part made of whatever a lookup (of recipes) finds for the tag's stack, when the page is made. */
    @Nullable
    private static GuidePartFactory deferred(XmlTag tag,
        java.util.function.Function<ItemStack, List<GuidePartFactory>> lookup) {
        ItemStack stack = loadItemStack(tag);
        if (stack == null) {
            return null;
        }
        return new GuidePartFactory() {
            @Override
            public GuidePart createNew(GuiGuide gui) {
                List<GuidePart> parts = new ArrayList<>();
                createAll(gui, parts);
                return new GuidePartMulti(gui, parts, () -> true);
            }

            @Override
            public void createAll(GuiGuide gui, List<GuidePart> out) {
                for (GuidePartFactory factory : lookup.apply(stack)) {
                    factory.createAll(gui, out);
                }
            }
        };
    }

    public static List<GuidePartFactory> loadAllCrafting(ItemStack stack, int chapterLevel) {
        List<GuidePartFactory> list = new ArrayList<>();
        List<GuidePartFactory> recipeParts = RecipeLookupHelper.getAllRecipes(stack);
        if (!recipeParts.isEmpty()) {
            list.add(GuidePartNewPage::new);
            list.add(chapter(recipeParts.size() == 1 ? "buildcraft.guide.recipe.create"
                : "buildcraft.guide.recipe.create.plural", chapterLevel));
            list.addAll(recipeParts);
        }
        List<GuidePartFactory> usageParts = RecipeLookupHelper.getAllUsages(stack);
        // Ensure we don't have any duplicate recipes
        usageParts.removeAll(recipeParts);
        if (!usageParts.isEmpty()) {
            if (recipeParts.size() != 1) {
                list.add(GuidePartNewPage::new);
            }
            list.add(chapter(usageParts.size() == 1 ? "buildcraft.guide.recipe.use"
                : "buildcraft.guide.recipe.use.plural", chapterLevel));
            list.addAll(usageParts);
        }
        return list;
    }

    /** Adds an item's recipes and usages to the end of its page (if the page didn't show them already). */
    public static void appendAllCrafting(ItemStack stack, List<GuidePart> parts, GuiGuide gui) {
        List<GuidePart> recipeParts = new ArrayList<>();
        for (GuidePartFactory factory : RecipeLookupHelper.getAllRecipes(stack)) {
            factory.createAll(gui, recipeParts);
        }
        recipeParts.removeAll(parts);
        if (!recipeParts.isEmpty()) {
            parts.add(new GuidePartNewPage(gui));
            parts.add(chapter(recipeParts.size() == 1 ? "buildcraft.guide.recipe.create"
                : "buildcraft.guide.recipe.create.plural", 0).createNew(gui));
            parts.addAll(recipeParts);
        }
        List<GuidePart> usageParts = new ArrayList<>();
        for (GuidePartFactory factory : RecipeLookupHelper.getAllUsages(stack)) {
            factory.createAll(gui, usageParts);
        }
        usageParts.removeAll(parts);
        if (!usageParts.isEmpty()) {
            if (usageParts.size() != 1) {
                parts.add(new GuidePartNewPage(gui));
            }
            parts.add(chapter(usageParts.size() == 1 ? "buildcraft.guide.recipe.use"
                : "buildcraft.guide.recipe.use.plural", 0).createNew(gui));
            parts.addAll(usageParts);
        }
    }

    public static GuidePartFactory chapter(String after) {
        return chapter(after, 0);
    }

    public static GuidePartFactory chapter(String after, int level) {
        return gui -> new GuideChapterWithin(gui, level, LocaleUtil.localize(after));
    }

    public static GuidePartFactory translate(String text) {
        return gui -> new GuideText(gui, new PageLine(0, LocaleUtil.localize(text), false));
    }

    @Nullable
    public static GuidePartFactory loadGroup(XmlTag tag) {
        String domain = tag.get("domain");
        String group = tag.get("group");
        if (domain == null || group == null) {
            BuildCraftReborn.LOGGER.warn("[lib.guide.loader.xml] Missing domain or group in {}", tag);
            return null;
        }
        GuideGroupSet set = GuideGroupManager.get(domain, group);
        if (set == null) {
            BuildCraftReborn.LOGGER.warn("[lib.guide.loader.xml] Unknown group {}:{}", domain, group);
            return null;
        }
        return gui -> new GuidePartGroup(gui, set, GroupDirection.SRC_TO_ENTRY);
    }

    @Nullable
    public static ItemStack loadItemStack(XmlTag tag) {
        String id = tag.get("stack");
        if (id == null) {
            BuildCraftReborn.LOGGER.warn("[lib.guide.loader.xml] Missing 'stack' for an itemstack from {}", tag);
            return null;
        }
        OptionallyDisabled<ItemStack> stack = parseItemStack(id);
        if (!stack.isPresent()) {
            BuildCraftReborn.LOGGER.warn("[lib.guide.loader.xml] {}", stack.getDisabledReason());
            return null;
        }
        String count = tag.get("count");
        if (count != null) {
            try {
                stack.get().setCount(Integer.parseInt(count.trim()));
            } catch (NumberFormatException nfe) {
                BuildCraftReborn.LOGGER.warn("[lib.guide.loader.xml] {} was not a valid number", count);
            }
        }
        return stack.get();
    }

    /** Parses {@code "item_id"} or {@code "item_id, count"}. */
    public static OptionallyDisabled<ItemStack> parseItemStack(String line) {
        String[] args = line.split(",");
        ResourceLocation id = ResourceLocation.tryParse(args[0].trim());
        Item item = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item == null) {
            return new OptionallyDisabled<>(args[0] + " was not a valid item!");
        }
        ItemStack stack = new ItemStack(item);
        if (args.length > 1) {
            try {
                stack.setCount(Integer.parseInt(args[1].trim()));
            } catch (NumberFormatException nfe) {
                return new OptionallyDisabled<>(args[1] + " was not a valid number: " + nfe.getLocalizedMessage());
            }
        }
        return new OptionallyDisabled<>(stack);
    }
}
