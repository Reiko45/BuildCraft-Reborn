/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.inventory.filter;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.lib.misc.StackUtil;

/** Matches stacks that match any of the given stacks (see {@link StackUtil#isMatchingItem(ItemStack, ItemStack)}).
 * An empty filter matches everything. */
public class ArrayStackFilter implements IStackFilter {

    protected NonNullList<ItemStack> stacks;

    public ArrayStackFilter(ItemStack... stacks) {
        this.stacks = StackUtil.listOf(stacks);
    }

    @Override
    public boolean matches(ItemStack stack) {
        if (stacks.size() == 0 || !hasFilter()) {
            return true;
        }
        for (ItemStack s : stacks) {
            if (StackUtil.isMatchingItem(s, stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean matches(IStackFilter filter2) {
        for (ItemStack s : stacks) {
            if (filter2.matches(s)) {
                return true;
            }
        }

        return false;
    }

    public NonNullList<ItemStack> getStacks() {
        return stacks;
    }

    public boolean hasFilter() {
        for (ItemStack filter : stacks) {
            if (!filter.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NonNullList<ItemStack> getExamples() {
        return stacks;
    }
}
