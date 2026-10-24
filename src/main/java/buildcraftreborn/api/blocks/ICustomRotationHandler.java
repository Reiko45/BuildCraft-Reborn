/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ICustomRotationHandler {
    /** @return {@link InteractionResult#SUCCESS} if the block was rotated, {@link InteractionResult#FAIL} if it could
     *         have been but can't be right now, or {@link InteractionResult#PASS} if this handler doesn't apply. */
    InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched);
}
