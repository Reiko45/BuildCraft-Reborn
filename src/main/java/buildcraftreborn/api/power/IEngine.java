/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.power;

import net.minecraft.core.Direction;

/** Engines should implement this interface if they want to support BuildCraft's behaviour of passing power between
 * engines without using receivePower() (which has other issues). */
public interface IEngine {
    /** Returns true if the engine wants to receive power from another engine on this side. */
    boolean canReceiveFromEngine(Direction side);

    /** Receives power from an engine.
     *
     * @param microJoules The number of micro joules to add.
     * @param simulate If true then just pretend you received power- don't actually change any of your internal state.
     * @return True if all the power was accepted, false if not.
     * @see buildcraftreborn.api.mj.IMjReceiver#receivePower(long, boolean) */
    boolean receivePower(long microJoules, boolean simulate);
}
