/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.block.IBlockWithFacing;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.silicon.tile.TileLaser;

/** The laser: fires power at the laser tables in front of it. */
public class BlockLaser extends BlockBCTile_Neptune implements IBlockWithFacing {
    public BlockLaser(Properties properties) {
        super(properties);
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileLaser(pos, state);
    }

    @Override
    public boolean canFaceVertically() {
        return true;
    }
}
