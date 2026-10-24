/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.builders.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.builders.snapshot.SnapshotBuilder;
import buildcraftreborn.builders.tile.TileConstructionMarker;
import buildcraftreborn.core.client.BuildCraftLaserManager;
import buildcraftreborn.lib.client.render.laser.LaserBoxRenderer;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8;
import buildcraftreborn.lib.client.render.laser.LaserRenderer_BC8;

/** The construction marker: the box of its blueprint, a short laser showing which way the blueprint faces (7.1), and
 * the blocks being placed. */
public class RenderConstructionMarker implements BlockEntityRenderer<TileConstructionMarker> {
    public RenderConstructionMarker(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(TileConstructionMarker tile, float partialTicks, PoseStack pose, MultiBufferSource buffers,
        int combinedLight, int combinedOverlay) {
        Vec3 origin = Vec3.atLowerCornerOf(tile.getBlockPos());
        VertexConsumer buffer = buffers.getBuffer(RenderType.cutout());
        if (tile.box.isInitialized()) {
            LaserBoxRenderer.renderLaserBox(tile.box, BuildCraftLaserManager.STRIPES_WRITE, true, pose, buffer,
                origin);
        }
        Vec3 centre = Vec3.atCenterOf(tile.getBlockPos());
        Vec3 head = centre.add(Vec3.atLowerCornerOf(tile.getDirection().getNormal()).scale(0.5));
        LaserRenderer_BC8.renderLaser(new LaserData_BC8(BuildCraftLaserManager.STRIPES_WRITE_DIRECTION, centre, head,
            1 / 16D), pose, buffer, origin);
        SnapshotBuilder<?> builder = tile.getBuilder();
        if (builder != null) {
            RenderSnapshotBuilder.render(builder, tile.getLevel(), tile.getBlockPos(), partialTicks, pose, buffers);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(TileConstructionMarker tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(TileConstructionMarker tile) {
        AABB box = new AABB(tile.getBlockPos());
        if (tile.box.isInitialized()) {
            box = box.minmax(tile.box.getBoundingBox());
        }
        return box.inflate(2);
    }
}
