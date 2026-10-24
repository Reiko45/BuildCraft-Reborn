/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import buildcraftreborn.api.transport.pipe.IPipeBehaviourRenderer;
import buildcraftreborn.api.transport.pipe.IPipeFlowRenderer;
import buildcraftreborn.api.transport.pipe.PipeBehaviour;
import buildcraftreborn.api.transport.pipe.PipeFlow;
import buildcraftreborn.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.transport.client.PipeRegistryClient;
import buildcraftreborn.transport.pipe.Pipe;
import buildcraftreborn.transport.tile.TilePipeHolder;

/** Draws the moving parts of a pipe: wires, dynamic pluggables (gate lights, the pulsar), the flow (items, fluids,
 * power) and the behaviour (the stripes pipe's head). The rest is the block model. */
public class RenderPipeHolder implements BlockEntityRenderer<TilePipeHolder> {
    public RenderPipeHolder(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(TilePipeHolder pipe, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
        int combinedLight, int combinedOverlay) {
        PipeWireRenderer.renderWires(pipe, poseStack, buffers, combinedLight);

        for (Direction face : Direction.values()) {
            PipePluggable plug = pipe.getPluggable(face);
            if (plug != null) {
                renderPlug(plug, partialTicks, poseStack, buffers, combinedLight, combinedOverlay);
            }
        }

        Pipe p = pipe.getPipe();
        if (p == null) {
            return;
        }
        if (p.flow != null) {
            renderFlow(p.flow, partialTicks, poseStack, buffers, combinedLight, combinedOverlay);
        }
        if (p.behaviour != null) {
            renderBehaviour(p.behaviour, partialTicks, poseStack, buffers, combinedLight, combinedOverlay);
        }
    }

    private static <P extends PipePluggable> void renderPlug(P plug, float partialTicks, PoseStack poseStack,
        MultiBufferSource buffers, int light, int overlay) {
        IPlugDynamicRenderer<P> renderer = PipeRegistryClient.getPlugRenderer(plug);
        if (renderer != null) {
            renderer.render(plug, partialTicks, poseStack, buffers, light, overlay);
        }
    }

    private static <F extends PipeFlow> void renderFlow(F flow, float partialTicks, PoseStack poseStack,
        MultiBufferSource buffers, int light, int overlay) {
        IPipeFlowRenderer<F> renderer = PipeRegistryClient.getFlowRenderer(flow);
        if (renderer != null) {
            renderer.render(flow, partialTicks, poseStack, buffers, light, overlay);
        }
    }

    private static <B extends PipeBehaviour> void renderBehaviour(B behaviour, float partialTicks,
        PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        IPipeBehaviourRenderer<B> renderer = PipeRegistryClient.getBehaviourRenderer(behaviour);
        if (renderer != null) {
            renderer.render(behaviour, partialTicks, poseStack, buffers, light, overlay);
        }
    }

    /** Items can stick out of the pipe a little, and the stripes head reaches into the next block. */
    @Override
    public AABB getRenderBoundingBox(TilePipeHolder pipe) {
        return new AABB(pipe.getBlockPos()).inflate(1);
    }
}
