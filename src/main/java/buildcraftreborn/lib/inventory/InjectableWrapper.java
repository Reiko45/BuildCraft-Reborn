/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.inventory;


import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;

import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.inventory.IItemTransactor;
import buildcraftreborn.api.transport.IInjectable;

import buildcraftreborn.lib.misc.StackUtil;

public class InjectableWrapper implements IItemTransactor {
    private final IInjectable injectable;
    private final Direction from;

    public InjectableWrapper(IInjectable injectable, Direction facing) {
        this.injectable = injectable;
        this.from = facing;
    }

    
    @Override
    public ItemStack insert(ItemStack stack, boolean allOrNone, boolean simulate) {
        if (allOrNone) {
            stack = stack.copy();
            ItemStack leftOver = injectable.injectItem(stack, false, from, null, 0);
            if (leftOver.isEmpty()) {
                ItemStack reallyLeftOver = injectable.injectItem(stack, !simulate, from, null, 0);
                // sanity check: it really helps debugging
                if (!reallyLeftOver.isEmpty()) {
                    throw new IllegalStateException("Found an invalid IInjectable instance! (leftOver = "//
                        + leftOver + ", reallyLeftOver = " + reallyLeftOver + ", " + injectable.getClass() + ")");
                } else {
                    return ItemStack.EMPTY;
                }
            } else {
                return stack;
            }
        } else {
            return injectable.injectItem(stack, !simulate, from, null, 0);
        }
    }

    @Override
    public NonNullList<ItemStack> insert(NonNullList<ItemStack> stacks, boolean simulate) {
        return ItemTransactorHelper.insertAllBypass(this, stacks, simulate);
    }

    
    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        return ItemStack.EMPTY;
    }
}
