/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.core;

import net.minecraft.core.BlockPos;

/** To be implemented by TileEntities able to provide a square area on the world, typically BuildCraft markers. */
public interface IAreaProvider {
    BlockPos min();

    BlockPos max();

    /** Remove from the world all objects used to define the area. */
    void removeFromWorld();
}
