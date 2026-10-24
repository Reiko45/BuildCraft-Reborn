/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.fuels;

import net.neoforged.neoforge.fluids.FluidStack;

public interface IFuel {
    /** @return The input fluid. The amount is ignored. */
    FluidStack getFluid();

    /** @return The number of ticks that a single bucket (1000mb) of this fuel will burn for. */
    int getTotalBurningTime();

    /** @return The amount (in micro mj) of power that this fuel will give off in 1 tick. */
    long getPowerPerCycle();
}
