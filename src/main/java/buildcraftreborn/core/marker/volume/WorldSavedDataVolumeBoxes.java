/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.marker.volume;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.network.PacketDistributor;

/** The volume boxes of one level, saved with it and sent to its players whenever they change. */
public class WorldSavedDataVolumeBoxes extends SavedData {
    private static final String DATA_NAME = "buildcraft_volume_boxes";
    public static final SavedData.Factory<WorldSavedDataVolumeBoxes> FACTORY =
        new SavedData.Factory<>(WorldSavedDataVolumeBoxes::new, WorldSavedDataVolumeBoxes::load);

    public ServerLevel level;
    public final List<VolumeBox> volumeBoxes = new ArrayList<>();

    public WorldSavedDataVolumeBoxes() {}

    @Nullable
    public VolumeBox getVolumeBoxAt(BlockPos pos) {
        return volumeBoxes.stream().filter(volumeBox -> volumeBox.box.contains(pos)).findFirst().orElse(null);
    }

    public VolumeBox addVolumeBox(BlockPos pos) {
        VolumeBox box = new VolumeBox(level, pos);
        volumeBoxes.add(box);
        return box;
    }

    @Nullable
    public VolumeBox getVolumeBoxFromId(UUID id) {
        return volumeBoxes.stream().filter(volumeBox -> volumeBox.id.equals(id)).findFirst().orElse(null);
    }

    @Nullable
    public VolumeBox getCurrentEditing(Player player) {
        return volumeBoxes.stream().filter(volumeBox -> volumeBox.isEditingBy(player)).findFirst().orElse(null);
    }

    public void tick() {
        boolean dirty = false;
        for (VolumeBox volumeBox : volumeBoxes) {
            if (!volumeBox.isEditing()) {
                continue;
            }
            Player player = volumeBox.getPlayer(level);
            if (player == null) {
                volumeBox.pauseEditing();
                dirty = true;
            } else {
                AABB oldAabb = volumeBox.box.getBoundingBox();
                volumeBox.box.reset();
                volumeBox.box.extendToEncompass(volumeBox.getHeld());
                BlockPos lookingAt = BlockPos.containing(player.getEyePosition()
                    .add(player.getLookAngle().scale(volumeBox.getDist())));
                volumeBox.box.extendToEncompass(lookingAt);
                if (!volumeBox.box.getBoundingBox().equals(oldAabb)) {
                    dirty = true;
                }
            }
        }
        for (VolumeBox volumeBox : volumeBoxes) {
            if (volumeBox.locks.removeIf(lock -> !lock.cause.stillWorks(level))) {
                dirty = true;
            }
        }
        if (dirty) {
            setDirty();
        }
    }

    /** Saves and sends the boxes to every player in the level (1.12's {@code markDirty}). */
    @Override
    public void setDirty() {
        super.setDirty();
        if (level != null) {
            PacketDistributor.sendToPlayersInDimension(level, VolumeBoxesPayload.of(volumeBoxes, level.registryAccess()));
        }
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (VolumeBox box : volumeBoxes) {
            list.add(box.writeToNBT(registries));
        }
        nbt.put("volumeBoxes", list);
        return nbt;
    }

    private static WorldSavedDataVolumeBoxes load(CompoundTag nbt, HolderLookup.Provider registries) {
        WorldSavedDataVolumeBoxes data = new WorldSavedDataVolumeBoxes();
        ListTag list = nbt.getList("volumeBoxes", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            data.volumeBoxes.add(new VolumeBox(null, list.getCompound(i), registries));
        }
        return data;
    }

    public static WorldSavedDataVolumeBoxes get(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalArgumentException("Tried to create a world saved data instance on the client!");
        }
        WorldSavedDataVolumeBoxes instance = serverLevel.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
        if (instance.level != serverLevel) {
            instance.level = serverLevel;
            for (VolumeBox box : instance.volumeBoxes) {
                box.level = serverLevel;
            }
        }
        return instance;
    }
}
