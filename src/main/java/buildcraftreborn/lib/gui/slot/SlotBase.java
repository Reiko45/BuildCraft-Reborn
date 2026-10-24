/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.slot;

import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.SlotItemHandler;

import buildcraftreborn.lib.tile.item.IItemHandlerAdv;

/** A menu slot backed by a BuildCraft item handler. */
public class SlotBase extends SlotItemHandler {
    public final int handlerIndex;
    public final IItemHandlerAdv itemHandler;

    public SlotBase(IItemHandlerAdv itemHandler, int slotIndex, int posX, int posY) {
        super(itemHandler, slotIndex, posX, posY);
        this.handlerIndex = slotIndex;
        this.itemHandler = itemHandler;
    }

    public boolean canShift() {
        return true;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return itemHandler.canSet(handlerIndex, stack);
    }

    /** @return The left over. */
    public ItemStack insert(ItemStack stack, boolean simulate) {
        return getItemHandler().insertItem(handlerIndex, stack, simulate);
    }
}
