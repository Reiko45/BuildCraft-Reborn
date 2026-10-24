/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;

import buildcraftreborn.api.transport.pipe.IPipeBehaviourRenderer;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.transport.client.BCTransportModels;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourStripes;

/** Draws the stripes pipe's head, facing the way it works. */
public enum PipeBehaviourRendererStripes implements IPipeBehaviourRenderer<PipeBehaviourStripes> {
    INSTANCE;

    @Override
    public void render(PipeBehaviourStripes stripes, float partialTicks, PoseStack poseStack,
        MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
        Direction dir = stripes.direction;
        if (dir == null) return;
        MutableQuad[] quads = BCTransportModels.getStripesDynQuads(dir);
        VertexConsumer vc = buffers.getBuffer(RenderType.cutout());
        MutableQuad copy = new MutableQuad();
        for (MutableQuad q : quads) {
            copy.copyFrom(q);
            copy.multShade();
            copy.render(poseStack.last(), vc, combinedLight, combinedOverlay);
        }
    }
}
