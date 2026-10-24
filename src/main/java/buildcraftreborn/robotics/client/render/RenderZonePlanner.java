/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.robotics.client.render;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.robotics.client.BCRoboticsClient;
import buildcraftreborn.robotics.tile.TileZonePlanner;
import buildcraftreborn.robotics.zone.ZonePlannerMapChunk;
import buildcraftreborn.robotics.zone.ZonePlannerMapChunkKey;
import buildcraftreborn.robotics.zone.ZonePlannerMapDataClient;

/** Draws a small map (one pixel every four blocks) of the area around a zone planner on its screen. Up on the map
 * is away from whoever looks at the screen. */
public class RenderZonePlanner implements BlockEntityRenderer<TileZonePlanner> {
    private static final int TEXTURE_WIDTH = 10;
    private static final int TEXTURE_HEIGHT = 8;
    private static final int SCALE = 4;

    private record WorldPos(ResourceKey<Level> dimension, BlockPos pos) {}

    private static final Cache<WorldPos, ResourceLocation> TEXTURES = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES).removalListener(RenderZonePlanner::onRemove).build();
    private static int nextTextureId = 0;

    public RenderZonePlanner(BlockEntityRendererProvider.Context context) {}

    private static void onRemove(RemovalNotification<WorldPos, ResourceLocation> notification) {
        ResourceLocation texture = notification.getValue();
        if (texture != null) {
            Minecraft.getInstance().getTextureManager().release(texture);
        }
    }

    /** Frees every map texture (when leaving a world). */
    public static void clear() {
        TEXTURES.invalidateAll();
    }

    @Override
    public void render(TileZonePlanner tile, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light,
        int overlay) {
        BlockState state = tile.getBlockState();
        if (!state.is(BCRoboticsBlocks.ZONE_PLANNER.get())) {
            return;
        }
        Direction facing = state.getValue(BlockBCBase_Neptune.PROP_FACING);
        ResourceLocation texture = getTexture(tile, facing);
        if (texture == null) {
            return;
        }
        float offset = 0.001f;
        float minH = 3 / 16f - offset, maxH = 13 / 16f + offset;
        float minY = 5 / 16f - offset, maxY = 13 / 16f + offset;
        Vector3f normal = facing.step();
        // the viewer's right, as they look at the screen
        Vector3f right = facing.getOpposite().getClockWise().step();
        VertexConsumer vc = buffers.getBuffer(RenderType.text(texture));
        PoseStack.Pose last = pose.last();
        // bottom left, bottom right, top right, top left (seen from the front)
        // (u, v): the image's first row is at the bottom of the screen
        float[][] corners = { { minH, minY, 0, 0 }, { maxH, minY, 1, 0 }, { maxH, maxY, 1, 1 }, { minH, maxY, 0, 1 } };
        for (float[] corner : corners) {
            float h = corner[0] - 0.5f;
            float x = 0.5f + normal.x() * (0.5f + offset) + right.x() * h;
            float z = 0.5f + normal.z() * (0.5f + offset) + right.z() * h;
            vc.addVertex(last, x, corner[1], z).setColor(-1).setUv(corner[2], corner[3])
                .setLight(LightTexture.FULL_BRIGHT);
        }
    }

    @Nullable
    private static ResourceLocation getTexture(TileZonePlanner tile, Direction facing) {
        Level level = tile.getLevel();
        if (level == null) {
            return null;
        }
        WorldPos key = new WorldPos(level.dimension(), tile.getBlockPos());
        ResourceLocation texture = TEXTURES.getIfPresent(key);
        if (texture == null) {
            texture = createTexture(tile, facing);
            if (texture != null) {
                TEXTURES.put(key, texture);
            }
        }
        return texture;
    }

    /** @return The map texture, or null if some of the map hasn't arrived from the server yet. */
    @Nullable
    private static ResourceLocation createTexture(TileZonePlanner tile, Direction facing) {
        Level level = tile.getLevel();
        int mapLevel = BCRoboticsClient.getMapLevel();
        Direction right = facing.getOpposite().getClockWise();
        Direction away = facing.getOpposite();
        NativeImage image = new NativeImage(TEXTURE_WIDTH, TEXTURE_HEIGHT, false);
        for (int textureX = 0; textureX < TEXTURE_WIDTH; textureX++) {
            for (int textureY = 0; textureY < TEXTURE_HEIGHT; textureY++) {
                int offsetRight = (textureX - TEXTURE_WIDTH / 2) * SCALE;
                int offsetAway = (textureY - TEXTURE_HEIGHT / 2) * SCALE;
                BlockPos pos = tile.getBlockPos().relative(right, offsetRight).relative(away, offsetAway);
                ZonePlannerMapChunk chunk = ZonePlannerMapDataClient.INSTANCE.getChunk(level,
                    new ZonePlannerMapChunkKey(new ChunkPos(pos), level.dimension(), mapLevel));
                if (chunk == null) {
                    image.close();
                    return null;
                }
                int colour = chunk.getColour(pos.getX(), pos.getZ()) | 0xFF_00_00_00;
                image.setPixelRGBA(textureX, textureY, FastColor.ABGR32.fromArgb32(colour));
            }
        }
        ResourceLocation location = BuildCraftReborn.id("zone_planner_map/" + nextTextureId++);
        Minecraft.getInstance().getTextureManager().register(location, new DynamicTexture(image));
        return location;
    }
}
