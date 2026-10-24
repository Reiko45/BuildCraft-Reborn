/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pipe;

import net.minecraft.core.Direction;

import net.neoforged.neoforge.energy.IEnergyStorage;

public interface IFlowRedstoneFlux extends IFlowPowerLike {
    /** Makes this pipe reconfigure itself, possibly due to the addition of new modules. */
    @Override
    void reconfigure();

    /** Attempts to extract power from the {@link IEnergyStorage} connected to this pipe on the given side.
     * 
     * @param maxPower The Maximum amount of power that can be extracted.
     * @param from The side (of this pipe) to take power from.
     * @return The amount of power extracted. */
    int tryExtractPower(int maxPower, Direction from);
}
