/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.core;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public interface IFluidHandlerAdv extends IFluidHandler {
    /** Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
     *
     * @param filter A filter to filter the possible fluids that can be extracted.
     * @param maxDrain The maximum amount of fluid to drain
     * @param action If {@link FluidAction#SIMULATE}, drain will only be simulated.
     * @return FluidStack representing the Fluid and amount that was (or would have been, if simulated) drained. Never
     *         null; {@link FluidStack#EMPTY} if nothing was drained. */
    FluidStack drain(IFluidFilter filter, int maxDrain, FluidAction action);
}
