/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.robotics.zone;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/** Which map chunk: a chunk of a dimension, seen from a height {@link #level} (in steps of {@link #LEVEL_HEIGHT}
 * blocks), so that the map can show what is under a roof.
 * <p>
 * 1.21: a record, and the dimension is its key (1.12: a numeric id). */
public record ZonePlannerMapChunkKey(ChunkPos chunkPos, ResourceKey<Level> dimension, int level) {
    public static final int LEVEL_HEIGHT = 32;

    public static final StreamCodec<FriendlyByteBuf, ZonePlannerMapChunkKey> STREAM_CODEC = StreamCodec.of(
        (buf, key) -> {
            buf.writeInt(key.chunkPos.x);
            buf.writeInt(key.chunkPos.z);
            buf.writeResourceKey(key.dimension);
            buf.writeInt(key.level);
        },
        buf -> new ZonePlannerMapChunkKey(new ChunkPos(buf.readInt(), buf.readInt()),
            buf.readResourceKey(Registries.DIMENSION), buf.readInt()));
}
