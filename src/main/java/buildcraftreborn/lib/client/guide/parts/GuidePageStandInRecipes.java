/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.entry.ItemStackValueFilter;
import buildcraftreborn.lib.client.guide.entry.PageEntryItemStack;
import buildcraftreborn.lib.client.guide.entry.PageValue;
import buildcraftreborn.lib.client.guide.loader.XmlPageLoader;

/** The page of an item that has no page of its own: its recipes and uses. */
public class GuidePageStandInRecipes extends GuidePage {
    public GuidePageStandInRecipes(GuiGuide gui, List<GuidePart> parts, ItemStack stack) {
        super(gui, parts, new PageValue<>(PageEntryItemStack.INSTANCE, new ItemStackValueFilter(stack)));
    }

    public static GuidePageFactory createFactory(ItemStack stack) {
        List<GuidePartFactory> factories = XmlPageLoader.loadAllCrafting(stack, 0);
        if (factories.isEmpty()) {
            return gui -> new GuidePageStandInRecipes(gui, ImmutableList.of(new GuideText(gui, "No recipes!")),
                stack);
        }
        return gui -> {
            List<GuidePart> parts = new ArrayList<>();
            for (GuidePartFactory factory : factories) {
                factory.createAll(gui, parts);
            }
            return new GuidePageStandInRecipes(gui, parts, stack);
        };
    }

    @Override
    public boolean shouldPersistHistory() {
        return false;
    }

    @Override
    public GuidePageBase createReloaded() {
        // Recipes won't have reloaded, so there's no need to change what is displayed.
        return this;
    }
}
