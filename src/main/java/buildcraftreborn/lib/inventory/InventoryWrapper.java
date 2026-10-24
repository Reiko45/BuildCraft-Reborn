/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.inventory;


import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.IStackFilter;

import buildcraftreborn.lib.misc.StackUtil;


public final class InventoryWrapper extends AbstractInvItemTransactor {
    private final Container inventory;

    public InventoryWrapper(Container inventory) {
        this.inventory = inventory;
    }

    @Override
    
    protected ItemStack insert(int slot, ItemStack stack, boolean simulate) {
        ItemStack current = inventory.getItem(slot);
        if (!inventory.canPlaceItem(slot, stack)) {
            return stack;
        }
        if (current.isEmpty()) {
            int max = Math.min(inventory.getMaxStackSize(), stack.getMaxStackSize());
            ItemStack split = stack.split(max);
            if (!simulate) {
                inventory.setItem(slot, split);
            }
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            } else {
                return stack;
            }
        }
        if (StackUtil.canMerge(current, stack)) {
            ItemStack merged = current.copy();
            merged.setCount(merged.getCount() + stack.getCount());
            int size = Math.min(inventory.getMaxStackSize(), merged.getMaxStackSize());
            if (merged.getCount() > size) {
                stack.setCount(stack.getCount() - (merged.getCount() - size));
                merged.setCount(size);
                if (!simulate) {
                    inventory.setItem(slot, merged);
                }
                return stack;
            } else {
                if (!simulate) {
                    inventory.setItem(slot, merged);
                }
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    @Override
    
    protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
        ItemStack current = inventory.getItem(slot);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (filter.matches(current.copy())) {
            if (current.getCount() < min) {
                return ItemStack.EMPTY;
            }
            int size = Math.min(current.getCount(), max);
            current = current.copy();
            ItemStack other = current.split(size);
            if (!simulate) {
                if (current.getCount() <= 0) {
                    current = ItemStack.EMPTY;
                }
                inventory.setItem(slot, current);
            }
            return other;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    protected int getSlotCount() {
        return inventory.getContainerSize();
    }

    @Override
    protected boolean isEmpty(int slot) {
        return inventory.getItem(slot).isEmpty();
    }
}
