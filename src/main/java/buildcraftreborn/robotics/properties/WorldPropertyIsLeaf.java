/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.properties;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/** 1.21: the leaves block tag (1.12: the "treeLeaves" ore dictionary name). */
public class WorldPropertyIsLeaf extends WorldProperty {
    @Override
    protected boolean get(BlockGetter blockAccess, BlockState state, BlockPos pos) {
        return state.is(BlockTags.LEAVES);
    }
}
