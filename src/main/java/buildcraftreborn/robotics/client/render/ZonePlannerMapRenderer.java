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
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;

import buildcraftreborn.robotics.zone.ZonePlannerMapChunk.MapColourData;
import buildcraftreborn.robotics.zone.ZonePlannerMapChunk;
import buildcraftreborn.robotics.zone.ZonePlannerMapChunkKey;
import buildcraftreborn.robotics.zone.ZonePlannerMapDataClient;

/** Draws the zone planner's map: each column of a chunk as a coloured cuboid, from the bottom of the world up to
 * its top block. Blocks are centred on whole coordinates.
 * <p>
 * 1.21: each chunk is a vertex buffer (1.12: a GL display list), rebuilt when new data for it arrives. */
public enum ZonePlannerMapRenderer {
    INSTANCE;

    private record ChunkBuffer(ZonePlannerMapChunk source, VertexBuffer buffer) {}

    private final Cache<ZonePlannerMapChunkKey, ChunkBuffer> chunkBuffers = CacheBuilder.newBuilder()
        .expireAfterAccess(20, TimeUnit.SECONDS).removalListener(ZonePlannerMapRenderer::onRemove).build();
    private int r = 255, g = 255, b = 255, a = 255;

    private static void onRemove(RemovalNotification<ZonePlannerMapChunkKey, ChunkBuffer> notification) {
        ChunkBuffer value = notification.getValue();
        if (value != null) {
            value.buffer.close();
        }
    }

    /** Sets the colour of the following cuboids (ARGB; an alpha of 0 is drawn as opaque, as 1.12's map colours had
     * none). */
    public void setColor(int argb) {
        r = (argb >> 16) & 0xFF;
        g = (argb >> 8) & 0xFF;
        b = argb & 0xFF;
        int alpha = (argb >>> 24) & 0xFF;
        a = alpha == 0 ? 255 : alpha;
    }

    private void vertex(VertexConsumer builder, double x, double y, double z, double shade) {
        builder.addVertex((float) x, (float) y, (float) z).setColor((int) (r * shade), (int) (g * shade),
            (int) (b * shade), a);
    }

    /** A cuboid centred on x and z, whose top is at y. */
    public void drawBlockCuboid(VertexConsumer builder, double x, double y, double z, double height,
        double radius) {
        double rX = radius;
        double rY = height * 0.5;
        double rZ = radius;

        y -= rY;

        vertex(builder, x - rX, y + rY, z + rZ, 1);
        vertex(builder, x + rX, y + rY, z + rZ, 1);
        vertex(builder, x + rX, y + rY, z - rZ, 1);
        vertex(builder, x - rX, y + rY, z - rZ, 1);

        vertex(builder, x - rX, y - rY, z + rZ, 0.6);
        vertex(builder, x - rX, y + rY, z + rZ, 0.6);
        vertex(builder, x - rX, y + rY, z - rZ, 0.6);
        vertex(builder, x - rX, y - rY, z - rZ, 0.6);

        vertex(builder, x + rX, y - rY, z - rZ, 0.6);
        vertex(builder, x + rX, y + rY, z - rZ, 0.6);
        vertex(builder, x + rX, y + rY, z + rZ, 0.6);
        vertex(builder, x + rX, y - rY, z + rZ, 0.6);

        vertex(builder, x - rX, y - rY, z - rZ, 0.8);
        vertex(builder, x - rX, y + rY, z - rZ, 0.8);
        vertex(builder, x + rX, y + rY, z - rZ, 0.8);
        vertex(builder, x + rX, y - rY, z - rZ, 0.8);

        vertex(builder, x + rX, y - rY, z + rZ, 0.8);
        vertex(builder, x + rX, y + rY, z + rZ, 0.8);
        vertex(builder, x - rX, y + rY, z + rZ, 0.8);
        vertex(builder, x - rX, y - rY, z + rZ, 0.8);
    }

    public void drawBlockCuboid(VertexConsumer builder, double x, double y, double z, double height) {
        drawBlockCuboid(builder, x, y, z, height, 0.5);
    }

    /** A single block (as tall as it is wide). */
    public void drawBlockCuboid(VertexConsumer builder, double x, double y, double z) {
        drawBlockCuboid(builder, x, y, z, 1);
    }

    /** @return The chunk's buffer (made now if its data has arrived), or null if there is no data for it yet. */
    @Nullable
    public VertexBuffer getChunkBuffer(ZonePlannerMapChunkKey key) {
        ZonePlannerMapChunk chunk = ZonePlannerMapDataClient.INSTANCE.getChunk(Minecraft.getInstance().level, key);
        ChunkBuffer existing = chunkBuffers.getIfPresent(key);
        if (existing != null && (chunk == null || existing.source == chunk)) {
            return existing.buffer;
        }
        if (chunk == null) {
            return null;
        }
        VertexBuffer buffer = genChunk(key, chunk);
        if (buffer == null) {
            return null;
        }
        chunkBuffers.put(key, new ChunkBuffer(chunk, buffer));
        return buffer;
    }

    @Nullable
    private VertexBuffer genChunk(ZonePlannerMapChunkKey key, ZonePlannerMapChunk chunk) {
        int bottom = Minecraft.getInstance().level.getMinBuildHeight();
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                MapColourData data = chunk.getData(x, z);
                if (data != null) {
                    setColor(data.colour());
                    // (1.12 started the columns at 0, which is no longer the bottom of the world)
                    drawBlockCuboid(builder, key.chunkPos().getMinBlockX() + x, data.posY(),
                        key.chunkPos().getMinBlockZ() + z, data.posY() - bottom);
                }
            }
        }
        MeshData mesh = builder.build();
        if (mesh == null) {
            return null;
        }
        VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        buffer.bind();
        buffer.upload(mesh);
        VertexBuffer.unbind();
        return buffer;
    }

    /** Frees every buffer (when leaving a world). */
    public void clear() {
        chunkBuffers.invalidateAll();
    }
}
