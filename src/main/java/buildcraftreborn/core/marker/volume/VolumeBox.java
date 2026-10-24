/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.marker.volume;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.lib.misc.data.Box;
import buildcraftreborn.lib.net.PacketBufferBC;

/** A box in the world that isn't held by any block: made with the volume box item, resized with the marker connector,
 * and used by fillers, architect tables and addons such as the filler planner.
 * <p>
 * 1.21: the level is set after loading, as saved data is loaded without one. */
public class VolumeBox {
    public Level level;
    public UUID id;
    public Box box;
    private UUID player = null;
    private UUID oldPlayer = null;
    private BlockPos held = null;
    private double dist = 0;
    private BlockPos oldMin = null, oldMax = null;
    public final Map<EnumAddonSlot, Addon> addons = new EnumMap<>(EnumAddonSlot.class);
    public final List<Lock> locks = new ArrayList<>();

    public VolumeBox(Level level, BlockPos at) {
        this.level = level;
        id = UUID.randomUUID();
        box = new Box(at, at);
    }

    public VolumeBox(@Nullable Level level, CompoundTag nbt, HolderLookup.Provider registries) {
        this.level = level;
        id = nbt.getUUID("id");
        box = new Box();
        box.initialize(nbt.getCompound("box"));
        player = nbt.hasUUID("player") ? nbt.getUUID("player") : null;
        oldPlayer = nbt.hasUUID("oldPlayer") ? nbt.getUUID("oldPlayer") : null;
        held = NbtUtils.readBlockPos(nbt, "held").orElse(null);
        dist = nbt.getDouble("dist");
        oldMin = NbtUtils.readBlockPos(nbt, "oldMin").orElse(null);
        oldMax = NbtUtils.readBlockPos(nbt, "oldMax").orElse(null);
        ListTag addonList = nbt.getList("addons", Tag.TAG_COMPOUND);
        for (int i = 0; i < addonList.size(); i++) {
            CompoundTag addonsEntryTag = addonList.getCompound(i);
            ResourceLocation name = ResourceLocation.tryParse(addonsEntryTag.getString("addonClass"));
            EnumAddonSlot slot = NBTUtilBC.readEnum(addonsEntryTag.get("slot"), EnumAddonSlot.class);
            Addon addon = name == null ? null : AddonsRegistry.INSTANCE.create(name);
            if (addon == null || slot == null) {
                BuildCraftReborn.LOGGER.warn("[core.volume] Dropping unknown addon {} in slot {}", name, slot);
                continue;
            }
            addon.volumeBox = this;
            addon.readFromNBT(addonsEntryTag.getCompound("addonData"), registries);
            addons.put(slot, addon);
            addon.postReadFromNbt();
        }
        ListTag lockList = nbt.getList("locks", Tag.TAG_COMPOUND);
        for (int i = 0; i < lockList.size(); i++) {
            Lock lock = new Lock();
            if (lock.readFromNBT(lockList.getCompound(i))) {
                locks.add(lock);
            }
        }
    }

    public VolumeBox(Level level, PacketBufferBC buf) throws IOException {
        this.level = level;
        fromBytes(buf);
    }

    public boolean isEditing() {
        return player != null;
    }

    private void resetEditing() {
        oldMin = oldMax = null;
        held = null;
        dist = 0;
    }

    public void cancelEditing() {
        player = null;
        box.reset();
        box.extendToEncompass(oldMin);
        box.extendToEncompass(oldMax);
        resetEditing();
    }

    public void confirmEditing() {
        player = null;
        resetEditing();
        addons.values().forEach(Addon::onVolumeBoxSizeChange);
    }

    public void pauseEditing() {
        oldPlayer = player;
        player = null;
    }

    public void resumeEditing() {
        player = oldPlayer;
        oldPlayer = null;
    }

    public void setPlayer(Player player) {
        this.player = player.getGameProfile().getId();
    }

    public boolean isEditingBy(@Nullable Player player) {
        return player != null && Objects.equals(this.player, player.getGameProfile().getId());
    }

    public boolean isPausedEditingBy(Player player) {
        return oldPlayer != null && Objects.equals(oldPlayer, player.getGameProfile().getId());
    }

    @Nullable
    public Player getPlayer(Level level) {
        return player == null ? null : level.getPlayerByUUID(player);
    }

