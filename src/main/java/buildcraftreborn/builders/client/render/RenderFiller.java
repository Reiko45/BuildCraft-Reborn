/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.builders.snapshot.TemplateBuilder;
import buildcraftreborn.builders.tile.TileFiller;
import buildcraftreborn.core.client.BuildCraftLaserManager;
import buildcraftreborn.lib.client.render.laser.LaserBoxRenderer;

/** Draws what the filler is doing, and the box of the markers it took its area from. */
public class RenderFiller implements BlockEntityRenderer<TileFiller> {
    public RenderFiller(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(TileFiller tile, float partialTicks, PoseStack pose, MultiBufferSource buffers,
        int combinedLight, int combinedOverlay) {
        TemplateBuilder builder = tile.getBuilder();
        if (builder != null) {
            RenderSnapshotBuilder.render(builder, tile.getLevel(), tile.getBlockPos(), partialTicks, pose, buffers);
        }
        if (tile.markerBox && tile.box.isInitialized()) {
            LaserBoxRenderer.renderLaserBox(tile.box, BuildCraftLaserManager.STRIPES_WRITE, true, pose,
                buffers.getBuffer(RenderType.cutout()), Vec3.atLowerCornerOf(tile.getBlockPos()));
        }
    }

    @Override
    public boolean shouldRenderOffScreen(TileFiller tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(TileFiller tile) {
        AABB box = new AABB(tile.getBlockPos());
        return tile.hasBox() ? box.minmax(new AABB(Vec3.atLowerCornerOf(tile.getBox().min()),
            Vec3.atLowerCornerOf(tile.getBox().max()).add(1, 1, 1))).inflate(2) : box;
    }
}
