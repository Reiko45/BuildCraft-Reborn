/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.inventory;

import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.api.core.IStackFilter;

/** An {@link buildcraftreborn.api.inventory.IItemTransactor} view of a NeoForge {@link IItemHandler}. */
public final class ItemHandlerWrapper extends AbstractInvItemTransactor {
    private final IItemHandler wrapped;

    public ItemHandlerWrapper(IItemHandler handler) {
        this.wrapped = handler;
    }

    @Override
    protected ItemStack insert(int slot, ItemStack stack, boolean simulate) {
        return wrapped.insertItem(slot, stack, simulate);
    }

    @Override
    protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
        if (min <= 0) min = 1;
        if (max < min) return ItemStack.EMPTY;
        ItemStack current = wrapped.getStackInSlot(slot);
        if (current.isEmpty() || current.getCount() < min) return ItemStack.EMPTY;
        if (filter.matches(asValid(current))) {
            return wrapped.extractItem(slot, max, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected int getSlotCount() {
        return wrapped.getSlots();
    }

    @Override
    protected boolean isEmpty(int slot) {
        return wrapped.getStackInSlot(slot).isEmpty();
    }
}
