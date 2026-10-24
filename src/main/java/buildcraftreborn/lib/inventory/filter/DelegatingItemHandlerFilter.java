/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.inventory.filter;


import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.api.core.IStackFilter;

public class DelegatingItemHandlerFilter implements IStackFilter {
    private final ISingleStackFilter perStackFilter;
    private final IItemHandler handler;

    public DelegatingItemHandlerFilter(ISingleStackFilter perStackFilter, IItemHandler handler) {
        this.perStackFilter = perStackFilter;
        this.handler = handler;
    }

    @Override
    public boolean matches(ItemStack stack) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (perStackFilter.matches(handler.getStackInSlot(slot), stack)) {
                return true;
            }
        }
        return false;
    }
}
