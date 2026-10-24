/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.wire;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;

/** Server to client: the wire systems (just their wire parts) near the player, by network id (1.12's
 * {@code MessageWireSystems}). */
public record WireSystemsPayload(Map<Integer, List<WireSystem.WireElement>> systems) implements CustomPacketPayload {
    public static final Type<WireSystemsPayload> TYPE = new Type<>(BuildCraftReborn.id("wire_systems"));

    public static final StreamCodec<FriendlyByteBuf, WireSystemsPayload> STREAM_CODEC =
        StreamCodec.of(WireSystemsPayload::write, WireSystemsPayload::read);

    public static WireSystemsPayload of(Map<Integer, WireSystem> wireSystems) {
        Map<Integer, List<WireSystem.WireElement>> map = new HashMap<>();
        wireSystems.forEach((id, system) -> map.put(id, system.elements.stream()
            .filter(element -> element.type == WireSystem.WireElement.Type.WIRE_PART).toList()));
        return new WireSystemsPayload(map);
    }

    private static void write(FriendlyByteBuf buf, WireSystemsPayload payload) {
        buf.writeInt(payload.systems.size());
        payload.systems.forEach((id, elements) -> {
            buf.writeInt(id);
            buf.writeInt(elements.size());
            elements.forEach(element -> element.toBytes(buf));
        });
    }

    private static WireSystemsPayload read(FriendlyByteBuf buf) {
        Map<Integer, List<WireSystem.WireElement>> map = new HashMap<>();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            int id = buf.readInt();
            int localCount = buf.readInt();
            ImmutableList.Builder<WireSystem.WireElement> elements = ImmutableList.builder();
            for (int j = 0; j < localCount; j++) {
                elements.add(new WireSystem.WireElement(buf));
            }
            map.put(id, elements.build());
        }
        return new WireSystemsPayload(map);
    }

    @Override
    public Type<WireSystemsPayload> type() {
        return TYPE;
    }

    public static void handle(WireSystemsPayload message, IPayloadContext ctx) {
        ClientWireSystems.INSTANCE.wireSystems.clear();
        message.systems.forEach((id, elements) -> ClientWireSystems.INSTANCE.wireSystems.put(id,
            new WireSystem(id, ImmutableList.copyOf(elements), null)));
    }
}
