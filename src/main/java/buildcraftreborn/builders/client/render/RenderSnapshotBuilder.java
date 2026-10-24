/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client.render;

import java.util.Collections;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.builders.BCBuildersSprites;
import buildcraftreborn.builders.snapshot.ITileForSnapshotBuilder;
import buildcraftreborn.builders.snapshot.SnapshotBuilder;
import buildcraftreborn.core.client.BuildCraftLaserManager;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8;
import buildcraftreborn.lib.client.render.laser.LaserRenderer_BC8;
import buildcraftreborn.lib.misc.MathUtil;
import buildcraftreborn.lib.misc.SpriteUtil;
import buildcraftreborn.lib.misc.VecUtil;

/** Draws what a filler or builder is doing: the items flying to where they will be placed, the little robot, and the
 * lasers it breaks blocks with. */
public final class RenderSnapshotBuilder {
    private RenderSnapshotBuilder() {}

    /** @param pose The pose of the tile's renderer (at {@code tilePos}). */
    public static <T extends ITileForSnapshotBuilder> void render(SnapshotBuilder<T> snapshotBuilder, Level level,
        BlockPos tilePos, float partialTicks, PoseStack pose, MultiBufferSource buffers) {
        Vec3 origin = Vec3.atLowerCornerOf(tilePos);
        Minecraft mc = Minecraft.getInstance();
        for (SnapshotBuilder<T>.PlaceTask placeTask : snapshotBuilder.clientPlaceTasks) {
            Vec3 prevPos = snapshotBuilder.prevClientPlaceTasks.stream()
                .filter(renderTaskLocal -> renderTaskLocal.pos.equals(placeTask.pos))
                .map(snapshotBuilder::getPlaceTaskItemPos)
                .findFirst()
                .orElse(snapshotBuilder.getPlaceTaskItemPos(
                    snapshotBuilder.new PlaceTask(tilePos, Collections.emptyList(), 0L)));
            Vec3 pos = prevPos.add(snapshotBuilder.getPlaceTaskItemPos(placeTask).subtract(prevPos)
                .scale(partialTicks));
            int light = LevelRenderer.getLightColor(level, BlockPos.containing(pos));
            for (ItemStack item : placeTask.items) {
                pose.pushPose();
                pose.translate(pos.x - origin.x, pos.y - origin.y, pos.z - origin.z);
                mc.getItemRenderer().renderStatic(item, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY,
                    pose, buffers, level, 0);
                pose.popPose();
            }
        }

        Vec3 robotPos = snapshotBuilder.robotPos;
        if (robotPos != null) {
            if (snapshotBuilder.prevRobotPos != null) {
                robotPos = snapshotBuilder.prevRobotPos.add(robotPos.subtract(snapshotBuilder.prevRobotPos)
                    .scale(partialTicks));
            }
            VertexConsumer buffer = buffers.getBuffer(RenderType.cutout());
            renderRobot(robotPos.subtract(origin), LevelRenderer.getLightColor(level, BlockPos.containing(robotPos)),
                pose, buffer);
            for (SnapshotBuilder<T>.BreakTask breakTask : snapshotBuilder.clientBreakTasks) {
                int index = (int) Math.round(MathUtil.clamp(breakTask.power * 1D / Math.max(1, breakTask.getTarget()),
                    0D, 1D) * (BuildCraftLaserManager.POWERS.length - 1));
                LaserRenderer_BC8.renderLaser(new LaserData_BC8(BuildCraftLaserManager.POWERS[index],
                    robotPos.subtract(new Vec3(0, 0.27, 0)), Vec3.atLowerCornerOf(breakTask.pos)
                        .add(VecUtil.VEC_HALF), 1 / 16D), pose, buffer, origin);
            }
        }
    }

    /** The robot: an 8 pixel cube, each face a different 8x8 part of the robot texture. */
    private static void renderRobot(Vec3 centre, int light, PoseStack pose, VertexConsumer buffer) {
        TextureAtlasSprite sprite = SpriteUtil.getBlockSprite(BCBuildersSprites.ROBOT);
        PoseStack.Pose p = pose.last();
        float r = 4 / 16F;
        float x = (float) centre.x, y = (float) centre.y, z = (float) centre.z;
        int i = 0;
        for (Direction face : Direction.values()) {
            float u0 = sprite.getU(i * 8 / 64F);
            float u1 = sprite.getU((i + 1) * 8 / 64F);
            float v0 = sprite.getV(0);
            float v1 = sprite.getV(8 / 64F);
            float[][] corners = faceCorners(face, x, y, z, r);
            float[][] uvs = { { u0, v1 }, { u1, v1 }, { u1, v0 }, { u0, v0 } };
            for (int c = 0; c < 4; c++) {
                buffer.addVertex(p, corners[c][0], corners[c][1], corners[c][2]).setColor(255, 255, 255, 255)
                    .setUv(uvs[c][0], uvs[c][1]).setLight(light)
                    .setNormal(p, face.getStepX(), face.getStepY(), face.getStepZ());
            }
            i++;
        }
    }

    /** The corners of one face of a cube, anticlockwise when seen from outside (bottom left first). */
    private static float[][] faceCorners(Direction face, float x, float y, float z, float r) {
        return switch (face) {
            case DOWN -> new float[][] { { x - r, y - r, z + r }, { x - r, y - r, z - r }, { x + r, y - r, z - r },
                { x + r, y - r, z + r } };
            case UP -> new float[][] { { x - r, y + r, z - r }, { x - r, y + r, z + r }, { x + r, y + r, z + r },
                { x + r, y + r, z - r } };
            case NORTH -> new float[][] { { x + r, y - r, z - r }, { x - r, y - r, z - r }, { x - r, y + r, z - r },
                { x + r, y + r, z - r } };
            case SOUTH -> new float[][] { { x - r, y - r, z + r }, { x + r, y - r, z + r }, { x + r, y + r, z + r },
                { x - r, y + r, z + r } };
            case WEST -> new float[][] { { x - r, y - r, z - r }, { x - r, y - r, z + r }, { x - r, y + r, z + r },
                { x - r, y + r, z - r } };
            case EAST -> new float[][] { { x + r, y - r, z + r }, { x + r, y - r, z - r }, { x + r, y + r, z - r },
                { x + r, y + r, z + r } };
        };
    }
}
