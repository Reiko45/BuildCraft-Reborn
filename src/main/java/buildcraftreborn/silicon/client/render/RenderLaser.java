/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.core.client.BuildCraftLaserManager;
import buildcraftreborn.lib.BCLibConfig;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8;
import buildcraftreborn.lib.client.render.laser.LaserRenderer_BC8;
import buildcraftreborn.robotics.client.RobotGogglesClient;
import buildcraftreborn.silicon.tile.TileLaser;

/** Draws the laser beam, coloured by how much power the laser is sending (red to blue). When beams are turned off in
 * the client config, a player wearing robot goggles still sees them (as with 1.12's development-only goggles). */
public class RenderLaser implements BlockEntityRenderer<TileLaser> {
    private static final int MAX_POWER = BuildCraftLaserManager.POWERS.length - 1;

    public RenderLaser(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(TileLaser tile, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
        int combinedLight, int combinedOverlay) {
        if (!(BCLibConfig.renderLaserBeams() || RobotGogglesClient.isWorn()) || tile.laserPos == null) {
            return;
        }
        long avg = tile.getAverageClient();
        if (avg <= 200_000) {
            return;
        }
        avg += 200_000;
        Direction side = tile.getBlockState().getValue(BlockBCBase_Neptune.BLOCK_FACING_6);
        Vec3 offset = new Vec3(0.5, 0.5, 0.5).add(Vec3.atLowerCornerOf(side.getNormal()).scale(4 / 16D));
        int index = (int) (avg * MAX_POWER / tile.getMaxPowerPerTick());
        if (index > MAX_POWER) {
            index = MAX_POWER;
        }
        Vec3 origin = Vec3.atLowerCornerOf(tile.getBlockPos());
        LaserData_BC8 laser = new LaserData_BC8(BuildCraftLaserManager.POWERS[index], origin.add(offset),
            tile.laserPos, 1 / 16D);
        // The pose is already at the laser's block, so pass that as the "camera"
        LaserRenderer_BC8.renderLaser(laser, poseStack, buffers.getBuffer(RenderType.cutout()), origin);
    }

    @Override
    public boolean shouldRenderOffScreen(TileLaser tile) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(TileLaser tile) {
        AABB box = new AABB(tile.getBlockPos());
        return tile.laserPos == null ? box : box.minmax(new AABB(tile.laserPos, tile.laserPos).inflate(0.5));
    }
}
