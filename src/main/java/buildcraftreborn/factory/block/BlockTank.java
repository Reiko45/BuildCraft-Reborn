/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraftreborn.api.properties.BuildCraftProperties;
import buildcraftreborn.factory.tile.TileTank;
import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** The tank. 1.12 worked out {@code joined_below} when the block was drawn ({@code getActualState}); it is stored in
 * the block state here and updated when the block below changes. */
public class BlockTank extends BlockBCTile_Neptune implements ITankBlockConnector {
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public BlockTank(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BuildCraftProperties.JOINED_BELOW, false));
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileTank(pos, state);
    }

    @Override
    protected void addProperties(StateDefinition.Builder<Block, BlockState> builder) {
        super.addProperties(builder);
        builder.add(BuildCraftProperties.JOINED_BELOW);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
        return side.getAxis() == Direction.Axis.Y && adjacent.getBlock() instanceof ITankBlockConnector;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        return state.setValue(BuildCraftProperties.JOINED_BELOW, below.getBlock() instanceof ITankBlockConnector);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor world,
        BlockPos pos, BlockPos neighbourPos) {
        if (direction == Direction.DOWN) {
            state = state.setValue(BuildCraftProperties.JOINED_BELOW,
                neighbour.getBlock() instanceof ITankBlockConnector);
        }
        return super.updateShape(state, direction, neighbour, world, pos, neighbourPos);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof TileTank tank) {
            return tank.getComparatorLevel();
        }
        return 0;
    }
}
