/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.block;

import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraftreborn.api.properties.BuildCraftProperties;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;

/** The quarry's frame: a thin bar that joins up with other frames and the quarry. It has no drops.
 * <p>
 * 1.21: the connections are block state properties kept up to date by {@link #updateShape} (1.12 worked them out
 * when drawing). */
public class BlockFrame extends BlockBCBase_Neptune {
    public static final Map<Direction, BooleanProperty> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    private static final VoxelShape BASE_SHAPE = Block.box(4, 4, 4, 12, 12, 12);
    private static final VoxelShape[] CONNECTION_SHAPES = {
        Block.box(4, 0, 4, 12, 4, 12), // down
        Block.box(4, 12, 4, 12, 16, 12), // up
        Block.box(4, 4, 0, 12, 12, 4), // north
        Block.box(4, 4, 12, 12, 12, 16), // south
        Block.box(0, 4, 4, 4, 12, 12), // west
        Block.box(12, 4, 4, 16, 12, 12), // east
    };
    private static final VoxelShape[] SHAPES = new VoxelShape[64];

    static {
        for (int mask = 0; mask < 64; mask++) {
            VoxelShape shape = BASE_SHAPE;
            for (Direction side : Direction.values()) {
                if ((mask & (1 << side.get3DDataValue())) != 0) {
                    shape = Shapes.or(shape, CONNECTION_SHAPES[side.get3DDataValue()]);
                }
            }
            SHAPES[mask] = shape;
        }
    }

    public BlockFrame(Properties properties) {
        super(properties);
        BlockState state = defaultBlockState();
        for (BooleanProperty prop : CONNECTED_MAP.values()) {
            state = state.setValue(prop, false);
        }
        registerDefaultState(state);
    }

    @Override
    protected void addProperties(StateDefinition.Builder<Block, BlockState> builder) {
        super.addProperties(builder);
        CONNECTED_MAP.values().forEach(builder::add);
    }

    public static boolean connectsTo(BlockState neighbour) {
        return neighbour.getBlock() instanceof BlockFrame || neighbour.getBlock() instanceof BlockQuarry;
    }

    /** @return The frame state with its connections set for the blocks around {@code pos}. */
    public BlockState withConnections(BlockState state, BlockGetter level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            state = state.setValue(CONNECTED_MAP.get(side), connectsTo(level.getBlockState(pos.relative(side))));
        }
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level,
        BlockPos pos, BlockPos neighbourPos) {
        return state.setValue(CONNECTED_MAP.get(direction), connectsTo(neighbour));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int mask = 0;
        for (Direction side : Direction.values()) {
            if (state.getValue(CONNECTED_MAP.get(side))) {
                mask |= 1 << side.get3DDataValue();
            }
        }
        return SHAPES[mask];
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
