/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.core.tile.TileMarkerVolume;
import buildcraftreborn.lib.block.BlockMarkerBase;
import buildcraftreborn.lib.tile.TileBC_Neptune;

public class BlockMarkerVolume extends BlockMarkerBase {
    public BlockMarkerVolume(Properties properties) {
        super(properties);
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileMarkerVolume(pos, state);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos,
        boolean isMoving) {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
        checkSignalState(world, pos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        checkSignalState(world, pos);
    }

    private static void checkSignalState(Level world, BlockPos pos) {
        if (world.isClientSide) {
            return;
        }
        if (world.getBlockEntity(pos) instanceof TileMarkerVolume volume) {
            boolean powered = world.hasNeighborSignal(pos);
            if (volume.isShowingSignals() != powered) {
                volume.switchSignals();
            }
        }
    }
}
