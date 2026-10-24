/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.enums.EnumPowerStage;
import buildcraftreborn.lib.engine.TileEngineBase_BC8;

/** Draws an engine with its moving piston, from the same geometry as 1.12's {@code engine_base.json} variable model:
 * the base plate, the moving plate (raised by {@code progress_size}), the coloured trunk (which glows with the power
 * stage) and the bellows chamber between the plates.
 * <p>
 * Port note: 1.12 evaluated that JSON with the expression language every frame (lib/client/model). That model system
 * is ported with the pluggables and gates that need it (phase 4); engines draw the same shapes directly here. */
public class RenderEngine_BC8<T extends TileEngineBase_BC8> implements BlockEntityRenderer<T> {
    private static final ResourceLocation CHAMBER = BuildCraftReborn.id("block/engine/chamber_base");

    private final ResourceLocation back, side;
    private final boolean stageLight;

    /** @param back The texture of the plates' faces ({@code block/engine/<type>/back}).
     * @param side The texture of the plates' edges.
     * @param stageLight Whether the trunk glows with the power stage (false for the creative engine). */
    public RenderEngine_BC8(ResourceLocation back, ResourceLocation side, boolean stageLight) {
        this.back = back;
        this.side = side;
        this.stageLight = stageLight;
    }

    public static <T extends TileEngineBase_BC8> BlockEntityRendererProvider<T> provider(String type,
        boolean stageLight) {
        ResourceLocation back = BuildCraftReborn.id("block/engine/" + type + "/back");
        ResourceLocation side = BuildCraftReborn.id("block/engine/" + type + "/side");
        return ctx -> new RenderEngine_BC8<>(back, side, stageLight);
    }

    /** The trunk texture for a power stage ({@code block/engine/trunk_<stage>}; black uses the overheat one). */
    public static ResourceLocation trunkTexture(EnumPowerStage stage) {
        String name = stage == EnumPowerStage.BLACK ? "overheat" : stage.getModelName();
        return BuildCraftReborn.id("block/engine/trunk_" + name);
    }

    /** 1.12's {@code progress_size}: how far (in pixels) the moving plate is raised for a given progress. */
    public static float progressSize(float progress) {
        return (progress > 0.5f ? (1 - progress) : progress) * (8 * 2 - 0.01f);
    }

    public static int stageLight(EnumPowerStage stage) {
        return switch (stage) {
            case OVERHEAT, RED -> 10;
            case YELLOW -> 7;
            case GREEN -> 4;
            default -> 0;
        };
    }

    @Override
    public void render(T engine, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light,
        int overlay) {
        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite spriteBack = atlas.getSprite(back);
        TextureAtlasSprite spriteSide = atlas.getSprite(side);
        TextureAtlasSprite spriteChamber = atlas.getSprite(CHAMBER);
        EnumPowerStage stage = engine.getPowerStage();
        TextureAtlasSprite spriteTrunk = atlas.getSprite(trunkTexture(stage));

        float p = progressSize(engine.getProgressClient(partialTicks));
        VertexConsumer vc = buffers.getBuffer(RenderType.cutout());

        pose.pushPose();
        rotateFromUp(pose, engine.getCurrentFacing());

        // base
        box(vc, pose, light, 0, 0, 0, 16, 4, 16, spriteBack, spriteSide, null, 4);
        // base_moving
        box(vc, pose, light, 0, 4 + p, 0, 16, 8 + p, 16, spriteBack, spriteSide, null, 4);
        // trunk: glows with the power stage
        int trunkLight = light;
        if (stageLight) {
            int blockLight = Math.max(LightTexture.block(light), stageLight(stage));
            trunkLight = LightTexture.pack(blockLight, LightTexture.sky(light));
        }
        box(vc, pose, trunkLight, 4, 4, 4, 12, 16, 12, spriteTrunk, spriteTrunk, null, 12);
        // chamber: sides only, texture squashed with the bellows
        if (p > 0) {
            box(vc, pose, light, 3, 4, 3, 13, 4 + p, 13, null, null, spriteChamber, p);
        }
        pose.popPose();
    }

