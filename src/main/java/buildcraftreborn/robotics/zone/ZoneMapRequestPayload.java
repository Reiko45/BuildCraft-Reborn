/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.robotics.zone;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;

/** Client to server: asks for a map chunk for the zone planner (1.12's {@code MessageZoneMapRequest}).
 * <p>
 * 1.21: only answered for loaded chunks of the player's own dimension within {@link #MAX_CHUNK_DISTANCE} chunks of
 * them. */
public record ZoneMapRequestPayload(ZonePlannerMapChunkKey key) implements CustomPacketPayload {
    public static final Type<ZoneMapRequestPayload> TYPE = new Type<>(BuildCraftReborn.id("zone_map_request"));

    public static final StreamCodec<FriendlyByteBuf, ZoneMapRequestPayload> STREAM_CODEC =
        ZonePlannerMapChunkKey.STREAM_CODEC.map(ZoneMapRequestPayload::new, ZoneMapRequestPayload::key);

    /** How far (in chunks) from the player a map chunk may be. */
    public static final int MAX_CHUNK_DISTANCE = 32;

    @Override
    public Type<ZoneMapRequestPayload> type() {
        return TYPE;
    }

    public static void handle(ZoneMapRequestPayload message, IPayloadContext ctx) {
        Player player = ctx.player();
        if (!canSee(player, message.key)) {
            return;
        }
        ZonePlannerMapChunk chunk = ZonePlannerMapDataServer.INSTANCE.getChunk(player.level(), message.key);
        if (chunk != null) {
            ctx.reply(new ZoneMapResponsePayload(message.key, chunk));
        }
    }

    /** @return True if the player may be sent the map chunk. */
    public static boolean canSee(Player player, ZonePlannerMapChunkKey key) {
        if (!player.level().dimension().equals(key.dimension())) {
            return false;
        }
        ChunkPos playerChunk = player.chunkPosition();
        return playerChunk.getChessboardDistance(key.chunkPos()) <= MAX_CHUNK_DISTANCE;
    }
}
