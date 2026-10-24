/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.robotics.zone;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.Level;

/** Map chunks, kept for a while. The server makes them; the client asks the server for them.
 * <p>
 * 1.21: the server keeps them for 10 seconds instead of 1.12's two minutes, and the client asks again for ones older
 * than 30 seconds, so changes to the world show on the map sooner. */
public abstract class ZonePlannerMapData {
    protected final Cache<ZonePlannerMapChunkKey, ZonePlannerMapChunk> data;

    protected ZonePlannerMapData(int keepSeconds) {
        data = CacheBuilder.newBuilder().expireAfterWrite(keepSeconds, TimeUnit.SECONDS).build();
    }

    /** Use {@link #getChunk(Level, ZonePlannerMapChunkKey)} for a cached version */
    @Nullable
    protected abstract ZonePlannerMapChunk loadChunk(Level level, ZonePlannerMapChunkKey key);

    @Nullable
    public ZonePlannerMapChunk getChunk(Level level, ZonePlannerMapChunkKey key) {
        ZonePlannerMapChunk chunk = data.getIfPresent(key);
        if (chunk != null) {
            return chunk;
        }
        chunk = loadChunk(level, key);
        if (chunk != null) {
            data.put(key, chunk);
        }
        return chunk;
    }

    public void clear() {
        data.invalidateAll();
    }
}
