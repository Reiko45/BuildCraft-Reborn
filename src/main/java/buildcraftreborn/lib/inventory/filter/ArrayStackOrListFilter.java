/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.inventory.filter;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.misc.StackUtil;

/** Like {@link ArrayStackFilter}, but a list in the filter matches what the list matches. */
public class ArrayStackOrListFilter extends ArrayStackFilter {

    public ArrayStackOrListFilter(ItemStack... stacks) {
        super(stacks);
    }

    @Override
    public boolean matches(ItemStack stack) {
        if (stacks.size() == 0 || !hasFilter()) {
            return true;
        }

        for (ItemStack s : stacks) {
            if (StackUtil.isMatchingItemOrList(s, stack)) {
                return true;
            }
        }

        return false;
    }
}
