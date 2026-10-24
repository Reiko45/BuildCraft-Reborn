/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.tile.item;

import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

public class CombinedItemHandlerWrapper extends CombinedInvWrapper implements IItemHandlerFiltered {
    public CombinedItemHandlerWrapper(IItemHandlerModifiable... itemHandler) {
        super(itemHandler);
    }

    @Override
    public ItemStack getFilter(int slot) {
        int index = getIndexForSlot(slot);
        IItemHandlerModifiable handler = getHandlerFromIndex(index);
        slot = getSlotFromIndex(slot, index);
        if (handler instanceof IItemHandlerFiltered filtered) {
            return filtered.getFilter(slot);
        }
        return handler.getStackInSlot(slot);
    }
}
