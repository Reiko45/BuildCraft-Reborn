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

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.tile.TileMiningWell;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraftreborn.lib.client.render.tile.RenderPartCube;

/** The mining well's two LEDs (power and status) and its tube. */
public class RenderMiningWell implements BlockEntityRenderer<TileMiningWell> {
    private static final int[] COLOUR_POWER = new int[16];
    private static final int COLOUR_STATUS_ON = abgrToArgb(0xFF_77_DD_77); // a light green
    private static final int COLOUR_STATUS_OFF = abgrToArgb(0xFF_1f_10_1b); // black-ish

    private static final int BLOCK_LIGHT_STATUS_ON = 0xF;
    private static final int BLOCK_LIGHT_STATUS_OFF = 0x0;

    private static final double POWER = 2.5 / 16.0;
    private static final double STATUS = 4.5 / 16.0;
    private static final double Y = 5.5 / 16.0;

    private static final LaserType TUBE_LASER = RenderTube.createTubeLaser(BuildCraftReborn.id("block/mining_well/tube"));

    static {
        for (int i = 0; i < COLOUR_POWER.length; i++) {
            int c = ((i * 0x40) / COLOUR_POWER.length) & 0xFF;
            int r = (((i * 0xB0) / COLOUR_POWER.length) & 0xFF) + 0x4F;
            int colour = (0xFF << 24) | (c << 16) | (c << 8) | r;
            COLOUR_POWER[i] = abgrToArgb(colour);
        }
    }

    /** 1.12 vertex colours kept red in the lowest byte; 1.21 vertex colours are ARGB. */
    static int abgrToArgb(int abgr) {
        int r = abgr & 0xFF;
        int b = (abgr >> 16) & 0xFF;
        return (abgr & 0xFF00FF00) | (r << 16) | b;
    }

    private final RenderPartCube ledPower = new RenderPartCube();
    private final RenderPartCube ledStatus = new RenderPartCube();
    private final RenderTube tubeRenderer = new RenderTube(TUBE_LASER);

    public RenderMiningWell(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(TileMiningWell tile, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
        int packedLight, int packedOverlay) {
        Direction facing = Direction.NORTH;
        BlockState state = tile.getLevel().getBlockState(tile.getBlockPos());
        if (state.getBlock() == BCFactoryBlocks.MINING_WELL.get()) {
            facing = state.getValue(BlockBCBase_Neptune.PROP_FACING);
        }

        final int dX, dZ;
        final double ledX, ledZ;
        if (facing.getAxis() == Axis.X) {
            dX = 0;
            dZ = facing.getAxisDirection().getStep();
            ledZ = 0.5;
            if (facing == Direction.EAST) {
                ledX = 15.8 / 16.0;
            } else {
                ledX = 0.2 / 16.0;
            }
        } else {
            dX = -facing.getAxisDirection().getStep();
            dZ = 0;
            ledX = 0.5;
            if (facing == Direction.SOUTH) {
                ledZ = 15.8 / 16.0;
            } else {
                ledZ = 0.2 / 16.0;
            }
        }

        int combinedLight = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().relative(facing));
        VertexConsumer buffer = buffers.getBuffer(RenderType.cutout());

        ledPower.light = combinedLight;
        ledStatus.light = combinedLight;

        ledPower.position(ledX + dX * POWER, Y, ledZ + dZ * POWER);
        float percentFilled = tile.getPercentFilledForRender();
        int colourIndex = (int) (percentFilled * (COLOUR_POWER.length - 1));
        ledPower.colour = COLOUR_POWER[colourIndex];
        ledPower.maxLight(percentFilled > 0.01 ? BLOCK_LIGHT_STATUS_ON : BLOCK_LIGHT_STATUS_OFF, 0);
        ledPower.render(poseStack.last(), buffer);

        ledStatus.position(ledX + dX * STATUS, Y, ledZ + dZ * STATUS);
        boolean complete = tile.isComplete();
        ledStatus.colour = complete ? COLOUR_STATUS_OFF : COLOUR_STATUS_ON;
        ledStatus.maxLight(complete ? BLOCK_LIGHT_STATUS_OFF : BLOCK_LIGHT_STATUS_ON, 0);
        ledStatus.render(poseStack.last(), buffer);

        tubeRenderer.render(tile, partialTicks, poseStack, buffer);
    }

    @Override
    public boolean shouldRenderOffScreen(TileMiningWell tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(TileMiningWell tile) {
        return AABB.INFINITE;
    }
}
