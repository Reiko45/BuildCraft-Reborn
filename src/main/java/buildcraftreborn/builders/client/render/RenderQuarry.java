/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.builders.tile.TileQuarry;
import buildcraftreborn.core.client.BuildCraftLaserManager;
import buildcraftreborn.lib.client.render.laser.LaserBoxRenderer;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8.LaserRow;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraftreborn.lib.client.render.laser.LaserRenderer_BC8;
import buildcraftreborn.lib.misc.VecUtil;

/** Draws the quarry's moving frame and drill (as lasers made of frame and drill textures), the laser it uses while
 * it builds its frame, and the planned frame before it starts. */
public class RenderQuarry implements BlockEntityRenderer<TileQuarry> {
    private static final ResourceLocation FRAME_SPRITE = BuildCraftReborn.id("block/frame/default");
    private static final ResourceLocation DRILL_SPRITE = BuildCraftReborn.id("block/quarry/drill");

    public static final LaserType FRAME = new LaserType(new LaserRow(FRAME_SPRITE, 0, 0, 0, 0), null,
        new LaserRow[] { new LaserRow(FRAME_SPRITE, 0, 4, 16, 12) }, new LaserRow(FRAME_SPRITE, 0, 4, 16, 12),
        new LaserRow(FRAME_SPRITE, 0, 0, 0, 0));
    public static final LaserType FRAME_BOTTOM = new LaserType(new LaserRow(FRAME_SPRITE, 0, 0, 0, 0), null,
        new LaserRow[] { new LaserRow(FRAME_SPRITE, 0, 4, 16, 12) }, new LaserRow(FRAME_SPRITE, 0, 4, 16, 12),
        new LaserRow(FRAME_SPRITE, 4, 4, 12, 12));
    public static final LaserType DRILL = new LaserType(new LaserRow(DRILL_SPRITE, 6, 0, 10, 4), null,
        new LaserRow[] { new LaserRow(DRILL_SPRITE, 0, 0, 16, 4) }, null, new LaserRow(DRILL_SPRITE, 6, 0, 10, 4));

    public RenderQuarry(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(TileQuarry tile, float partialTicks, PoseStack pose, MultiBufferSource buffers,
        int combinedLight, int combinedOverlay) {
        if (!tile.frameBox.isInitialized()) {
            return;
        }
        VertexConsumer buffer = buffers.getBuffer(RenderType.cutout());
        // The pose is at the quarry, so it is the "camera" for the laser renderer.
        Vec3 origin = Vec3.atLowerCornerOf(tile.getBlockPos());
        BlockPos min = tile.frameBox.min();
        BlockPos max = tile.frameBox.max();
        double yOffset = 1 + 4 / 16D;

        if (tile.currentTask instanceof TileQuarry.TaskBreakBlock taskBreakBlock) {
            BlockPos pos = taskBreakBlock.breakPos;
            if (tile.drillPos == null) {
                if (taskBreakBlock.clientPower != 0) {
                    // Don't render a laser before we have any power
                    LaserData_BC8 laser = new LaserData_BC8(BuildCraftLaserManager.POWER_LOW,
                        VecUtil.convertCenter(tile.getBlockPos()), VecUtil.convertCenter(pos), 1 / 16.0);
                    LaserRenderer_BC8.renderLaser(laser, pose, buffer, origin);
                }
            } else {
                long power = (long) (taskBreakBlock.prevClientPower
                    + (taskBreakBlock.clientPower - taskBreakBlock.prevClientPower) * (double) partialTicks);
                VoxelShape shape = tile.getLevel().getBlockState(pos).getShape(tile.getLevel(), pos);
                AABB aabb = shape.isEmpty() ? new AABB(0, 0, 0, 1, 1, 1) : shape.bounds();
                double value = (double) power / Math.max(1, taskBreakBlock.getTarget());
                if (value < 0.9) {
                    value = 1 - value / 0.9;
                } else {
                    value = (value - 0.9) / 0.1;
                }
                double scaleMin = 1 - (1 - aabb.maxY) - (aabb.maxY - aabb.minY) / 2;
                double scaleMax = 1 + 4 / 16D;
                yOffset = scaleMin + value * (scaleMax - scaleMin);
            }
        }

        if (tile.clientDrillPos != null && tile.prevClientDrillPos != null) {
            Vec3 p = tile.prevClientDrillPos.add(tile.clientDrillPos.subtract(tile.prevClientDrillPos)
                .scale(partialTicks));
            double top = max.getY() + 0.5;
            laser(FRAME, new Vec3(p.x + 0.5, top, p.z), new Vec3(p.x + 0.5, top, max.getZ() + 12 / 16D), pose,
                buffer, origin);
            laser(FRAME, new Vec3(p.x + 0.5, top, p.z), new Vec3(p.x + 0.5, top, min.getZ() + 4 / 16D), pose,
                buffer, origin);
            laser(FRAME, new Vec3(p.x, top, p.z + 0.5), new Vec3(max.getX() + 12 / 16D, top, p.z + 0.5), pose,
                buffer, origin);
            laser(FRAME, new Vec3(p.x, top, p.z + 0.5), new Vec3(min.getX() + 4 / 16D, top, p.z + 0.5), pose,
                buffer, origin);
            laser(FRAME_BOTTOM, new Vec3(p.x + 0.5, p.y + 1 + 4 / 16D, p.z + 0.5), new Vec3(p.x + 0.5, top, p.z + 0.5),
                pose, buffer, origin);
            laser(DRILL, new Vec3(p.x + 0.5, p.y + 1 + yOffset, p.z + 0.5), new Vec3(p.x + 0.5, p.y + yOffset,
                p.z + 0.5), pose, buffer, origin);
        } else {
            LaserBoxRenderer.renderLaserBox(tile.frameBox, BuildCraftLaserManager.STRIPES_WRITE, true, pose, buffer,
                origin);
        }
    }

    private static void laser(LaserType type, Vec3 from, Vec3 to, PoseStack pose, VertexConsumer buffer,
        Vec3 origin) {
        if (from.distanceToSqr(to) < 1e-6) {
            return;
        }
        LaserRenderer_BC8.renderLaser(new LaserData_BC8(type, from, to, 1 / 16D, true, true, 0), pose, buffer,
            origin);
    }

    @Override
    public boolean shouldRenderOffScreen(TileQuarry tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(TileQuarry tile) {
        AABB box = new AABB(tile.getBlockPos());
        if (tile.frameBox.isInitialized()) {
            box = box.minmax(tile.frameBox.getBoundingBox());
        }
        if (tile.miningBox.isInitialized()) {
            box = box.minmax(tile.miningBox.getBoundingBox());
        }
        return box;
    }
}
