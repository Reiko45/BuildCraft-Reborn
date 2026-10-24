/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.api.blocks.ICustomRotationHandler;
import buildcraftreborn.energy.tile.TileDynamoMJ;
import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** The MJ dynamo block. Like an engine, it is drawn by its block entity renderer and turns to face a receiver. */
public class BlockDynamoMJ extends BlockBCTile_Neptune implements ICustomRotationHandler {
    public BlockDynamoMJ(Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileDynamoMJ(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
        boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide) return;
        if (level.getBlockEntity(pos) instanceof TileDynamoMJ dynamo) {
            dynamo.rotateIfInvalid();
        }
    }

    // ICustomRotationHandler

    @Override
    public InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (world.getBlockEntity(pos) instanceof TileDynamoMJ dynamo) {
            return dynamo.attemptRotation();
        }
        return InteractionResult.FAIL;
    }
}
