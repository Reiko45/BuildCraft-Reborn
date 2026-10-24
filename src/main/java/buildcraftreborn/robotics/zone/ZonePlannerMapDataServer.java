/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.robotics.zone;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.Level;

/** Scans map chunks on the server. Only loaded chunks are scanned (1.12 loaded, or even generated, any chunk that a
 * client asked for). */
public class ZonePlannerMapDataServer extends ZonePlannerMapData {
    public static final ZonePlannerMapDataServer INSTANCE = new ZonePlannerMapDataServer();

    private ZonePlannerMapDataServer() {
        super(10);
    }

    @Override
    @Nullable
    protected ZonePlannerMapChunk loadChunk(Level level, ZonePlannerMapChunkKey key) {
        if (!level.dimension().equals(key.dimension()) || !level.hasChunk(key.chunkPos().x, key.chunkPos().z)) {
            return null;
        }
        return new ZonePlannerMapChunk(level, key);
    }
}
