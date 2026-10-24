/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.core;

import java.util.List;

import net.minecraft.core.BlockPos;

/** To be implemented by TileEntities able to provide a path on the world, typically BuildCraft path markers. */
public interface IPathProvider {
    /** @return The completed path. This should loop back onto itself (The last position is the same as the first
     *         position) if the path repeats. */
    List<BlockPos> getPath();

    /** Remove from the world all objects used to define the path. */
    void removeFromWorld();
}
