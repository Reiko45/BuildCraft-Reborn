/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.net;

import java.io.IOException;

import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;

/** A raw block entity payload, sent either way. Replaces 1.12's {@code MessageUpdateTile}: the bytes are written by
 * {@code TileBC_Neptune.writePayload} and read by {@code readPayload} on the other side, starting with a short message
 * ID. */
public record TileUpdatePayload(BlockPos pos, byte[] data) implements CustomPacketPayload {
    public static final Type<TileUpdatePayload> TYPE = new Type<>(BuildCraftReborn.id("tile_update"));

    /** Payloads larger than this are refused (same limit as 1.12). */
    public static final int MAX_SIZE = 1 << 24;

    public static final StreamCodec<FriendlyByteBuf, TileUpdatePayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, TileUpdatePayload::pos,
        ByteBufCodecs.byteArray(MAX_SIZE), TileUpdatePayload::data,
        TileUpdatePayload::new);

    /** Server side, payloads for blocks further away than this are dropped. */
    private static final double MAX_SERVER_DISTANCE_SQ = 64 * 64;

    public TileUpdatePayload {
        if (data.length > MAX_SIZE) {
            throw new IllegalStateException("Can't write out " + data.length + " bytes!");
        }
    }

    @Override
    public Type<TileUpdatePayload> type() {
        return TYPE;
    }

    /** Runs on the main thread of whichever side received the payload. */
    public static void handle(TileUpdatePayload message, IPayloadContext ctx) {
        Player player = ctx.player();
        Level level = player.level();
        if (ctx.flow().isServerbound()) {
            // Never let a client load chunks or poke at far-away machines.
            if (!level.isLoaded(message.pos) || player.distanceToSqr(message.pos.getCenter()) > MAX_SERVER_DISTANCE_SQ) {
                return;
            }
        }
        BlockEntity tile = level.getBlockEntity(message.pos);
        if (tile instanceof IPayloadReceiver receiver) {
            PacketBufferBC buffer = new PacketBufferBC(Unpooled.wrappedBuffer(message.data), level.registryAccess());
            try {
                receiver.receivePayload(ctx, buffer);
            } catch (IOException io) {
                throw new RuntimeException(io);
            } finally {
                buffer.release();
            }
        } else {
            BuildCraftReborn.LOGGER.warn("Dropped message for player {} for tile at {} (found {})",
                player.getName().getString(), message.pos, tile);
        }
    }
}
