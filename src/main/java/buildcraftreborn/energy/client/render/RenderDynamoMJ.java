/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.enums.EnumPowerStage;
import buildcraftreborn.energy.tile.TileDynamoMJ;
import buildcraftreborn.lib.client.render.tile.RenderEngine_BC8;

/** Draws the MJ dynamo from the geometry of 1.12's {@code mj_dynamo.json} variable model: a back plate, a moving
 * front plate (smaller than an engine's), the coloured trunk and the bellows chamber. */
public class RenderDynamoMJ implements BlockEntityRenderer<TileDynamoMJ> {
    private static final ResourceLocation BACK = BuildCraftReborn.id("block/mj_dynamo/back");
    private static final ResourceLocation FRONT = BuildCraftReborn.id("block/mj_dynamo/front");
    private static final ResourceLocation SIDE = BuildCraftReborn.id("block/mj_dynamo/side");
    private static final ResourceLocation CHAMBER = BuildCraftReborn.id("block/engine/chamber_base");

    public static final BlockEntityRendererProvider<TileDynamoMJ> PROVIDER = ctx -> new RenderDynamoMJ();

    @Override
    public void render(TileDynamoMJ dynamo, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light,
        int overlay) {
        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite back = atlas.getSprite(BACK);
        TextureAtlasSprite front = atlas.getSprite(FRONT);
        TextureAtlasSprite side = atlas.getSprite(SIDE);
        TextureAtlasSprite chamber = atlas.getSprite(CHAMBER);
        EnumPowerStage stage = dynamo.getPowerStage();
        TextureAtlasSprite trunk = atlas.getSprite(RenderEngine_BC8.trunkTexture(stage));

        float p = RenderEngine_BC8.progressSize(dynamo.getProgressClient(partialTicks));
        VertexConsumer vc = buffers.getBuffer(RenderType.cutout());

        pose.pushPose();
        RenderEngine_BC8.rotateFromUp(pose, dynamo.getCurrentDirection());
        PoseStack.Pose last = pose.last();

        // base: the back plate
        cuboid(vc, last, light, 0, 0, 0, 16, 4, 16, back, 0, 0, 16, 16, side, 0, 0, 16, 4, true);
        // the moving front plate
        cuboid(vc, last, light, 2, 4 + p, 2, 14, 8 + p, 14, front, 0, 0, 12, 12, front, 0, 12, 12, 16, true);
        // trunk: glows with the power stage
        int blockLight = Math.max(LightTexture.block(light), RenderEngine_BC8.stageLight(stage));
        int trunkLight = LightTexture.pack(blockLight, LightTexture.sky(light));
        cuboid(vc, last, trunkLight, 4, 4, 4, 12, 16, 12, trunk, 0, 0, 8, 8, trunk, 8, 0, 16, 12, true);
        // chamber: sides only, the texture squashed with the bellows (1.12: uv [3, progress_size, 13, 0])
        if (p > 0) {
            cuboid(vc, last, light, 3, 4, 3, 13, 4 + p, 13, null, 0, 0, 0, 0, chamber, 3, p, 13, 0, false);
        }
        pose.popPose();
    }

    /** Draws a cuboid (coordinates and UVs in pixels), with one UV rectangle for its ends and one for its sides. */
    private static void cuboid(VertexConsumer vc, PoseStack.Pose pose, int light, float x0, float y0, float z0,
        float x1, float y1, float z1, TextureAtlasSprite ends, float eu0, float ev0, float eu1, float ev1,
        TextureAtlasSprite sides, float su0, float sv0, float su1, float sv1, boolean drawEnds) {
        float a0 = x0 / 16, b0 = y0 / 16, c0 = z0 / 16, a1 = x1 / 16, b1 = y1 / 16, c1 = z1 / 16;
        if (drawEnds && ends != null) {
            quad(vc, pose, light, ends, 0.5f, 0, -1, 0, a0, b0, c1, eu0, ev0, a0, b0, c0, eu0, ev1, a1, b0, c0, eu1,
                ev1, a1, b0, c1, eu1, ev0);
            quad(vc, pose, light, ends, 1.0f, 0, 1, 0, a0, b1, c0, eu0, ev0, a0, b1, c1, eu0, ev1, a1, b1, c1, eu1,
                ev1, a1, b1, c0, eu1, ev0);
        }
        // north (-Z), south (+Z), west (-X), east (+X); vertices top-left, bottom-left, bottom-right, top-right
        quad(vc, pose, light, sides, 0.8f, 0, 0, -1, a1, b1, c0, su0, sv0, a1, b0, c0, su0, sv1, a0, b0, c0, su1, sv1,
            a0, b1, c0, su1, sv0);
        quad(vc, pose, light, sides, 0.8f, 0, 0, 1, a0, b1, c1, su0, sv0, a0, b0, c1, su0, sv1, a1, b0, c1, su1, sv1,
            a1, b1, c1, su1, sv0);
        quad(vc, pose, light, sides, 0.6f, -1, 0, 0, a0, b1, c0, su0, sv0, a0, b0, c0, su0, sv1, a0, b0, c1, su1,
            sv1, a0, b1, c1, su1, sv0);
        quad(vc, pose, light, sides, 0.6f, 1, 0, 0, a1, b1, c1, su0, sv0, a1, b0, c1, su0, sv1, a1, b0, c0, su1, sv1,
            a1, b1, c0, su1, sv0);
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose pose, int light, TextureAtlasSprite sprite,
        float shade, float nx, float ny, float nz, float... v) {
        for (int i = 0; i < 4; i++) {
            int o = i * 5;
            vc.addVertex(pose, v[o], v[o + 1], v[o + 2])
                .setColor(shade, shade, shade, 1f)
                .setUv(sprite.getU(v[o + 3] / 16f), sprite.getV(v[o + 4] / 16f))
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
        }
    }
}
