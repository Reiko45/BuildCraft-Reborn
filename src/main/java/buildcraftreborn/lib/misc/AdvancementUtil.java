/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import buildcraftreborn.BuildCraftReborn;

/** Grants BuildCraft's code-triggered advancements. Each one has a single {@code code_trigger} criterion using the
 * {@code minecraft:impossible} trigger, so it can only be granted from here. */
public class AdvancementUtil {
    private static final Set<ResourceLocation> UNKNOWN_ADVANCEMENTS = new HashSet<>();

    public static void unlockAdvancement(@Nullable Player player, ResourceLocation advancementName) {
        if (!(player instanceof ServerPlayer playerMP) || player instanceof FakePlayer) {
            return;
        }
        AdvancementHolder advancement = playerMP.server.getAdvancements().get(advancementName);
        if (advancement != null) {
            // never assume the advancement exists, we create them but they are removable by datapacks
            playerMP.getAdvancements().award(advancement, "code_trigger");
        } else if (UNKNOWN_ADVANCEMENTS.add(advancementName)) {
            BuildCraftReborn.LOGGER.warn("[lib.advancement] Attempted to trigger undefined advancement: {}",
                advancementName);
        }
    }

    /** @return true if the player was online (so the advancement could be granted). */
    public static boolean unlockAdvancement(@Nullable UUID player, ResourceLocation advancementName) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || player == null) {
            return false;
        }
        ServerPlayer online = server.getPlayerList().getPlayer(player);
        if (online != null) {
            unlockAdvancement(online, advancementName);
            return true;
        }
        return false;
    }
}
