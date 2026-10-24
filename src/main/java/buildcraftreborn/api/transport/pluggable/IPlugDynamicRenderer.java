/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pluggable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;

/** Draws the moving parts of a pluggable (gate lights, a pulsar's piston). 1.21: called from the pipe's block entity
 * renderer with the pose stack at the pipe's position. */
public interface IPlugDynamicRenderer<P extends PipePluggable> {
    void render(P plug, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLight,
        int combinedOverlay);
}
