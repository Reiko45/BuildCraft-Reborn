/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import buildcraftreborn.api.blocks.ICustomRotationHandler;

/** Marker interface used by {@link BlockBCBase_Neptune} to automatically add a facing property to blocks.
 * {@link #getFacingProperty()} must not depend on instance fields: it is called from the block's constructor. */
public interface IBlockWithFacing extends ICustomRotationHandler {
    default boolean canFaceVertically() {
        return false;
    }

    default DirectionProperty getFacingProperty() {
        return canFaceVertically() ? BlockBCBase_Neptune.BLOCK_FACING_6 : BlockBCBase_Neptune.PROP_FACING;
    }

    default boolean canBeRotated(Level world, BlockPos pos, BlockState state) {
        return true;
    }

    /** Rotates the block one step, as the wrench does. */
    @Override
    default InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (!canBeRotated(world, pos, state)) {
            return InteractionResult.FAIL;
        }
        Direction currentFacing = state.getValue(getFacingProperty());
        Direction newFacing = canFaceVertically() ? Direction.from3DDataValue(currentFacing.get3DDataValue() + 1)
            : currentFacing.getClockWise();
        world.setBlockAndUpdate(pos, state.setValue(getFacingProperty(), newFacing));
        return InteractionResult.SUCCESS;
    }
}
