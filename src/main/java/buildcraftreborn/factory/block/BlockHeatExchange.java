/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.block;

import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import buildcraftreborn.factory.tile.TileHeatExchange;
import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.block.IBlockWithFacing;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** The heat exchanger. Its block state shows which part of a heat exchanger this block is (set by the
 * {@link TileHeatExchange} when the exchanger forms or breaks up) and whether the exchangers on either side face the
 * same way, so the block model can join them up. 1.12 worked all of this out when drawing ({@code getActualState}).
 * <p>
 * Wrenching a lone exchanger turns it 90 degrees; wrenching one that is part of a larger exchanger turns the whole
 * exchanger round, which swaps the start and end. */
public class BlockHeatExchange extends BlockBCTile_Neptune implements IBlockWithFacing {

    public enum EnumExchangePart implements StringRepresentable {
        START,
        MIDDLE,
        END;

        private final String lowerCaseName = name().toLowerCase(Locale.ROOT);

        @Override
        public String getSerializedName() {
            return lowerCaseName;
        }
    }

    public static final EnumProperty<EnumExchangePart> PROP_PART = EnumProperty.create("part", EnumExchangePart.class);
    /** The exchanger on the left (looking at the front) faces the same way. */
    public static final BooleanProperty PROP_CONNECTED_LEFT = BooleanProperty.create("connected_left");
    /** The exchanger on the right (looking at the front) faces the same way. */
    public static final BooleanProperty PROP_CONNECTED_RIGHT = BooleanProperty.create("connected_right");

    public BlockHeatExchange(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
            .setValue(PROP_PART, EnumExchangePart.MIDDLE)
            .setValue(PROP_CONNECTED_LEFT, false)
            .setValue(PROP_CONNECTED_RIGHT, false));
    }

    @Override
    protected void addProperties(StateDefinition.Builder<Block, BlockState> builder) {
        super.addProperties(builder);
        builder.add(PROP_PART, PROP_CONNECTED_LEFT, PROP_CONNECTED_RIGHT);
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileHeatExchange(pos, state);
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
        if (direction.getAxis() != Direction.Axis.Y) {
            state = withConnections(state, world, pos);
        }
        return super.updateShape(state, direction, neighbour, world, pos, neighbourPos);
    }

    public static BlockState withConnections(BlockState state, BlockGetter world, BlockPos pos) {
        Direction thisFacing = state.getValue(PROP_FACING);
        return state
            .setValue(PROP_CONNECTED_LEFT, doesNeighbourConnect(world, pos, thisFacing, thisFacing.getClockWise()))
            .setValue(PROP_CONNECTED_RIGHT,
                doesNeighbourConnect(world, pos, thisFacing, thisFacing.getCounterClockWise()));
    }

    private static boolean doesNeighbourConnect(BlockGetter world, BlockPos pos, Direction thisFacing,
        Direction dir) {
        BlockState neighbour = world.getBlockState(pos.relative(dir));
        if (neighbour.getBlock() instanceof BlockHeatExchange) {
            return neighbour.getValue(PROP_FACING) == thisFacing;
        }
        return false;
    }

    @Override
    public InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileHeatExchange exchange) {
            return exchange.rotate() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        return InteractionResult.FAIL;
    }
}
