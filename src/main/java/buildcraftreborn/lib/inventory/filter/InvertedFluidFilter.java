/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.inventory.filter;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.core.IFluidFilter;

public class InvertedFluidFilter implements IFluidFilter {

    public final IFluidFilter delegate;

    public InvertedFluidFilter(IFluidFilter delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean matches(FluidStack fluid) {
        return !delegate.matches(fluid);
    }
}
