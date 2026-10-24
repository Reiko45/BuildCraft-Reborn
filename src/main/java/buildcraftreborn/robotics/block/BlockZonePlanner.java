/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.robotics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.block.IBlockWithFacing;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.robotics.tile.TileZonePlanner;

/** The zone planner. Its front shows a small map of the area around it. */
public class BlockZonePlanner extends BlockBCTile_Neptune implements IBlockWithFacing {
    public BlockZonePlanner(Properties properties) {
        super(properties);
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileZonePlanner(pos, state);
    }
}
