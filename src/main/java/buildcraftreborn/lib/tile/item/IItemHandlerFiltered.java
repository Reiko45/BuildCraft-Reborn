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

/** An item handler whose slots may hold a "filter" stack that is shown even when the slot is empty. */
public interface IItemHandlerFiltered extends IItemHandler {
    /** @return The filter stack for the given slot (by default, whatever is in the slot). */
    default ItemStack getFilter(int slot) {
        return getStackInSlot(slot);
    }
}
