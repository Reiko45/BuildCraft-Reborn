/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import buildcraftreborn.BuildCraftReborn;

/** Fake players that act for a machine's owner (so protection mods see the owner breaking blocks).
 * <p>
 * 1.21: backed by NeoForge's {@link FakePlayerFactory}, which keeps one fake player per level and profile and drops
 * them when the level unloads (1.12 kept its own map and cleared it on world unload). */
public enum FakePlayerProvider {
    INSTANCE;

    /** The default {@link GameProfile} to use if a tile entity cannot determine its real owner. Most of the time this
     * shouldn't be necessary, as we should be able to get a {@link GameProfile} from all players that place or create
     * tiles/robots */
    public static final GameProfile NULL_PROFILE;

    static {
        UUID id = UUID.nameUUIDFromBytes("buildcraft.core".getBytes(StandardCharsets.UTF_8));
        NULL_PROFILE = new GameProfile(id, "[BuildCraft]");
    }

    /** @return A fake player that can be used IN THE CURRENT METHOD CONTEXT ONLY! */
    public FakePlayer getBuildCraftPlayer(ServerLevel world) {
        return getFakePlayer(world, NULL_PROFILE, BlockPos.ZERO);
    }

    /** @param profile The owner's profile.
     * @return A fake player that can be used IN THE CURRENT METHOD CONTEXT ONLY! */
    public FakePlayer getFakePlayer(ServerLevel world, @Nullable GameProfile profile) {
        return getFakePlayer(world, profile, BlockPos.ZERO);
    }

    /** @param profile The owner's profile.
     * @param pos Where the fake player should stand.
     * @return A fake player that can be used IN THE CURRENT METHOD CONTEXT ONLY! */
    public FakePlayer getFakePlayer(ServerLevel world, @Nullable GameProfile profile, BlockPos pos) {
        if (profile == null) {
            BuildCraftReborn.LOGGER.warn("[lib.fake] Null GameProfile! This is a bug!", new IllegalArgumentException());
            profile = NULL_PROFILE;
        }
        FakePlayer player = FakePlayerFactory.get(world, profile);
        player.setPos(pos.getX(), pos.getY(), pos.getZ());
        return player;
    }
}
