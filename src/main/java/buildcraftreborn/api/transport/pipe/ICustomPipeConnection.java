/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pipe;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ICustomPipeConnection {
    /** @return How long the connecting pipe should extend for, in addition to its normal 4/16f connection. Values less
     *         than or equal to <code>-4 / 16.0f</code> indicate that the pipe will not connect at all, and will render
     *         as it it was not connected. */
    float getExtension(Level level, BlockPos pos, Direction face, BlockState state);
}
