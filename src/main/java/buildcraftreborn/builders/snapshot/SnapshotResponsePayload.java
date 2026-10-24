/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;

/** Server to client: a snapshot the client asked for, as gzipped NBT (1.12's {@code MessageSnapshotResponse}). */
public record SnapshotResponsePayload(byte[] data) implements CustomPacketPayload {
    public static final Type<SnapshotResponsePayload> TYPE = new Type<>(BuildCraftReborn.id("snapshot_response"));

    /** Large blueprints need room: 32 MiB, well under the 1 MiB-per-part split limit times NeoForge's part count. */
    public static final StreamCodec<FriendlyByteBuf, SnapshotResponsePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.byteArray(32 * 1024 * 1024), SnapshotResponsePayload::data, SnapshotResponsePayload::new);

    public static SnapshotResponsePayload of(Snapshot snapshot) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            NbtIo.writeCompressed(Snapshot.writeToNBT(snapshot), out);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return new SnapshotResponsePayload(out.toByteArray());
    }

    @Override
    public Type<SnapshotResponsePayload> type() {
        return TYPE;
    }

    public static void handle(SnapshotResponsePayload message, IPayloadContext ctx) {
        try {
            Snapshot snapshot = Snapshot.readFromNBT(
                NbtIo.readCompressed(new ByteArrayInputStream(message.data), NbtAccounter.unlimitedHeap()));
            ClientSnapshots.INSTANCE.onSnapshotReceived(snapshot);
        } catch (IOException e) {
            BuildCraftReborn.LOGGER.warn("[builders.snapshot] Failed to read a snapshot from the server", e);
        }
    }
}
