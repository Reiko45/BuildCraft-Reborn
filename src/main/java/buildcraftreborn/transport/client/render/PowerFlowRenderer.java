/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import buildcraftreborn.lib.client.model.ModelUtil;
import buildcraftreborn.lib.client.model.ModelUtil.UvFaceData;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.lib.misc.MathUtil;
import buildcraftreborn.lib.misc.VecUtil;

/** Draws the glowing power flow inside kinesis (MJ) and FE pipes: a bar per section, as wide as the power flowing,
 * with its texture scrolling the way the power goes. */
final class PowerFlowRenderer {
    private static final int FULL_BRIGHT = LightTexture.pack(15, 15);

    private PowerFlowRenderer() {}

    /** @param power The power in each section, by side ordinal (1 is a full pipe).
     * @param connected Whether each side is connected.
     * @param sideOffsets The texture scroll of each side, from 0 to 16.
     * @param centreOffset The texture scroll of the centre, per axis. */
    static void render(double[] power, boolean[] connected, double[] sideOffsets, Vec3 centreOffset,
        TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffers) {
        double centrePower = 0;
        for (double p : power) {
            centrePower = Math.max(centrePower, p);
        }
        if (centrePower <= 0) {
            return;
        }
        VertexConsumer vc = buffers.getBuffer(RenderType.translucent());
        for (Direction side : Direction.values()) {
            int i = side.ordinal();
            if (connected[i]) {
                renderSidePower(side, power[i], centrePower, sideOffsets[i], sprite, poseStack, vc);
            }
        }
        renderCentrePower(centrePower, centreOffset, sprite, poseStack, vc);
    }

    static double computeOffset(double tick0, double tick1, float partialTicks) {
        if (tick0 + 8 < tick1) {
            // overflow upwards
            tick0 += 16;
        } else if (tick1 + 8 < tick0) {
            // overflow downwards
            tick1 += 16;
        }
        double offset = MathUtil.interp(partialTicks, tick0, tick1);
        if (offset >= 16) {
            offset -= 16;
        }
        return offset;
    }

    private static void renderSidePower(Direction side, double power, double centrePower, double offset,
        TextureAtlasSprite sprite, PoseStack poseStack, VertexConsumer vc) {
        if (power < 0) {
            return;
        }
        double radius = Math.min(0.248 * power, 0.248);
        double centreRadius = 0.252 - (0.248 * centrePower);

        Vec3 centre = VecUtil.offset(VecUtil.VEC_HALF, side, 0.25 + 0.125 - centreRadius / 2);
        Vec3 radiusV = new Vec3(radius, radius, radius);
        radiusV = VecUtil.replaceValue(radiusV, side.getAxis(), 0.125 + centreRadius / 2);

        Vector3f centreF = new Vector3f((float) centre.x, (float) centre.y, (float) centre.z);
        Vector3f radiusF = new Vector3f((float) radiusV.x, (float) radiusV.y, (float) radiusV.z);

        UvFaceData uvs = new UvFaceData();
        for (Direction face : Direction.values()) {
            if (face == side.getOpposite()) {
                continue;
            }
            AABB box = new AABB(centre.subtract(radiusV).scale(0.5), centre.add(radiusV).scale(0.5));
            box = box.move(VecUtil.offset(Vec3.ZERO, side, offset * side.getAxisDirection().getStep() / 32));
            ModelUtil.mapBoxToUvs(box, face, uvs);

            MutableQuad quad = ModelUtil.createFace(face, centreF, radiusF, uvs);
            quad.texFromSprite(sprite);
            quad.render(poseStack.last(), vc, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }
    }

    private static void renderCentrePower(double power, Vec3 offset, TextureAtlasSprite sprite, PoseStack poseStack,
        VertexConsumer vc) {
        float radius = Math.min(0.248f * (float) power, 0.248f);
        Vector3f centre = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f radiusP = new Vector3f(radius, radius, radius);

        UvFaceData uvs = new UvFaceData();
        for (Direction face : Direction.values()) {
            AABB box = new AABB(//
                new Vec3(0.5 - radius, 0.5 - radius, 0.5 - radius).scale(0.5), //
                new Vec3(0.5 + radius, 0.5 + radius, 0.5 + radius).scale(0.5)//
            );
            box = box.move(offset.x / 32.0, offset.y / 32.0, offset.z / 32.0);
            ModelUtil.mapBoxToUvs(box, face, uvs);

            MutableQuad quad = ModelUtil.createFace(face, centre, radiusP, uvs);
            quad.texFromSprite(sprite);
            quad.render(poseStack.last(), vc, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }
    }
}
