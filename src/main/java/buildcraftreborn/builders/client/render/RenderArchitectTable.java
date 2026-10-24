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

import buildcraftreborn.builders.tile.TileArchitectTable;
import buildcraftreborn.core.client.BuildCraftLaserManager;
import buildcraftreborn.lib.client.render.laser.LaserBoxRenderer;

/** Draws the box of the markers an architect table took its area from. */
public class RenderArchitectTable implements BlockEntityRenderer<TileArchitectTable> {
    public RenderArchitectTable(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(TileArchitectTable tile, float partialTicks, PoseStack pose, MultiBufferSource buffers,
        int combinedLight, int combinedOverlay) {
        if (!tile.markerBox || !tile.box.isInitialized()) {
            return;
        }
        LaserBoxRenderer.renderLaserBox(tile.box, BuildCraftLaserManager.STRIPES_READ, true, pose,
            buffers.getBuffer(RenderType.cutout()), Vec3.atLowerCornerOf(tile.getBlockPos()));
    }

    @Override
    public boolean shouldRenderOffScreen(TileArchitectTable tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(TileArchitectTable tile) {
        AABB box = new AABB(tile.getBlockPos());
        return tile.box.isInitialized() ? box.minmax(tile.box.getBoundingBox()) : box;
    }
}
