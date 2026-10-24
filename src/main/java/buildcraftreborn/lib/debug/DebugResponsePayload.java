/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.debug;

import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;

/** Server to client: the server side of a block's F3 debug info (1.12's {@code MessageDebugResponse}). */
public record DebugResponsePayload(List<String> left, List<String> right) implements CustomPacketPayload {
    public static final Type<DebugResponsePayload> TYPE = new Type<>(BuildCraftReborn.id("debug_response"));

    public static final StreamCodec<FriendlyByteBuf, DebugResponsePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(512)), DebugResponsePayload::left,
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(512)), DebugResponsePayload::right,
        DebugResponsePayload::new);

    @Override
    public Type<DebugResponsePayload> type() {
        return TYPE;
    }

    public static void handle(DebugResponsePayload message, IPayloadContext ctx) {
        ClientDebuggables.setServerInfo(message.left, message.right);
    }
}
