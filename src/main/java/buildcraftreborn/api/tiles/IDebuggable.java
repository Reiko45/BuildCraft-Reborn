/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.tiles;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;

public interface IDebuggable {
    /** Get the debug information from a tile entity as a list of strings, used for the F3 debug menu. The left and
     * right parameters correspond to the sides of the F3 screen.
     *
     * @param side The side the block was clicked on, may be null if we don't know, or is the "centre" side */
    void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side);

    /** Same as {@link #getDebugInfo(List, List, Direction)}, but only called on the client. */
    default void getClientDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {}
}
