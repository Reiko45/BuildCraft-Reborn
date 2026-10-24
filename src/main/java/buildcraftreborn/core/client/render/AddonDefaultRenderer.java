/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.core.marker.volume.Addon;
import buildcraftreborn.lib.misc.SpriteUtil;

/** Draws an addon as a small textured cube on its corner of the volume box. */
public class AddonDefaultRenderer<T extends Addon> implements IFastAddonRenderer<T> {
    private final String sprite;

    public AddonDefaultRenderer() {
        this(BuildCraftReborn.MODID + ":block/white");
    }

    public AddonDefaultRenderer(String sprite) {
        this.sprite = sprite;
    }

    @Override
    public void renderAddonFast(T addon, Player player, float partialTicks, PoseStack pose, MultiBufferSource buffers,
        Vec3 camera) {
        renderCube(addon.getBoundingBox().move(camera.scale(-1)), SpriteUtil.getBlockSprite(sprite), 255, pose,
            buffers.getBuffer(RenderType.cutout()));
    }

    /** One cube, with each face shaded like 1.12 did. */
    public static void renderCube(AABB bb, TextureAtlasSprite s, int alpha, PoseStack pose, VertexConsumer vb) {
        PoseStack.Pose p = pose.last();
        quad(p, vb, s, 204, alpha, 0, 0, -1, bb.minX, bb.maxY, bb.minZ, bb.maxX, bb.maxY, bb.minZ, bb.maxX, bb.minY,
            bb.minZ, bb.minX, bb.minY, bb.minZ);
        quad(p, vb, s, 204, alpha, 0, 0, 1, bb.minX, bb.minY, bb.maxZ, bb.maxX, bb.minY, bb.maxZ, bb.maxX, bb.maxY,
            bb.maxZ, bb.minX, bb.maxY, bb.maxZ);
        quad(p, vb, s, 127, alpha, 0, -1, 0, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.minZ, bb.maxX, bb.minY,
            bb.maxZ, bb.minX, bb.minY, bb.maxZ);
        quad(p, vb, s, 255, alpha, 0, 1, 0, bb.minX, bb.maxY, bb.maxZ, bb.maxX, bb.maxY, bb.maxZ, bb.maxX, bb.maxY,
            bb.minZ, bb.minX, bb.maxY, bb.minZ);
        quad(p, vb, s, 153, alpha, -1, 0, 0, bb.minX, bb.minY, bb.maxZ, bb.minX, bb.maxY, bb.maxZ, bb.minX, bb.maxY,
            bb.minZ, bb.minX, bb.minY, bb.minZ);
        quad(p, vb, s, 153, alpha, 1, 0, 0, bb.maxX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.minZ, bb.maxX, bb.maxY,
            bb.maxZ, bb.maxX, bb.minY, bb.maxZ);
    }

    private static void quad(PoseStack.Pose p, VertexConsumer vb, TextureAtlasSprite s, int shade, int alpha, float nx,
        float ny, float nz, double x0, double y0, double z0, double x1, double y1, double z1, double x2, double y2,
        double z2, double x3, double y3, double z3) {
        int light = LightTexture.FULL_BRIGHT;
        vb.addVertex(p, (float) x0, (float) y0, (float) z0).setColor(shade, shade, shade, alpha)
            .setUv(s.getU0(), s.getV0()).setLight(light).setNormal(p, nx, ny, nz);
        vb.addVertex(p, (float) x1, (float) y1, (float) z1).setColor(shade, shade, shade, alpha)
            .setUv(s.getU0(), s.getV1()).setLight(light).setNormal(p, nx, ny, nz);
        vb.addVertex(p, (float) x2, (float) y2, (float) z2).setColor(shade, shade, shade, alpha)
            .setUv(s.getU1(), s.getV1()).setLight(light).setNormal(p, nx, ny, nz);
        vb.addVertex(p, (float) x3, (float) y3, (float) z3).setColor(shade, shade, shade, alpha)
            .setUv(s.getU1(), s.getV0()).setLight(light).setNormal(p, nx, ny, nz);
    }
}
