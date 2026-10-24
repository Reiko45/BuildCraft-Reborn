/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.properties;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.api.core.IWorldProperty;

/** A world property worked out from the block state.
 * <p>
 * 1.21: 7.1 cached each property for whole chunks and listened for block changes to keep the cache right. Block
 * lookups are cheap now, so the property is worked out each time. Blocks in chunks that aren't loaded never match
 * (as in 7.1), so a search never loads chunks. */
public abstract class WorldProperty implements IWorldProperty {

    @Override
    public boolean get(Level world, BlockPos pos) {
        if (!world.isLoaded(pos)) {
            return false;
        }
        return get(world, world.getBlockState(pos), pos);
    }

    @Override
    public void clear() {}

    protected abstract boolean get(BlockGetter blockAccess, BlockState state, BlockPos pos);
}
