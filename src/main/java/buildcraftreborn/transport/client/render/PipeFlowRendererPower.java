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
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.transport.pipe.IPipeFlowRenderer;
import buildcraftreborn.transport.client.BCTransportClientSprites;
import buildcraftreborn.transport.pipe.flow.PipeFlowPower;
import buildcraftreborn.transport.pipe.flow.PipeFlowPower.Section;

/** Draws the power flowing through a kinesis pipe. */
public enum PipeFlowRendererPower implements IPipeFlowRenderer<PipeFlowPower> {
    INSTANCE;

    @Override
    public void render(PipeFlowPower flow, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
        int combinedLight, int combinedOverlay) {
        double[] power = new double[6];
        boolean[] connected = new boolean[6];
        double[] offsets = new double[6];
        for (Direction side : Direction.values()) {
            Section s = flow.getSection(side);
            int i = side.ordinal();
            power[i] = s.displayPower / (double) MjAPI.MJ;
            connected[i] = flow.pipe.isConnected(side);
            offsets[i] = PowerFlowRenderer.computeOffset(s.clientDisplayFlowLast, s.clientDisplayFlow, partialTicks);
        }
        Vec3 last = flow.clientDisplayFlowCentreLast;
        Vec3 now = flow.clientDisplayFlowCentre;
        Vec3 centre = new Vec3(PowerFlowRenderer.computeOffset(last.x, now.x, partialTicks),
            PowerFlowRenderer.computeOffset(last.y, now.y, partialTicks),
            PowerFlowRenderer.computeOffset(last.z, now.z, partialTicks));
        PowerFlowRenderer.render(power, connected, offsets, centre, BCTransportClientSprites.POWER_FLOW.getSprite(),
            poseStack, buffers);
    }
}
