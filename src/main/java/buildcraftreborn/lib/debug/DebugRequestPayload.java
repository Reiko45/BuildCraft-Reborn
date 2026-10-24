/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.debug;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.lib.item.ItemDebugger;

/** Client to server: asks for the server side of the F3 debug info of the block the player looks at (1.12's
 * {@code MessageDebugRequest}). Only answered for players who may see it (see
 * {@link ItemDebugger#isShowDebugInfo(Player)}), and (new in 1.21) only for blocks within reach of them. */
public record DebugRequestPayload(BlockPos pos, Direction side) implements CustomPacketPayload {
    public static final Type<DebugRequestPayload> TYPE = new Type<>(BuildCraftReborn.id("debug_request"));

    public static final StreamCodec<FriendlyByteBuf, DebugRequestPayload> STREAM_CODEC = StreamCodec.of(
        (buf, msg) -> {
            buf.writeBlockPos(msg.pos);
            buf.writeEnum(msg.side);
        }, buf -> new DebugRequestPayload(buf.readBlockPos(), buf.readEnum(Direction.class)));

    /** How far away (squared) a block may be for its debug info to be sent. */
    private static final double MAX_DISTANCE_SQ = 64 * 64;

    @Override
    public Type<DebugRequestPayload> type() {
        return TYPE;
    }

    public static void handle(DebugRequestPayload message, IPayloadContext ctx) {
        Player player = ctx.player();
        if (!ItemDebugger.isShowDebugInfo(player)) {
            ctx.reply(new DebugResponsePayload(List.of(), List.of()));
            return;
        }
        if (!player.level().isLoaded(message.pos)
            || player.distanceToSqr(message.pos.getCenter()) > MAX_DISTANCE_SQ) {
            return;
        }
        BlockEntity tile = player.level().getBlockEntity(message.pos);
        if (tile instanceof IDebuggable debuggable) {
            List<String> left = new ArrayList<>();
            List<String> right = new ArrayList<>();
            debuggable.getDebugInfo(left, right, message.side);
            ctx.reply(new DebugResponsePayload(left, right));
        }
    }
}
