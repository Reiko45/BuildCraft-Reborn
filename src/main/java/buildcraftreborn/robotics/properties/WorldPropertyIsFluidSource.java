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
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

/** A source block of a fluid (waterlogged blocks don't count, as they can't be pumped). */
public class WorldPropertyIsFluidSource extends WorldProperty {
    @Override
    protected boolean get(BlockGetter blockAccess, BlockState state, BlockPos pos) {
        return state.getBlock() instanceof LiquidBlock && state.getFluidState().isSource();
    }
}
