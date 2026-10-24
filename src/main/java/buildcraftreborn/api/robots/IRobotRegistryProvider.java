/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.robots;

import net.minecraft.world.level.Level;

public interface IRobotRegistryProvider {
    /** @return The registry of robots and stations for the level. On the client this is an empty registry. */
    IRobotRegistry getRegistry(Level world);
}
