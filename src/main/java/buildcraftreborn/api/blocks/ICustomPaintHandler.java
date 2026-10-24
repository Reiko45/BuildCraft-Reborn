/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.blocks;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Provides a way to paint blocks from any position. You can either implement this on a block, or register an instance
 * for a block with {@link CustomPaintHelper} */
public interface ICustomPaintHandler {
    /** Attempts to paint the given block. This can also only paint a specific part of the block (as the hit position is
     * given).
     *
     * @param level The level that the block is contained within.
     * @param pos The position of the block.
     * @param state The current state of the block.
     * @param hitPos The absolute hit position of the paintbrush.
     * @param hitSide The side of the block that was hit.
     * @param paintColour The paint colour to attempt to paint with, null if the paint should be cleared.
     * @return SUCCESS if the block changed, FAIL if it could have been handled but was already that colour, or PASS if
     *         this handler doesn't know the block. */
    InteractionResult attemptPaint(Level level, BlockPos pos, BlockState state, Vec3 hitPos,
        @Nullable Direction hitSide, @Nullable DyeColor paintColour);
}
