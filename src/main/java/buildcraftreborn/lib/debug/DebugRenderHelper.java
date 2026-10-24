/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraftreborn.lib.client.render.DetachedRenderer.IDetachedRenderer;

/** Draws the current advanced debug target (see {@link BCAdvDebugging}), and has helpers for debug renderers: see-
 * through coloured boxes ({@code 0xAARRGGBB}). */
@OnlyIn(Dist.CLIENT)
public enum DebugRenderHelper implements IDetachedRenderer {
    INSTANCE;

    @Override
    public void render(Player player, float partialTicks, PoseStack pose, MultiBufferSource buffers, Vec3 camera) {
        IAdvDebugTarget target = BCAdvDebugging.getClientDebugTarget();
        if (target == null) {
            return;
        }
        IDetachedRenderer renderer = target.getDebugRenderer();
        if (renderer != null) {
            renderer.render(player, partialTicks, pose, buffers, camera);
        }
    }

    /** @return The buffer that the helpers below draw into. */
    public static VertexConsumer buffer(MultiBufferSource buffers) {
        return buffers.getBuffer(RenderType.debugQuads());
    }

    /** Draws a box (in world coordinates). */
    public static void renderAABB(PoseStack pose, VertexConsumer vc, Vec3 camera, AABB aabb, int colour) {
        AABB bb = aabb.move(camera.scale(-1));
        PoseStack.Pose last = pose.last();
        float x0 = (float) bb.minX, y0 = (float) bb.minY, z0 = (float) bb.minZ;
        float x1 = (float) bb.maxX, y1 = (float) bb.maxY, z1 = (float) bb.maxZ;
        int a = (colour >>> 24) & 0xFF, r = (colour >> 16) & 0xFF, g = (colour >> 8) & 0xFF, b = colour & 0xFF;
        // down, up, north, south, west, east
        quad(last, vc, r, g, b, a, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        quad(last, vc, r, g, b, a, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
        quad(last, vc, r, g, b, a, x0, y1, z0, x1, y1, z0, x1, y0, z0, x0, y0, z0);
        quad(last, vc, r, g, b, a, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        quad(last, vc, r, g, b, a, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        quad(last, vc, r, g, b, a, x1, y1, z0, x1, y1, z1, x1, y0, z1, x1, y0, z0);
    }

    /** Draws a half-size cube in the middle of a block. */
    public static void renderSmallCuboid(PoseStack pose, VertexConsumer vc, Vec3 camera, BlockPos pos, int colour) {
        renderAABB(pose, vc, camera, new AABB(pos).deflate(0.25), colour);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer vc, int r, int g, int b, int a, float... v) {
        for (int i = 0; i < 4; i++) {
            vc.addVertex(pose, v[i * 3], v[i * 3 + 1], v[i * 3 + 2]).setColor(r, g, b, a);
        }
    }
}
