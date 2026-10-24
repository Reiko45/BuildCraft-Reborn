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

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.factory.tile.TileMiner;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraftreborn.lib.client.render.laser.LaserRenderer_BC8;

/** Draws the moving end of a miner's tube, from the machine down to where the tube currently reaches, as a laser. */
public class RenderTube {
    private final LaserType laserType;

    public RenderTube(LaserType laserType) {
        this.laserType = laserType;
    }

    /** @param poseStack The block entity renderer's pose (translated to the tile's block). */
    public void render(TileMiner tile, float partialTicks, PoseStack poseStack, VertexConsumer buffer) {
        if (tile.isComplete()) {
            return;
        }
        double tubeY = tile.getBlockPos().getY() - tile.getLength(partialTicks);
        BlockPos from = tile.getBlockPos();
        Vec3 start = new Vec3(from.getX() + 0.5, from.getY(), from.getZ() + 0.5);
        Vec3 end = new Vec3(from.getX() + 0.5, tubeY, from.getZ() + 0.5);
        LaserData_BC8 data = new LaserData_BC8(laserType, start, end, 1 / 16.0);
        // The pose is already at the block, so the block's corner stands in for the camera.
        LaserRenderer_BC8.renderLaser(data, poseStack, buffer, Vec3.atLowerCornerOf(from));
    }

    /** The tube laser used by the mining well and the pump, from the given 16x16 tube sprite. */
    public static LaserType createTubeLaser(net.minecraft.resources.ResourceLocation sprite) {
        LaserData_BC8.LaserRow cap = new LaserData_BC8.LaserRow(sprite, 0, 8, 8, 16);
        LaserData_BC8.LaserRow middle = new LaserData_BC8.LaserRow(sprite, 0, 0, 16, 8);
        LaserData_BC8.LaserRow[] middles = { middle };
        return new LaserType(cap, middle, middles, null, cap);
    }
}
