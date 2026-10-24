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

/** Draws a pipe behaviour in the world. See {@link IPipeFlowRenderer} for what changed in 1.21. */
public interface IPipeBehaviourRenderer<B extends PipeBehaviour> {
    void render(B behaviour, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLight,
        int combinedOverlay);
}
