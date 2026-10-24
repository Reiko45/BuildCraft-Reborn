/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** A question about a block in the world ("is it soft?", "is it wood?") that robots ask a lot, so the answer may be
 * cached. */
public interface IWorldProperty {
    boolean get(Level world, BlockPos pos);

    void clear();
}
