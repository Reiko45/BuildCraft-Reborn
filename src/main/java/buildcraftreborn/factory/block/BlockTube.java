/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraftreborn.factory.tile.TileMiner;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;

/** The column below a mining well or pump. Unbreakable (its machine removes it), and creative players can't break it
 * while the machine is still above it. */
public class BlockTube extends BlockBCBase_Neptune {
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 16, 12);

    public BlockTube(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player,
        boolean willHarvest, FluidState fluid) {
        BlockPos currentPos = pos;
        // noinspection StatementWithEmptyBody
        while (world.getBlockState(currentPos = currentPos.above()).getBlock() == this) {
        }
        if (!(world.getBlockEntity(currentPos) instanceof TileMiner)) {
            return super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
        } else {
            return false;
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
