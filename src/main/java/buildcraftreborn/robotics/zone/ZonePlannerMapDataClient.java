/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.robotics.zone;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.Util;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.network.PacketDistributor;

/** The client's map chunks: asks the server for each missing one, and asks again if no answer came within a few
 * seconds (the server doesn't answer for chunks that aren't loaded, or are too far from the player).
 * <p>
 * 1.21: a chunk older than 30 seconds is asked for again (1.12 kept each one for two minutes), and still shown until
 * the new one comes. */
public class ZonePlannerMapDataClient extends ZonePlannerMapData {
    public static final ZonePlannerMapDataClient INSTANCE = new ZonePlannerMapDataClient();

    private static final int KEEP_SECONDS = 120;
    private static final long RETRY_MILLIS = 5000;
    private static final long REFRESH_MILLIS = 30_000;

    /** The time each pending chunk was asked for. */
    private final Map<ZonePlannerMapChunkKey, Long> pending = new HashMap<>();
    /** The time each chunk came. */
    private final Cache<ZonePlannerMapChunkKey, Long> receivedTimes =
        CacheBuilder.newBuilder().expireAfterWrite(KEEP_SECONDS, TimeUnit.SECONDS).build();

    private ZonePlannerMapDataClient() {
        super(KEEP_SECONDS);
    }

    @Override
    @Nullable
    public ZonePlannerMapChunk getChunk(Level level, ZonePlannerMapChunkKey key) {
        ZonePlannerMapChunk chunk = super.getChunk(level, key);
        if (chunk != null) {
            Long received = receivedTimes.getIfPresent(key);
            if (received == null || Util.getMillis() - received > REFRESH_MILLIS) {
                request(key);
            }
        }
        return chunk;
    }

    @Override
    @Nullable
    protected ZonePlannerMapChunk loadChunk(Level level, ZonePlannerMapChunkKey key) {
        request(key);
        return null;
    }

    private void request(ZonePlannerMapChunkKey key) {
        long now = Util.getMillis();
        Long asked = pending.get(key);
        if (asked == null || now - asked > RETRY_MILLIS) {
            pending.put(key, now);
            PacketDistributor.sendToServer(new ZoneMapRequestPayload(key));
        }
    }

    public void onChunkReceived(ZonePlannerMapChunkKey key, ZonePlannerMapChunk chunk) {
        pending.remove(key);
        receivedTimes.put(key, Util.getMillis());
        data.put(key, chunk);
    }

    @Override
    public void clear() {
        super.clear();
        pending.clear();
        receivedTimes.invalidateAll();
    }
}
