/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client.render;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.builders.snapshot.SnapshotBuilder;
import buildcraftreborn.builders.tile.TileBuilder;
import buildcraftreborn.core.client.BuildCraftLaserManager;
import buildcraftreborn.lib.client.render.laser.LaserBoxRenderer;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8;
import buildcraftreborn.lib.client.render.laser.LaserRenderer_BC8;
import buildcraftreborn.lib.misc.VecUtil;

/** Draws the box the builder is building in, its path, and what it is doing. */
public class RenderBuilder implements BlockEntityRenderer<TileBuilder> {
    private static final double OFFSET = 0.1;

    public RenderBuilder(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(TileBuilder tile, float partialTicks, PoseStack pose, MultiBufferSource buffers,
        int combinedLight, int combinedOverlay) {
        Vec3 origin = Vec3.atLowerCornerOf(tile.getBlockPos());
        VertexConsumer buffer = buffers.getBuffer(RenderType.cutout());
        if (tile.getBox().isInitialized()) {
            LaserBoxRenderer.renderLaserBox(tile.getBox(), BuildCraftLaserManager.STRIPES_WRITE, true, pose, buffer,
                origin);
        }
        List<BlockPos> path = tile.path;
        if (path != null) {
            BlockPos last = null;
            for (BlockPos p : path) {
                if (last != null) {
                    Vec3 from = Vec3.atLowerCornerOf(last).add(VecUtil.VEC_HALF);
                    Vec3 to = Vec3.atLowerCornerOf(p).add(VecUtil.VEC_HALF);
                    LaserData_BC8 data = new LaserData_BC8(BuildCraftLaserManager.STRIPES_WRITE_DIRECTION,
                        offset(from, to), offset(to, from), 1 / 16.1);
                    LaserRenderer_BC8.renderLaser(data, pose, buffer, origin);
                }
                last = p;
            }
        }
        SnapshotBuilder<?> builder = tile.getBuilder();
        if (builder != null) {
            RenderSnapshotBuilder.render(builder, tile.getLevel(), tile.getBlockPos(), partialTicks, pose, buffers);
        }
    }

    private static Vec3 offset(Vec3 from, Vec3 to) {
        Vec3 dir = to.subtract(from).normalize();
        return from.add(dir.scale(OFFSET));
    }

    @Override
    public boolean shouldRenderOffScreen(TileBuilder tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(TileBuilder tile) {
        AABB box = new AABB(tile.getBlockPos());
        if (tile.getBox().isInitialized()) {
            box = box.minmax(tile.getBox().getBoundingBox());
        }
        if (tile.path != null) {
            for (BlockPos p : tile.path) {
                box = box.minmax(new AABB(p));
            }
        }
        return box.inflate(2);
    }
}
