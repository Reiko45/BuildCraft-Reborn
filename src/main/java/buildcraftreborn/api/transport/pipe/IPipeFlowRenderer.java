/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pipe;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;

/** Draws a pipe flow in the world (items moving through the pipe, fluids, power). 1.21: called from the pipe's block
 * entity renderer with the pose stack already at the pipe's position; 1.12 passed the camera-relative position and a
 * buffer to draw into. */
public interface IPipeFlowRenderer<F extends PipeFlow> {
    void render(F flow, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLight,
        int combinedOverlay);
}