    /** Rotates the model (built facing up) to face {@code direction}, around the block centre. */
    public static void rotateFromUp(PoseStack pose, Direction direction) {
        pose.translate(0.5, 0.5, 0.5);
        switch (direction) {
            case DOWN -> pose.mulPose(Axis.XP.rotationDegrees(180));
            case NORTH -> pose.mulPose(Axis.XP.rotationDegrees(-90));
            case SOUTH -> pose.mulPose(Axis.XP.rotationDegrees(90));
            case EAST -> pose.mulPose(Axis.ZP.rotationDegrees(-90));
            case WEST -> pose.mulPose(Axis.ZP.rotationDegrees(90));
            default -> {}
        }
        pose.translate(-0.5, -0.5, -0.5);
    }

    /** Draws a cuboid (coordinates in pixels).
     *
     * @param ends Texture for the top and bottom faces (UV 0-16 for the plates, 0-8 for the trunk), or null for none.
     * @param sides Texture for the side faces (UV 0,0 to 16,sideV), or null to use {@code chamber}.
     * @param chamber If non-null, the sides use this texture with 1.12's chamber UVs (3..13 across, flipped).
     * @param sideV The V size of the side faces in pixels. */
    private static void box(VertexConsumer vc, PoseStack pose, int light, float x0, float y0, float z0, float x1,
        float y1, float z1, TextureAtlasSprite ends, TextureAtlasSprite sides, TextureAtlasSprite chamber,
        float sideV) {
        PoseStack.Pose last = pose.last();
        float a0 = x0 / 16, b0 = y0 / 16, c0 = z0 / 16, a1 = x1 / 16, b1 = y1 / 16, c1 = z1 / 16;
        boolean trunk = ends != null && ends == sides;
        if (ends != null) {
            float uMax = trunk ? 8 / 16f : 1;
            // down (normal -Y) and up (+Y)
            quad(vc, last, light, ends, 0.5f, 0, -1, 0, a0, b0, c1, 0, 0, a0, b0, c0, 0, uMax, a1, b0, c0, uMax, uMax,
                a1, b0, c1, uMax, 0);
            quad(vc, last, light, ends, 1.0f, 0, 1, 0, a0, b1, c0, 0, 0, a0, b1, c1, 0, uMax, a1, b1, c1, uMax, uMax,
                a1, b1, c0, uMax, 0);
        }
        TextureAtlasSprite s = chamber != null ? chamber : sides;
        float uL, uR, vT, vB;
        if (chamber != null) {
            uL = 3 / 16f;
            uR = 13 / 16f;
            vT = sideV / 16f; // 1.12: uv [3, progress_size, 13, 0] (flipped)
            vB = 0;
        } else if (trunk) {
            uL = 8 / 16f;
            uR = 1;
            vT = 0;
            vB = 12 / 16f;
        } else {
            uL = 0;
            uR = 1;
            vT = 0;
            vB = sideV / 16f;
        }
        // north (-Z), south (+Z), west (-X), east (+X); vertices top-left, bottom-left, bottom-right, top-right
        quad(vc, last, light, s, 0.8f, 0, 0, -1, a1, b1, c0, uL, vT, a1, b0, c0, uL, vB, a0, b0, c0, uR, vB, a0, b1,
            c0, uR, vT);
        quad(vc, last, light, s, 0.8f, 0, 0, 1, a0, b1, c1, uL, vT, a0, b0, c1, uL, vB, a1, b0, c1, uR, vB, a1, b1,
            c1, uR, vT);
        quad(vc, last, light, s, 0.6f, -1, 0, 0, a0, b1, c0, uL, vT, a0, b0, c0, uL, vB, a0, b0, c1, uR, vB, a0, b1,
            c1, uR, vT);
        quad(vc, last, light, s, 0.6f, 1, 0, 0, a1, b1, c1, uL, vT, a1, b0, c1, uL, vB, a1, b0, c0, uR, vB, a1, b1,
            c0, uR, vT);
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose pose, int light, TextureAtlasSprite sprite,
        float shade, float nx, float ny, float nz, float... v) {
        for (int i = 0; i < 4; i++) {
            int o = i * 5;
            vc.addVertex(pose, v[o], v[o + 1], v[o + 2])
                .setColor(shade, shade, shade, 1f)
                .setUv(sprite.getU(v[o + 3]), sprite.getV(v[o + 4]))
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
        }
    }
}
