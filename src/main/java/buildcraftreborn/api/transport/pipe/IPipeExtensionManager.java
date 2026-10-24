/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraftreborn.api.transport.IStripesActivator;

public interface IPipeExtensionManager {

    /**
     * Requests an extension by one block from a IStripesActivator (usually a stripes transport pipe) with the pipe supplied
     * by the stack by moving the stripes pipe to the front and placing the new transport pipe behind.
     * If the pipe is a registered retraction pipe (per default only the void transport pipe is - register one with {@link #registerRetractionPipe(PipeDefinition)}) it retracts the pipeline instead by moving the stripes pipe one block
     * in the opposite direction, replacing the previous transport pipe.
     *
     * @param level   the level
     * @param pos     the position or origin of the request (usually the stripes pipe position)
     * @param dir     the direction of the proposed extension
     * @param stripes the stripes pipe
     * @param stack   the pipe stack to use (Note: only uses one item and sends the rest back)
     * @return true on success, false otherwise
     */
    boolean requestPipeExtension(Level level, BlockPos pos, Direction dir, IStripesActivator stripes, ItemStack stack);

    /**
     * Registers a pipe as a retraction trigger for pipe extension requests
     *
     * @param pipeDefinition the pipe definition
     */
    void registerRetractionPipe(PipeDefinition pipeDefinition);

}
