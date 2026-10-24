/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.inventory.filter;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import buildcraftreborn.api.core.IFluidFilter;
import buildcraftreborn.lib.misc.StackUtil;

/** Returns true if the stack matches any one one of the filter stacks. */
public class ArrayFluidFilter implements IFluidFilter {
    protected FluidStack[] fluids;

    public ArrayFluidFilter(ItemStack... stacks) {
        this(StackUtil.listOf(stacks));
    }

    public ArrayFluidFilter(FluidStack... fluids) {
        this.fluids = fluids;
    }

    public ArrayFluidFilter(NonNullList<ItemStack> stacks) {
        fluids = new FluidStack[stacks.size()];
        for (int i = 0; i < stacks.size(); ++i) {
            fluids[i] = FluidUtil.getFluidContained(stacks.get(i)).orElse(FluidStack.EMPTY);
        }
    }

    public boolean hasFilter() {
        for (FluidStack filter : fluids) {
            if (filter != null && !filter.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(FluidStack fluid) {
        for (FluidStack filter : fluids) {
            if (filter != null && !filter.isEmpty() && FluidStack.isSameFluidSameComponents(filter, fluid)) {
                return true;
            }
        }
        return false;
    }
}
