/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.net.PacketBufferBC;

/** Client to server: asks for the snapshot with this key (1.12's {@code MessageSnapshotRequest}). */
public record SnapshotRequestPayload(byte[] key) implements CustomPacketPayload {
    public static final Type<SnapshotRequestPayload> TYPE = new Type<>(BuildCraftReborn.id("snapshot_request"));

    public static final StreamCodec<FriendlyByteBuf, SnapshotRequestPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BYTE_ARRAY, SnapshotRequestPayload::key, SnapshotRequestPayload::new);

    public static SnapshotRequestPayload of(Snapshot.Key key, net.minecraft.core.RegistryAccess registries) {
        return new SnapshotRequestPayload(PacketBufferBC.write(registries, key::writeToByteBuf).toByteArray());
    }

    @Override
    public Type<SnapshotRequestPayload> type() {
        return TYPE;
    }

    public static void handle(SnapshotRequestPayload message, IPayloadContext ctx) {
        Snapshot.Key key = new Snapshot.Key(
            new PacketBufferBC(Unpooled.wrappedBuffer(message.key), ctx.player().registryAccess()));
        Snapshot snapshot = GlobalSavedDataSnapshots.get(LogicalSide.SERVER).getSnapshot(key);
        if (snapshot != null) {
            ctx.reply(SnapshotResponsePayload.of(snapshot));
        }
    }
}
