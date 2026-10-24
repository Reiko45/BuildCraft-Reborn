/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.wire;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.transport.pipe.IPipeHolder;

/** Server to client: which wire systems (by network id) are powered (1.12's {@code MessageWireSystemsPowered}). */
public record WireSystemsPoweredPayload(Map<Integer, Boolean> powered) implements CustomPacketPayload {
    public static final Type<WireSystemsPoweredPayload> TYPE = new Type<>(BuildCraftReborn.id("wire_systems_powered"));

    public static final StreamCodec<FriendlyByteBuf, WireSystemsPoweredPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.INT, ByteBufCodecs.BOOL), WireSystemsPoweredPayload::powered,
        WireSystemsPoweredPayload::new);

    @Override
    public Type<WireSystemsPoweredPayload> type() {
        return TYPE;
    }

    public static void handle(WireSystemsPoweredPayload message, IPayloadContext ctx) {
        Level level = ctx.player().level();
        message.powered.forEach((id, powered) -> {
            WireSystem system = ClientWireSystems.INSTANCE.wireSystems.get(id);
            if (system == null) {
                return;
            }
            for (WireSystem.WireElement element : system.elements) {
                if (element.type != WireSystem.WireElement.Type.WIRE_PART) {
                    continue;
                }
                BlockEntity tile = level.getBlockEntity(element.blockPos);
                if (tile instanceof IPipeHolder holder && holder.getWireManager() instanceof WireManager wires) {
                    if (wires.getColorOfPart(element.wirePart) != null) {
                        boolean changed = powered ? wires.poweredClient.add(element.wirePart)
                            : wires.poweredClient.remove(element.wirePart);
                        if (changed) {
                            holder.scheduleRenderUpdate();
                        }
                    }
                }
            }
        });
    }
}
