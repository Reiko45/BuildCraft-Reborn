/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.factory.tile.TilePump;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraftreborn.lib.client.render.tile.RenderPartCube;

/** The pump's LEDs (power and status, on each of its four sides) and its tube. */
public class RenderPump implements BlockEntityRenderer<TilePump> {
    private static final int[] COLOUR_POWER = new int[16];
    private static final int COLOUR_STATUS_ON = RenderMiningWell.abgrToArgb(0xFF_77_DD_77); // a light green
    private static final int COLOUR_STATUS_OFF = RenderMiningWell.abgrToArgb(0xFF_1f_10_1b); // black-ish

    private static final int BLOCK_LIGHT_STATUS_ON = 0xF;
    private static final int BLOCK_LIGHT_STATUS_OFF = 0x0;

    private static final double POWER = 1.5 / 16.0;
    private static final double STATUS = 3.5 / 16.0;
    private static final double Y = 13.5 / 16.0;

    private static final LaserType TUBE_LASER = RenderTube.createTubeLaser(BuildCraftReborn.id("block/pump/tube"));

    static {
        for (int i = 0; i < COLOUR_POWER.length; i++) {
            int c = (i * 0x40) / COLOUR_POWER.length;
            int r = (i * 0xE0) / COLOUR_POWER.length + 0x1F;
            int colour = (0xFF << 24) + (c << 16) + (c << 8) + r;
            COLOUR_POWER[i] = RenderMiningWell.abgrToArgb(colour);
        }
    }

    private final RenderPartCube[] ledPower = new RenderPartCube[4];
    private final RenderPartCube[] ledStatus = new RenderPartCube[4];
    private final RenderTube tubeRenderer = new RenderTube(TUBE_LASER);

    public RenderPump(BlockEntityRendererProvider.Context context) {
        for (int i = 0; i < 4; i++) {
            Direction facing = Direction.from2DDataValue(i);
            final int dX, dZ;
            final double ledX, ledZ;
            if (facing.getAxis() == Axis.X) {
                dX = 0;
                dZ = facing.getAxisDirection().getStep();
                ledZ = 0.5;
                if (facing == Direction.EAST) {
                    ledX = 15.6 / 16.0;
                } else {
                    ledX = 0.4 / 16.0;
                }
            } else {
                dX = -facing.getAxisDirection().getStep();
                dZ = 0;
                ledX = 0.5;
                if (facing == Direction.SOUTH) {
                    ledZ = 15.6 / 16.0;
                } else {
                    ledZ = 0.4 / 16.0;
                }
            }
            ledPower[i] = new RenderPartCube(ledX + dX * POWER, Y, ledZ + dZ * POWER);
            ledStatus[i] = new RenderPartCube(ledX + dX * STATUS, Y, ledZ + dZ * STATUS);
        }
    }

    @Override
    public void render(TilePump tile, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
        int packedLight, int packedOverlay) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.cutout());
        float percentFilled = tile.getPercentFilledForRender();
        int powerColour = COLOUR_POWER[(int) (percentFilled * (COLOUR_POWER.length - 1))];

        boolean complete = tile.isComplete();
        int statusColour = complete ? COLOUR_STATUS_OFF : COLOUR_STATUS_ON;
        int statusLight = complete ? BLOCK_LIGHT_STATUS_OFF : BLOCK_LIGHT_STATUS_ON;

        for (int i = 0; i < 4; i++) {
            // Get the light level of a direction
            Direction dir = Direction.from2DDataValue(i);
            BlockPos pos = tile.getBlockPos().relative(dir);
            int block = tile.getLevel().getBrightness(LightLayer.BLOCK, pos);
            int sky = tile.getLevel().getBrightness(LightLayer.SKY, pos);

            ledPower[i].colour = powerColour;
            ledStatus[i].colour = statusColour;

            ledPower[i].light(block, sky);
            ledStatus[i].light(Math.max(statusLight, block), sky);

            ledPower[i].render(poseStack.last(), buffer);
            ledStatus[i].render(poseStack.last(), buffer);
        }

        tubeRenderer.render(tile, partialTicks, poseStack, buffer);
    }

    @Override
    public boolean shouldRenderOffScreen(TilePump tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(TilePump tile) {
        return AABB.INFINITE;
    }
}