    public void setHeldDistOldMinOldMax(BlockPos held, double dist, BlockPos oldMin, BlockPos oldMax) {
        this.held = held;
        this.dist = dist;
        this.oldMin = oldMin;
        this.oldMax = oldMax;
    }

    public BlockPos getHeld() {
        return held;
    }

    public double getDist() {
        return dist;
    }

    public Stream<Lock.Target> getLockTargetsStream() {
        return locks.stream().flatMap(lock -> lock.targets.stream());
    }

    public CompoundTag writeToNBT(HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("id", id);
        nbt.put("box", this.box.writeToNBT());
        if (player != null) {
            nbt.putUUID("player", player);
        }
        if (oldPlayer != null) {
            nbt.putUUID("oldPlayer", oldPlayer);
        }
        if (held != null) {
            nbt.put("held", NbtUtils.writeBlockPos(held));
        }
        nbt.putDouble("dist", dist);
        if (oldMin != null) {
            nbt.put("oldMin", NbtUtils.writeBlockPos(oldMin));
        }
        if (oldMax != null) {
            nbt.put("oldMax", NbtUtils.writeBlockPos(oldMax));
        }
        ListTag addonList = new ListTag();
        addons.forEach((slot, addon) -> {
            CompoundTag addonsEntryTag = new CompoundTag();
            addonsEntryTag.put("slot", NBTUtilBC.writeEnum(slot));
            addonsEntryTag.putString("addonClass", AddonsRegistry.INSTANCE.getNameByClass(addon.getClass()).toString());
            addonsEntryTag.put("addonData", addon.writeToNBT(new CompoundTag(), registries));
            addonList.add(addonsEntryTag);
        });
        nbt.put("addons", addonList);
        ListTag lockList = new ListTag();
        locks.forEach(lock -> lockList.add(lock.writeToNBT()));
        nbt.put("locks", lockList);
        return nbt;
    }

    public void toBytes(PacketBufferBC buf) {
        buf.writeUUID(id);
        box.writeData(buf);
        buf.writeBoolean(player != null);
        if (player != null) {
            buf.writeUUID(player);
        }
        buf.writeInt(addons.size());
        addons.forEach((slot, addon) -> {
            buf.writeEnum(slot);
            buf.writeUtf(AddonsRegistry.INSTANCE.getNameByClass(addon.getClass()).toString());
            addon.toBytes(buf);
        });
        buf.writeInt(locks.size());
        locks.forEach(lock -> lock.toBytes(buf));
    }

    public void fromBytes(PacketBufferBC buf) throws IOException {
        id = buf.readUUID();
        box = new Box();
        box.readData(buf);
        player = buf.readBoolean() ? buf.readUUID() : null;
        Map<EnumAddonSlot, Addon> newAddons = new EnumMap<>(EnumAddonSlot.class);
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            EnumAddonSlot slot = buf.readEnum(EnumAddonSlot.class);
            ResourceLocation rl = ResourceLocation.parse(buf.readUtf(1024));
            Addon addon = AddonsRegistry.INSTANCE.create(rl);
            if (addon == null) {
                throw new IOException("Unknown addon class " + rl);
            }
            addon.volumeBox = this;
            addon.onAdded();
            addon.fromBytes(buf);
            newAddons.put(slot, addon);
        }
        addons.keySet().removeIf(slot -> !newAddons.containsKey(slot));
        newAddons.entrySet().stream().filter(slotAddon -> !addons.containsKey(slotAddon.getKey()))
            .forEach(slotAddon -> addons.put(slotAddon.getKey(), slotAddon.getValue()));
        for (Map.Entry<EnumAddonSlot, Addon> slotAddon : newAddons.entrySet()) {
            Addon existing = addons.get(slotAddon.getKey());
            if (existing != slotAddon.getValue()) {
                PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer(), buf.registryAccess());
                slotAddon.getValue().toBytes(buffer);
                existing.fromBytes(buffer);
            }
        }
        locks.clear();
        int lockCount = buf.readInt();
        for (int i = 0; i < lockCount; i++) {
            Lock lock = new Lock();
            lock.fromBytes(buf);
            locks.add(lock);
        }
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && getClass() == o.getClass() && id.equals(((VolumeBox) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
