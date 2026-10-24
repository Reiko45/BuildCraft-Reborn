/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.marker.volume;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** 1.12's {@code BCCoreEventDist}: ticks the volume boxes being edited, and sends a level's boxes to players joining
 * it. */
public final class VolumeBoxEvents {
    private VolumeBoxEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(VolumeBoxEvents::onLevelTick);
        NeoForge.EVENT_BUS.addListener(VolumeBoxEvents::onEntityJoinLevel);
    }

    private static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            WorldSavedDataVolumeBoxes.get(level).tick();
        }
    }

    private static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getLevel() instanceof ServerLevel level) {
            WorldSavedDataVolumeBoxes data = WorldSavedDataVolumeBoxes.get(level);
            // 1.12 delayed this so it also worked in single player; payloads are queued until the client is ready
            PacketDistributor.sendToPlayer(player, VolumeBoxesPayload.of(data.volumeBoxes, level.registryAccess()));
            data.volumeBoxes.stream()
                .filter(volumeBox -> volumeBox.isPausedEditingBy(player))
                .forEach(VolumeBox::resumeEditing);
        }
    }
}
