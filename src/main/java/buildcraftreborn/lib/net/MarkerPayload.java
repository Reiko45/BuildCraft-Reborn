/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.net;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.marker.MarkerCache;

/** Server to client: markers or a connection were added or removed (1.12's {@code MessageMarker}). */
public record MarkerPayload(boolean add, boolean connection, int cacheId, List<BlockPos> positions)
    implements CustomPacketPayload {
    public static final Type<MarkerPayload> TYPE = new Type<>(BuildCraftReborn.id("marker"));

    public static final StreamCodec<FriendlyByteBuf, MarkerPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, MarkerPayload::add,
        ByteBufCodecs.BOOL, MarkerPayload::connection,
        ByteBufCodecs.VAR_INT, MarkerPayload::cacheId,
        BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), MarkerPayload::positions,
        MarkerPayload::new);

    public MarkerPayload {
        positions = List.copyOf(positions);
    }

    @Override
    public Type<MarkerPayload> type() {
        return TYPE;
    }

    public static void handle(MarkerPayload message, IPayloadContext ctx) {
        if (message.cacheId < 0 || message.cacheId >= MarkerCache.CACHES.size()) {
            if (MarkerCache.DEBUG) {
                BuildCraftReborn.LOGGER.warn("[lib.messages][marker] The cache ID {} was invalid!", message.cacheId);
            }
            return;
        }
        MarkerCache<?> cache = MarkerCache.CACHES.get(message.cacheId);
        cache.getSubCache(ctx.player().level()).handleMessageMain(message);
    }
}
