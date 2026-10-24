/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.inventory;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.inventory.IItemTransactor;

/** A transactor that holds nothing and accepts nothing. */
public enum NoSpaceTransactor implements IItemTransactor {
    INSTANCE;

    @Override
    public ItemStack insert(ItemStack stack, boolean allOrNone, boolean simulate) {
        return stack;
    }

    @Override
    public NonNullList<ItemStack> insert(NonNullList<ItemStack> stacks, boolean simulate) {
        return stacks;
    }

    @Override
    public ItemStack extract(@Nullable IStackFilter filter, int min, int max, boolean simulate) {
        return ItemStack.EMPTY;
    }
}
