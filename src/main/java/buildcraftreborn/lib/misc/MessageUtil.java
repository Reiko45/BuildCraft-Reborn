/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import java.util.Set;
import java.util.EnumSet;
import buildcraftreborn.lib.net.PacketBufferBC;

import java.util.Collection;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import buildcraftreborn.BuildCraftReborn;

/** Network helpers ported from 1.12's {@code MessageUtil}. Sending goes through NeoForge's {@link PacketDistributor}
 * rather than a {@code SimpleNetworkWrapper}.
 * <p>
 * Everything the server sends to clients goes through {@link #sendToPlayer}, which skips a player whose connection
 * doesn't have BuildCraft's channels: fake players other mods put in a level (with a connection that never negotiated
 * channels) and any client without the channel. NeoForge throws for those ("Payload ... may not be sent to the
 * client!"), which crashed the server when such a player joined a level, as BuildCraft sends volume boxes and markers
 * to every player that joins one. NeoForge's own {@link FakePlayer}s have no network channel at all, so they're
 * skipped before asking. */
public final class MessageUtil {
    private MessageUtil() {}

    /** Runs the task on the server thread a little later (after the current tick's work), for example to send a menu
     * its first data once the client has opened it. Does nothing if no server is running. */
    public static void doDelayedServer(Runnable task) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.tell(new TickTask(server.getTickCount(), task));
        }
    }

    /** Sends to one player, if its connection can take the payload (see the class comment).
     * @return Whether it was sent. */
    public static boolean sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        if (player instanceof FakePlayer || player.connection == null
            || player.connection.getConnection().channel() == null || !player.connection.hasChannel(payload)) {
            return false;
        }
        PacketDistributor.sendToPlayer(player, payload);
        return true;
    }

    /** Sends to every player that has the chunk containing {@code pos} loaded. Server side only. */
    public static void sendToAllWatching(Level level, BlockPos pos, CustomPacketPayload payload) {
        if (level instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false)) {
                sendToPlayer(player, payload);
            }
        }
    }

    /** Sends to every player in the level's dimension, as {@link PacketDistributor#sendToPlayersInDimension} does. */
    public static void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level().dimension() == level.dimension()) {
                sendToPlayer(player, payload);
            }
        }
    }

    public static void sendToPlayers(Collection<? extends Player> players, CustomPacketPayload payload) {
        for (Player player : players) {
            if (player instanceof ServerPlayer serverPlayer) {
                sendToPlayer(serverPlayer, payload);
            }
        }
    }

    public static void writeGameProfile(FriendlyByteBuf buffer, @Nullable GameProfile profile) {
        if (profile != null && profile.getId() != null && !profile.getName().isEmpty()) {
            buffer.writeBoolean(true);
            buffer.writeUUID(profile.getId());
            buffer.writeUtf(profile.getName());
        } else {
            buffer.writeBoolean(false);
        }
    }

    @Nullable
    public static GameProfile readGameProfile(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            UUID uuid = buffer.readUUID();
            String name = buffer.readUtf(256);
            return new GameProfile(uuid, name);
        }
        return null;
    }

    /** Checks that a payload was read completely, logging (or throwing, if {@code throwError}) when bytes are left. */
    public static void ensureEmpty(ByteBuf buf, boolean throwError, String extra) {
        int readableBytes = buf.readableBytes();
        if (readableBytes > 0) {
            String message = "Did not fully read the data! [" + extra + "] " + readableBytes + " bytes left over";
            if (throwError) {
                throw new IllegalStateException(message);
            }
            BuildCraftReborn.LOGGER.warn(message);
        }
    }

    /** Writes an enum value, or null. Read with {@link #readEnumOrNull(PacketBufferBC, Class)}. */
    public static void writeEnumOrNull(PacketBufferBC buffer, @Nullable Enum<?> value) {
        buffer.writeBoolean(value != null);
        if (value != null) {
            buffer.writeEnum(value);
        }
    }

    @Nullable
    public static <E extends Enum<E>> E readEnumOrNull(PacketBufferBC buffer, Class<E> clazz) {
        return buffer.readBoolean() ? buffer.readEnum(clazz) : null;
    }

    /** Writes one bit per constant of the enum: whether the set contains it. */
    /** Writes each boolean as a single bit (see {@link PacketBufferBC#writeBoolean}). */
    public static void writeBooleanArray(PacketBufferBC buf, boolean[] bool) {
        for (boolean b : bool) {
            buf.writeBoolean(b);
        }
    }

    public static boolean[] readBooleanArray(PacketBufferBC buf, int length) {
        boolean[] total = new boolean[length];
        readBooleanArray(buf, total);
        return total;
    }

    public static void readBooleanArray(PacketBufferBC buf, boolean[] into) {
        for (int i = 0; i < into.length; i++) {
            into[i] = buf.readBoolean();
        }
    }

    public static <E extends Enum<E>> void writeEnumSet(PacketBufferBC buf, Set<E> set, Class<E> clazz) {
        for (E e : clazz.getEnumConstants()) {
            buf.writeBoolean(set.contains(e));
        }
    }

    public static <E extends Enum<E>> EnumSet<E> readEnumSet(PacketBufferBC buf, Class<E> clazz) {
        EnumSet<E> set = EnumSet.noneOf(clazz);
        for (E e : clazz.getEnumConstants()) {
            if (buf.readBoolean()) {
                set.add(e);
            }
        }
        return set;
    }
}
