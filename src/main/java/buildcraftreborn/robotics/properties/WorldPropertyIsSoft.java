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
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.api.core.BuildCraftAPI;

/** Blocks a robot can move through and builders can build over: air, replaceable blocks, fluids, plants, snow, vines
 * and fire, and any block added to {@link BuildCraftAPI#softBlocks}. */
public class WorldPropertyIsSoft extends WorldProperty {
    @Override
    protected boolean get(BlockGetter blockAccess, BlockState state, BlockPos pos) {
        return isSoft(state);
    }

    public static boolean isSoft(BlockState state) {
        if (state.isAir() || state.canBeReplaced()) {
            return true;
        }
        var block = state.getBlock();
        return block instanceof LiquidBlock || block instanceof BushBlock || block instanceof SugarCaneBlock
            || block instanceof CactusBlock || block instanceof SnowLayerBlock || block instanceof VineBlock
            || block instanceof BaseFireBlock || BuildCraftAPI.softBlocks.contains(block);
    }
}
