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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.gui.ContainerBC_Neptune;

/** A message for the open menu, sent either way. Replaces 1.12's {@code MessageContainer}. Dropped unless the
 * receiving player still has that exact menu open. */
public record ContainerPayload(int windowId, int msgId, byte[] data) implements CustomPacketPayload {
    public static final Type<ContainerPayload> TYPE = new Type<>(BuildCraftReborn.id("container"));

    public static final StreamCodec<FriendlyByteBuf, ContainerPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ContainerPayload::windowId,
        ByteBufCodecs.VAR_INT, ContainerPayload::msgId,
        ByteBufCodecs.byteArray(TileUpdatePayload.MAX_SIZE), ContainerPayload::data,
        ContainerPayload::new);

    @Override
    public Type<ContainerPayload> type() {
        return TYPE;
    }

    public static void handle(ContainerPayload message, IPayloadContext ctx) {
        Player player = ctx.player();
        if (player.containerMenu instanceof ContainerBC_Neptune container
            && player.containerMenu.containerId == message.windowId) {
            PacketBufferBC buffer =
                new PacketBufferBC(Unpooled.wrappedBuffer(message.data), player.level().registryAccess());
            try {
                container.readMessage(message.msgId, buffer, ctx.flow().getReceptionSide(), ctx);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                buffer.release();
            }
        }
    }
}
