/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.tile.item;

import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.lib.misc.StackUtil;

/** An inventory whose slots only accept what the matching slot of another (filter) inventory holds. */
public class ItemHandlerFiltered extends ItemHandlerSimple implements IItemHandlerFiltered {
    private final IItemHandler filter;
    private final boolean emptyIsAnything;

    public ItemHandlerFiltered(IItemHandler filter, boolean emptyIsAnything) {
        super(filter.getSlots());
        this.emptyIsAnything = emptyIsAnything;
        this.filter = filter;
        setChecker((slot, stack) -> {
            ItemStack inSlot = filter.getStackInSlot(slot);
            if (inSlot.isEmpty()) {
                return emptyIsAnything;
            } else {
                return StackUtil.canMerge(stack, inSlot);
            }
        });
    }

    @Override
    public int getSlotLimit(int slot) {
        if (emptyIsAnything || !getFilter(slot).isEmpty()) {
            return super.getSlotLimit(slot);
        } else {
            return 0;
        }
    }

    @Override
    public ItemStack getFilter(int slot) {
        ItemStack current = getStackInSlot(slot);
        if (!current.isEmpty()) {
            return current;
        }
        return filter.getStackInSlot(slot);
    }
}
