/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.pipe.flow;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.api.transport.IInjectable;

/** An insert-only item handler for one side of an item pipe, so hoppers and other mods' machines can push items in.
 * (New in the port: 1.12 pipes only took items from BuildCraft's own injection API.) */
public final class InjectableItemHandler implements IItemHandler {
    private static final double INSERT_SPEED = 0.05;

    private final IInjectable injectable;
    private final Direction side;

    public InjectableItemHandler(IInjectable injectable, Direction side) {
        this.injectable = injectable;
        this.side = side;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !injectable.canInjectItems(side)) {
            return stack;
        }
        return injectable.injectItem(stack.copy(), !simulate, side, null, INSERT_SPEED);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }
}
