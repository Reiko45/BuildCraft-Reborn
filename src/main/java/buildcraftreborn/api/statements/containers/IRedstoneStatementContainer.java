/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.statements.containers;

import net.minecraft.core.Direction;

public interface IRedstoneStatementContainer {
    /** Get the redstone input from a given side.
     * 
     * @param side The side - use "null" for maximum input.
     * @return The redstone input, from 0 to 15. */
    int getRedstoneInput(Direction side);

    /** Set the redstone input for a given side.
     * 
     * @param side The side - use "null" for all sides.
     * @return Whether the set was successful. */
    boolean setRedstoneOutput(Direction side, int value);
}
