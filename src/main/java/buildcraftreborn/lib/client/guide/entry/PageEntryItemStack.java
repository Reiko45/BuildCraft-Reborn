/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.entry;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.BCLibConfig;
import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.GuideManager;
import buildcraftreborn.lib.client.guide.GuidePageRegistry;
import buildcraftreborn.lib.client.guide.data.JsonTypeTags;
import buildcraftreborn.lib.client.guide.loader.XmlPageLoader;
import buildcraftreborn.lib.client.guide.parts.GuidePart;
import buildcraftreborn.lib.client.guide.parts.contents.PageLink;
import buildcraftreborn.lib.client.guide.parts.contents.PageLinkItemPermutations;
import buildcraftreborn.lib.client.guide.parts.contents.PageLinkItemStack;
import buildcraftreborn.lib.gui.GuiStack;
import buildcraftreborn.lib.gui.ISimpleDrawable;
import buildcraftreborn.lib.misc.ItemStackKey;
import buildcraftreborn.lib.misc.LegacyText;
import buildcraftreborn.lib.misc.data.OptionallyDisabled;

/** Pages about items (and blocks). Every item without its own page is listed under "Others", with its recipes.
 * <p>
 * 1.21: the variants of each item are what the creative search tab shows (1.12: {@code getSubItems}), so the list
 * needs a world; outside one, each item is listed once. An entry's {@code "stack"} is an item id, optionally in
 * brackets (1.12's {@code "{id, count, meta}"} form is gone with damage values). */
public class PageEntryItemStack extends PageValueType<ItemStackValueFilter> {
    public static final PageEntryItemStack INSTANCE = new PageEntryItemStack();

    private static final JsonTypeTags TAGS = new JsonTypeTags("buildcraft.guide.contents.item_stacks");

    @Override
    public Class<ItemStackValueFilter> getEntryClass() {
        return ItemStackValueFilter.class;
    }

    @Override
    protected boolean isValid(ItemStackValueFilter typed) {
        return !typed.stack.baseStack.isEmpty();
    }

    @Override
    public void iterateAllDefault(IEntryLinkConsumer consumer) {
        boolean limitDomains = BuiltInRegistries.ITEM.size() > BCLibConfig.guideItemSearchLimit();
        Set<String> domains = new HashSet<>();
        // Always show all of minecraft's and BuildCraft's items
        domains.add("minecraft");
        domains.add(BuildCraftReborn.MODID);
        domains.addAll(GuidePageRegistry.INSTANCE.getSourceDomains());
        if (limitDomains) {
            BuildCraftReborn.LOGGER.warn("[lib.guide] Limiting the domains of items to only {} because there are {}"
                + " items!", domains, BuiltInRegistries.ITEM.size());
        }
        Map<Item, List<ItemStack>> variants = getVariants();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation regName = BuiltInRegistries.ITEM.getKey(item);
            if (item == Items.AIR || (limitDomains && !domains.contains(regName.getNamespace()))) {
                continue;
            }
            if (!GuideManager.INSTANCE.objectsAdded.add(item)) {
                continue;
            }
            List<ItemStack> stacks = variants == null ? List.of(new ItemStack(item)) : variants.get(item);
            if (stacks == null || stacks.isEmpty()) {
                continue;
            }
            if (stacks.size() > 200) {
                // Likely a "super-item" with thousands of variants: show it as one page entry
                consumer.addChild(TAGS, PageLinkItemPermutations.create(false, stacks));
                continue;
            }
            for (ItemStack stack : stacks) {
                consumer.addChild(TAGS, PageLinkItemStack.create(false, stack));
            }
        }
    }

    /** @return The creative search tab's stacks for each item, or null if there is no world to make them for. */
    @Nullable
    private static Map<Item, List<ItemStack>> getVariants() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return null;
        }
        boolean operatorItems = mc.options.operatorItemsTab().get() && mc.player.canUseGameMasterBlocks();
        CreativeModeTabs.tryRebuildTabContents(mc.level.enabledFeatures(), operatorItems, mc.level.registryAccess());
        Map<Item, List<ItemStack>> map = new LinkedHashMap<>();
        for (ItemStack stack : CreativeModeTabs.searchTab().getDisplayItems()) {
            map.computeIfAbsent(stack.getItem(), i -> new java.util.ArrayList<>()).add(stack);
        }
        return map;
    }

    @Override
    public OptionallyDisabled<PageLink> createLink(String to) {
        OptionallyDisabled<ItemStack> stack = XmlPageLoader.parseItemStack(to);
        if (stack.isPresent()) {
            return new OptionallyDisabled<>(PageLinkItemStack.create(true, stack.get()));
        }
        return new OptionallyDisabled<>(stack.getDisabledReason());
    }

    @Override
    public OptionallyDisabled<PageEntry<ItemStackValueFilter>> deserialize(ResourceLocation name, JsonObject json) {
        if (!json.has("stack")) {
            throw new JsonSyntaxException("Expected a string for 'stack', but got nothing for " + json);
        }
        String str = GsonHelper.getAsString(json, "stack");
        if (str.startsWith("(") && str.endsWith(")")) {
            str = str.substring(1, str.length() - 1);
        }
        ResourceLocation loc = ResourceLocation.tryParse(str.trim());
        if (loc == null || !BuiltInRegistries.ITEM.containsKey(loc)) {
            return new OptionallyDisabled<>("The item '" + str + "' doesn't exist.");
        }
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(loc));
        ItemStackValueFilter filter = new ItemStackValueFilter(new ItemStackKey(stack), false);
        return new OptionallyDisabled<>(new PageEntry<>(this, name, json, filter));
    }

    @Override
    public String getTitle(ItemStackValueFilter value) {
        return LegacyText.toLegacy(value.stack.baseStack.getHoverName());
    }

    @Override
    public List<String> getTooltip(ItemStackValueFilter value) {
        return LegacyText.getFormattedTooltip(value.stack.baseStack);
    }

    @Override
    public boolean matches(ItemStackValueFilter entry, Object obj) {
        if (obj instanceof ItemStackValueFilter filter) {
            obj = filter.stack.baseStack;
        }
        if (obj instanceof ItemStackKey key) {
            obj = key.baseStack;
        }
        if (obj instanceof ItemStack test) {
            ItemStack base = entry.stack.baseStack;
            if (base.isEmpty() || test.isEmpty() || base.getItem() != test.getItem()) {
                return false;
            }
            return !entry.matchComponents || ItemStack.isSameItemSameComponents(base, test);
        }
        return false;
    }

    @Override
    @Nullable
    public ISimpleDrawable createDrawable(ItemStackValueFilter value) {
        return new GuiStack(value.stack.baseStack);
    }

    @Override
    public Object getBasicValue(ItemStackValueFilter value) {
        return value.stack.baseStack.getItem();
    }

    @Override
    public void addPageEntries(ItemStackValueFilter value, GuiGuide gui, List<GuidePart> parts) {
        XmlPageLoader.appendAllCrafting(value.stack.baseStack, parts, gui);
    }
}
