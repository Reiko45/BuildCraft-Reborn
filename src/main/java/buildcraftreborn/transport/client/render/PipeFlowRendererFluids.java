/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client.render;

import java.util.Arrays;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.transport.pipe.IPipeFlowRenderer;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.lib.client.render.fluid.FluidRenderer;
import buildcraftreborn.lib.client.render.fluid.FluidSpriteType;
import buildcraftreborn.lib.misc.VecUtil;
import buildcraftreborn.transport.pipe.Pipe;
import buildcraftreborn.transport.pipe.flow.PipeFlowFluids;

/** Draws the fluid in a pipe: a box per section, filled to the section's amount. */
public enum PipeFlowRendererFluids implements IPipeFlowRenderer<PipeFlowFluids> {
    INSTANCE;

    @Override
    public void render(PipeFlowFluids flow, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
        int combinedLight, int combinedOverlay) {
        FluidStack forRender = flow.getFluidStackForRender();
        if (forRender == null || forRender.isEmpty()) {
            return;
        }

        boolean[] sides = new boolean[6];
        Arrays.fill(sides, true);

        double[] amounts = flow.getAmountsForRender(partialTicks);
        Vec3[] offsets = flow.getOffsetsForRender(partialTicks);

        IPipeHolder holder = flow.pipe.getHolder();
        int light = FluidRenderer.getCombinedLight(holder.getPipeWorld(), holder.getPipePos(), forRender);
        VertexConsumer vc = buffers.getBuffer(RenderType.translucent());

        boolean gas = FluidRenderer.isGaseous(forRender);
        boolean horizontal = false;
        boolean vertical = flow.pipe.isConnected(gas ? Direction.DOWN : Direction.UP);

        for (Direction face : Direction.values()) {
            double size = ((Pipe) flow.pipe).getConnectedDist(face);
            double amount = amounts[face.ordinal()];
            if (face.getAxis() != Axis.Y) {
                horizontal |= flow.pipe.isConnected(face) && amount > 0;
            }

            Vec3 center = VecUtil.offset(new Vec3(0.5, 0.5, 0.5), face, 0.245 + size / 2);
            Vec3 radius = new Vec3(0.24, 0.24, 0.24);
            radius = VecUtil.replaceValue(radius, face.getAxis(), 0.005 + size / 2);

            if (face.getAxis() == Axis.Y) {
                double perc = Math.sqrt(amount / flow.capacity);
                radius = new Vec3(perc * 0.24, radius.y, perc * 0.24);
            }

            Vec3 offset = offsets[face.ordinal()];
            if (offset == null) offset = Vec3.ZERO;
            center = center.add(offset);

            poseStack.pushPose();
            poseStack.translate(-offset.x, -offset.y, -offset.z);
            Vec3 min = center.subtract(radius);
            Vec3 max = center.add(radius);
            if (face.getAxis() == Axis.Y) {
                FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, 1, 1, min, max, poseStack.last(), vc,
                    light, sides);
            } else {
                FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, amount, flow.capacity, min, max,
                    poseStack.last(), vc, light, sides);
            }
            poseStack.popPose();
        }

        double amount = amounts[EnumPipePart.CENTER.getIndex()];
        double horizPos = 0.26;

        Vec3 offset = offsets[EnumPipePart.CENTER.getIndex()];
        if (offset == null) offset = Vec3.ZERO;
        poseStack.pushPose();
        poseStack.translate(-offset.x, -offset.y, -offset.z);

        if (horizontal | !vertical) {
            Vec3 min = new Vec3(0.26, 0.26, 0.26).add(offset);
            Vec3 max = new Vec3(0.74, 0.74, 0.74).add(offset);
            FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, amount, flow.capacity, min, max,
                poseStack.last(), vc, light, sides);
            horizPos += (max.y - min.y) * amount / flow.capacity;
        }

        if (vertical && horizPos < 0.74) {
            double perc = Math.sqrt(amount / flow.capacity);
            double minXZ = 0.5 - 0.24 * perc;
            double maxXZ = 0.5 + 0.24 * perc;

            double yMin = gas ? 0.26 : horizPos;
            double yMax = gas ? 1 - horizPos : 0.74;

            Vec3 min = new Vec3(minXZ, yMin, minXZ).add(offset);
            Vec3 max = new Vec3(maxXZ, yMax, maxXZ).add(offset);
            FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, 1, 1, min, max, poseStack.last(), vc, light,
                sides);
        }
        poseStack.popPose();
    }
}
