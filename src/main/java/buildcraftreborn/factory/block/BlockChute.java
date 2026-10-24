/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.block;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import buildcraftreborn.api.properties.BuildCraftProperties;
import buildcraftreborn.factory.tile.TileChute;
import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.block.IBlockWithFacing;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** The chute. Its {@code connected_<side>} properties show a spout on each side with an inventory. 1.12 worked these
 * out when drawing ({@code getActualState}); here they are stored in the block state and updated when a neighbour
 * changes. */
public class BlockChute extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final Map<Direction, BooleanProperty> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    public BlockChute(Properties properties) {
        super(properties);
        BlockState state = defaultBlockState().setValue(getFacingProperty(), Direction.UP);
        for (BooleanProperty prop : CONNECTED_MAP.values()) {
            state = state.setValue(prop, false);
        }
        registerDefaultState(state);
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileChute(pos, state);
    }

    @Override
    protected void addProperties(StateDefinition.Builder<Block, BlockState> builder) {
        super.addProperties(builder);
        CONNECTED_MAP.values().forEach(builder::add);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : withConnections(state, context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor world,
        BlockPos pos, BlockPos neighbourPos) {
        if (world instanceof Level level) {
            state = state.setValue(CONNECTED_MAP.get(direction), isConnected(state, level, pos, direction));
        }
        return super.updateShape(state, direction, neighbour, world, pos, neighbourPos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos,
        boolean isMoving) {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
        // A block entity appearing or disappearing next to the chute doesn't always change the block's shape, so
        // check again here as well.
        BlockState updated = withConnections(state, world, pos);
        if (updated != state) {
            world.setBlockAndUpdate(pos, updated);
        }
    }

    /** Facing changes (from the wrench) also change which side has the mouth. */
    public BlockState withConnections(BlockState state, Level world, BlockPos pos) {
        for (Direction side : Direction.values()) {
            state = state.setValue(CONNECTED_MAP.get(side), isConnected(state, world, pos, side));
        }
        return state;
    }

    private boolean isConnected(BlockState state, Level world, BlockPos pos, Direction side) {
        return side != state.getValue(getFacingProperty())
            && TileChute.hasInventoryAtPosition(world, pos.relative(side), side);
    }

    // IBlockWithFacing

    @Override
    public boolean canFaceVertically() {
        return true;
    }

    @Override
    public InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        InteractionResult result = IBlockWithFacing.super.attemptRotation(world, pos, state, sideWrenched);
        if (result.consumesAction()) {
            world.setBlockAndUpdate(pos, withConnections(world.getBlockState(pos), world, pos));
        }
        return result;
    }
}
