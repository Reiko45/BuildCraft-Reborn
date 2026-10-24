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

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;

/** Server to client: a map chunk for the zone planner (1.12's {@code MessageZoneMapResponse}). */
public record ZoneMapResponsePayload(ZonePlannerMapChunkKey key, ZonePlannerMapChunk chunk)
    implements CustomPacketPayload {
    public static final Type<ZoneMapResponsePayload> TYPE = new Type<>(BuildCraftReborn.id("zone_map_response"));

    public static final StreamCodec<FriendlyByteBuf, ZoneMapResponsePayload> STREAM_CODEC = StreamCodec.composite(
        ZonePlannerMapChunkKey.STREAM_CODEC, ZoneMapResponsePayload::key,
        ZonePlannerMapChunk.STREAM_CODEC, ZoneMapResponsePayload::chunk,
        ZoneMapResponsePayload::new);

    @Override
    public Type<ZoneMapResponsePayload> type() {
        return TYPE;
    }

    public static void handle(ZoneMapResponsePayload message, IPayloadContext ctx) {
        ZonePlannerMapDataClient.INSTANCE.onChunkReceived(message.key, message.chunk);
    }
}
