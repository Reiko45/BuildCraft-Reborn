/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.marker.volume;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.Unpooled;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.net.PacketBufferBC;

/** Server to client: every volume box of the player's level (1.12's {@code MessageVolumeBoxes}). Each box is
 * written into its own byte array with the server's registries, and read with the client's. */
public record VolumeBoxesPayload(List<byte[]> boxes) implements CustomPacketPayload {
    public static final Type<VolumeBoxesPayload> TYPE = new Type<>(BuildCraftReborn.id("volume_boxes"));

    public static final StreamCodec<FriendlyByteBuf, VolumeBoxesPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BYTE_ARRAY.apply(ByteBufCodecs.list()), VolumeBoxesPayload::boxes,
        VolumeBoxesPayload::new);

    public static VolumeBoxesPayload of(List<VolumeBox> volumeBoxes, RegistryAccess registries) {
        List<byte[]> list = new ArrayList<>();
        for (VolumeBox box : volumeBoxes) {
            list.add(PacketBufferBC.write(registries, box::toBytes).toByteArray());
        }
        return new VolumeBoxesPayload(list);
    }

    @Override
    public Type<VolumeBoxesPayload> type() {
        return TYPE;
    }

    public static void handle(VolumeBoxesPayload message, IPayloadContext ctx) {
        Level level = ctx.player().level();
        RegistryAccess registries = level.registryAccess();
        Map<VolumeBox, byte[]> received = new LinkedHashMap<>();
        for (byte[] bytes : message.boxes) {
            try {
                received.put(new VolumeBox(level, new PacketBufferBC(Unpooled.wrappedBuffer(bytes), registries)),
                    bytes);
            } catch (IOException e) {
                BuildCraftReborn.LOGGER.warn("[core.volume] Failed to read a volume box", e);
            }
        }
        List<VolumeBox> clientBoxes = ClientVolumeBoxes.INSTANCE.volumeBoxes;
        clientBoxes.removeIf(volumeBox -> !received.containsKey(volumeBox));
        for (Map.Entry<VolumeBox, byte[]> entry : received.entrySet()) {
            boolean wasContained = false;
            for (VolumeBox clientVolumeBox : clientBoxes) {
                if (clientVolumeBox.equals(entry.getKey())) {
                    try {
                        clientVolumeBox.level = level;
                        clientVolumeBox.fromBytes(
                            new PacketBufferBC(Unpooled.wrappedBuffer(entry.getValue()), registries));
                    } catch (IOException io) {
                        BuildCraftReborn.LOGGER.warn("[core.volume] Failed to update a volume box", io);
                    }
                    wasContained = true;
                    break;
                }
            }
            if (!wasContained) {
                clientBoxes.add(entry.getKey());
                for (Addon addon : entry.getKey().addons.values()) {
                    if (addon != null) {
                        addon.onAdded();
                    }
                }
            }
        }
    }
}
