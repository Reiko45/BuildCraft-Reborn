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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.lib.item.ItemDebugger;

/** Client to server: as {@link DebugRequestPayload}, for an entity the player looks at (robots). */
public record DebugEntityRequestPayload(int entityId) implements CustomPacketPayload {
    public static final Type<DebugEntityRequestPayload> TYPE = new Type<>(BuildCraftReborn.id("debug_entity_request"));

    public static final StreamCodec<FriendlyByteBuf, DebugEntityRequestPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT
        .map(DebugEntityRequestPayload::new, DebugEntityRequestPayload::entityId).cast();

    private static final double MAX_DISTANCE_SQ = 64 * 64;

    @Override
    public Type<DebugEntityRequestPayload> type() {
        return TYPE;
    }

    public static void handle(DebugEntityRequestPayload message, IPayloadContext ctx) {
        Player player = ctx.player();
        if (!ItemDebugger.isShowDebugInfo(player)) {
            ctx.reply(new DebugResponsePayload(List.of(), List.of()));
            return;
        }
        Entity entity = player.level().getEntity(message.entityId);
        if (entity instanceof IDebuggable debuggable && player.distanceToSqr(entity) <= MAX_DISTANCE_SQ) {
            List<String> left = new ArrayList<>();
            List<String> right = new ArrayList<>();
            debuggable.getDebugInfo(left, right, null);
            ctx.reply(new DebugResponsePayload(left, right));
        }
    }
}
