/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;

/** Base for BuildCraft blocks. Adds a facing property to blocks implementing {@link IBlockWithFacing}.
 * <p>
 * 1.21 notes: registry names, translation keys and creative tabs come from the {@code DeferredRegister} entry rather
 * than 1.12's {@code TagManager}, and metadata conversion is gone (block states are stored directly). */
public class BlockBCBase_Neptune extends Block {
    public static final DirectionProperty PROP_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final DirectionProperty BLOCK_FACING_6 = BlockStateProperties.FACING;

    /** Sensible default block properties (1.12: hardness 5, resistance 10, metal sound). */
    public static Properties defaultProperties() {
        return Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    public BlockBCBase_Neptune(Properties properties) {
        super(properties);
        if (this instanceof IBlockWithFacing withFacing) {
            registerDefaultState(defaultBlockState().setValue(withFacing.getFacingProperty(), Direction.NORTH));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        if (this instanceof IBlockWithFacing withFacing) {
            builder.add(withFacing.getFacingProperty());
        }
        addProperties(builder);
    }

    /** Add any extra block state properties here (called from the constructor, so don't use instance fields). */
    protected void addProperties(StateDefinition.Builder<Block, BlockState> builder) {}

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        if (this instanceof IBlockWithFacing withFacing) {
            DirectionProperty prop = withFacing.getFacingProperty();
            state = state.setValue(prop, rot.rotate(state.getValue(prop)));
        }
        return state;
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        if (this instanceof IBlockWithFacing withFacing) {
            DirectionProperty prop = withFacing.getFacingProperty();
            state = state.setValue(prop, mirror.mirror(state.getValue(prop)));
        }
        return state;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null && this instanceof IBlockWithFacing withFacing) {
            Direction orientation = context.getHorizontalDirection();
            LivingEntity placer = context.getPlayer();
            if (withFacing.canFaceVertically() && placer != null) {
                BlockPos pos = context.getClickedPos();
                if (Math.abs(placer.getX() - pos.getX()) < 2.0F && Math.abs(placer.getZ() - pos.getZ()) < 2.0F) {
                    double y = placer.getEyeY();
                    if (y - pos.getY() > 2.0D) {
                        orientation = Direction.DOWN;
                    }
                    if (pos.getY() - y > 0.0D) {
                        orientation = Direction.UP;
                    }
                }
            }
            state = state.setValue(withFacing.getFacingProperty(), orientation.getOpposite());
        }
        return state;
    }

    /** Convenience for subclasses reading an optional property. */
    protected static <T extends Comparable<T>> T getOr(BlockState state, Property<T> property, T fallback) {
        return state.hasProperty(property) ? state.getValue(property) : fallback;
    }
}
