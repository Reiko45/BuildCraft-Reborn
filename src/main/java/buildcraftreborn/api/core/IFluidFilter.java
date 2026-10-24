/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.core;

import net.neoforged.neoforge.fluids.FluidStack;

public interface IFluidFilter {
    /** @param fluid The fluid to test. Never null, but may be {@link FluidStack#isEmpty() empty}. */
    boolean matches(FluidStack fluid);
}
