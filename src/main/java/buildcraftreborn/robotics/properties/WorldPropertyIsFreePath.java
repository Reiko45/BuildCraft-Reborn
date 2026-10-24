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
import net.minecraft.world.level.block.state.BlockState;

/** Blocks a robot flies through when following a path: soft blocks, and blocks with nothing to bump into (torches,
 * rails, redstone and the like).
 * <p>
 * 1.21: 1.12's version of this property wasn't published, so this is written from what it is used for. */
public class WorldPropertyIsFreePath extends WorldProperty {
    @Override
    protected boolean get(BlockGetter blockAccess, BlockState state, BlockPos pos) {
        return WorldPropertyIsSoft.isSoft(state) || state.getCollisionShape(blockAccess, pos).isEmpty();
    }
}
