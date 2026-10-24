/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.engine;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.api.blocks.ICustomRotationHandler;
import buildcraftreborn.api.core.IEngineType;
import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** An engine block. 1.12 had one block with the engine type as metadata; 1.21 has one block per engine type, each
 * with its own block entity type. The engine is drawn by its block entity renderer (it animates), so the block has
 * no static model. */
public class BlockEngineBase_BC8 extends BlockBCTile_Neptune implements ICustomRotationHandler {
    public final IEngineType engineType;
    private final Supplier<? extends BlockEntityType<? extends TileEngineBase_BC8>> tileType;

    public BlockEngineBase_BC8(Properties properties, IEngineType engineType,
        Supplier<? extends BlockEntityType<? extends TileEngineBase_BC8>> tileType) {
        super(properties.noOcclusion());
        this.engineType = engineType;
        this.tileType = tileType;
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return tileType.get().create(pos, state);
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
        if (level.getBlockEntity(pos) instanceof TileEngineBase_BC8 engine) {
            engine.rotateIfInvalid();
        }
    }

    // ICustomRotationHandler

    @Override
    public InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (world.getBlockEntity(pos) instanceof TileEngineBase_BC8 engine) {
            return engine.attemptRotation();
        }
        return InteractionResult.FAIL;
    }
}
