/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.contents;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.client.guide.PageLine;
import buildcraftreborn.lib.client.guide.entry.ItemStackValueFilter;
import buildcraftreborn.lib.client.guide.entry.PageEntryItemStack;
import buildcraftreborn.lib.client.guide.entry.PageValue;
import buildcraftreborn.lib.client.guide.parts.GuidePage;
import buildcraftreborn.lib.client.guide.parts.GuidePageFactory;
import buildcraftreborn.lib.client.guide.parts.GuidePart;
import buildcraftreborn.lib.misc.ItemStackKey;

/** One link for an item with hundreds of variants: its page lists every variant. */
public final class PageLinkItemPermutations extends PageLink {
    private final List<ItemStack> permutations;

    private PageLinkItemPermutations(PageLine text, boolean startVisible, List<ItemStack> permutations) {
        super(text, startVisible);
        this.permutations = permutations;
    }

    @Override
    public GuidePageFactory getFactoryLink() {
        return gui -> {
            List<GuidePart> parts = new ArrayList<>();
            for (ItemStack stack : permutations) {
                parts.add(PageLinkItemStack.create(true, stack).createGuidePart(gui));
            }
            ItemStackValueFilter filter = new ItemStackValueFilter(new ItemStackKey(permutations.get(0)), false);
            return new GuidePage(gui, parts, new PageValue<>(PageEntryItemStack.INSTANCE, filter));
        };
    }

    public static PageLinkItemPermutations create(boolean startVisible, List<ItemStack> stacks) {
        PageLinkItemStack link = PageLinkItemStack.create(startVisible, stacks.get(0));
        return new PageLinkItemPermutations(link.text, startVisible, stacks);
    }
}
