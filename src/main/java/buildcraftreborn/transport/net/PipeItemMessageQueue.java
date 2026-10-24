/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.net;

import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.network.PacketDistributor;

/** Collects the travelling item updates of a tick, so each player gets one {@link MultiPipeItemPayload} per tick. */
public final class PipeItemMessageQueue {
    private static final Map<ServerPlayer, MultiPipeItemPayload> cachedPlayerPackets = new WeakHashMap<>();

    private PipeItemMessageQueue() {}

    /** Called at the end of every server tick. */
    public static void serverTick() {
        for (Entry<ServerPlayer, MultiPipeItemPayload> entry : cachedPlayerPackets.entrySet()) {
            PacketDistributor.sendToPlayer(entry.getKey(), entry.getValue());
        }
        cachedPlayerPackets.clear();
    }

    public static void appendTravellingItem(Level level, BlockPos pos, MultiPipeItemPayload.TravellingItemData data) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        for (ServerPlayer player : server.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false)) {
            cachedPlayerPackets.computeIfAbsent(player, pl -> new MultiPipeItemPayload()).append(pos, data);
        }
    }
}
