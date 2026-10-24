/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.builders.client.ClientArchitectTables;
import buildcraftreborn.lib.misc.SpriteUtil;

/** Draws the fading flash on each block an architect table has just scanned.
 * <p>
 * 1.21: drawn as a slightly larger translucent cube, so it shows on the block's own faces; 1.12 drew it through
 * everything, clipped to the scanned area with the stencil buffer. */
public final class RenderArchitectTables {
    private static final String SCAN_SPRITE = BuildCraftReborn.MODID + ":block/builders/scan";

    private RenderArchitectTables() {}

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS
            || ClientArchitectTables.SCANNED_BLOCKS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Vec3 camera = event.getCamera().getPosition();
        List<Map.Entry<BlockPos, Integer>> scanned = new ArrayList<>(ClientArchitectTables.SCANNED_BLOCKS.entrySet());
        scanned.sort(Comparator.<Map.Entry<BlockPos, Integer>>comparingDouble(
            e -> e.getKey().getCenter().distanceToSqr(camera)).reversed());
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer buffer = buffers.getBuffer(RenderType.translucent());
        TextureAtlasSprite sprite = SpriteUtil.getBlockSprite(SCAN_SPRITE);
        PoseStack.Pose p = pose.last();
        for (Map.Entry<BlockPos, Integer> entry : scanned) {
            int alpha = entry.getValue() * 50 / ClientArchitectTables.START_SCANNED_BLOCK_VALUE;
            AABB bb = new AABB(entry.getKey()).inflate(0.005).move(camera.scale(-1));
            for (Direction face : Direction.values()) {
                float[][] c = corners(bb, face);
                float[][] uv = { { sprite.getU0(), sprite.getV1() }, { sprite.getU1(), sprite.getV1() },
                    { sprite.getU1(), sprite.getV0() }, { sprite.getU0(), sprite.getV0() } };
                for (int i = 0; i < 4; i++) {
                    buffer.addVertex(p, c[i][0], c[i][1], c[i][2]).setColor(255, 255, 255, alpha)
                        .setUv(uv[i][0], uv[i][1]).setLight(LightTexture.FULL_BRIGHT)
                        .setNormal(p, face.getStepX(), face.getStepY(), face.getStepZ());
                }
            }
        }
        buffers.endBatch(RenderType.translucent());
    }

    private static float[][] corners(AABB b, Direction face) {
        float x0 = (float) b.minX, y0 = (float) b.minY, z0 = (float) b.minZ;
        float x1 = (float) b.maxX, y1 = (float) b.maxY, z1 = (float) b.maxZ;
        return switch (face) {
            case DOWN -> new float[][] { { x0, y0, z1 }, { x0, y0, z0 }, { x1, y0, z0 }, { x1, y0, z1 } };
            case UP -> new float[][] { { x0, y1, z0 }, { x0, y1, z1 }, { x1, y1, z1 }, { x1, y1, z0 } };
            case NORTH -> new float[][] { { x1, y0, z0 }, { x0, y0, z0 }, { x0, y1, z0 }, { x1, y1, z0 } };
            case SOUTH -> new float[][] { { x0, y0, z1 }, { x1, y0, z1 }, { x1, y1, z1 }, { x0, y1, z1 } };
            case WEST -> new float[][] { { x0, y0, z0 }, { x0, y0, z1 }, { x0, y1, z1 }, { x0, y1, z0 } };
            case EAST -> new float[][] { { x1, y0, z1 }, { x1, y0, z0 }, { x1, y1, z0 }, { x1, y1, z1 } };
        };
    }
}
