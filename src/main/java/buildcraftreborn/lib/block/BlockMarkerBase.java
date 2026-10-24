/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.block;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraftreborn.api.blocks.ICustomRotationHandler;

/** A small torch-like marker that stands on the face of a block. 1.12 also had an {@code active} property for the
 * model, but both its values used the same model, so it is gone. */
public abstract class BlockMarkerBase extends BlockBCTile_Neptune implements ICustomRotationHandler {
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        double halfWidth = 0.1;
        double h = 0.65;
        // Little variables to make reading a *bit* more sane
        final double nw = 0.5 - halfWidth;
        final double pw = 0.5 + halfWidth;
        final double ih = 1 - h;
        SHAPES.put(Direction.DOWN, Shapes.box(nw, ih, nw, pw, 1, pw));
        SHAPES.put(Direction.UP, Shapes.box(nw, 0, nw, pw, h, pw));
        SHAPES.put(Direction.SOUTH, Shapes.box(nw, nw, 0, pw, pw, h));
        SHAPES.put(Direction.NORTH, Shapes.box(nw, nw, ih, pw, pw, 1));
        SHAPES.put(Direction.EAST, Shapes.box(0, nw, nw, h, pw, pw));
        SHAPES.put(Direction.WEST, Shapes.box(ih, nw, nw, 1, pw, pw));
    }

    /** 1.12: circuit material with hardness 0.25. */
    public static Properties markerProperties() {
        return Properties.of().strength(0.25F).noCollission().noOcclusion().sound(SoundType.STONE)
            .pushReaction(PushReaction.DESTROY);
    }

    public BlockMarkerBase(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BLOCK_FACING_6, Direction.UP));
    }

    @Override
    protected void addProperties(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BLOCK_FACING_6);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(BLOCK_FACING_6));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
        CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(BLOCK_FACING_6, context.getClickedFace());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction side = state.getValue(BLOCK_FACING_6);
        BlockPos on = pos.relative(side.getOpposite());
        return level.getBlockState(on).isFaceSturdy(level, on, side);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
        LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.getValue(BLOCK_FACING_6).getOpposite() && !state.canSurvive(level, pos)) {
            // Breaks (and drops) like 1.12's neighborChanged did
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(BLOCK_FACING_6, rot.rotate(state.getValue(BLOCK_FACING_6)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(BLOCK_FACING_6, mirror.mirror(state.getValue(BLOCK_FACING_6)));
    }

    @Override
    public InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof BlockMarkerBase) {// Just check to make sure we have the right block...
            return VanillaRotationHandlers.rotateEnumFacing(world, pos, state, BLOCK_FACING_6,
                VanillaRotationHandlers.ROTATE_FACING);
        } else {
            return InteractionResult.PASS;
        }
    }
}
