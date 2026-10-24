/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.marker.volume;

import java.io.IOException;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import buildcraftreborn.lib.net.PacketBufferBC;

/** Something attached to a corner of a volume box (the filler planner, for example).
 * <p>
 * 1.21: how an addon is drawn is registered on the client ({@code AddonRenderers}) instead of being returned by the
 * addon. */
public abstract class Addon {
    public VolumeBox volumeBox;

    public EnumAddonSlot getSlot() {
        return volumeBox.addons.entrySet().stream()
            .filter(slotAddon -> slotAddon.getValue() == this)
            .findFirst()
            .orElseThrow(IllegalStateException::new)
            .getKey();
    }

    public AABB getBoundingBox() {
        return getSlot().getBoundingBox(volumeBox);
    }

    public boolean canBePlaceInto(VolumeBox volumeBox) {
        return !(this instanceof ISingleAddon
            && volumeBox.addons.values().stream().anyMatch(addon -> addon.getClass() == getClass()));
    }

    public void onAdded() {}

    public void onRemoved() {}

    public void onVolumeBoxSizeChange() {}

    public void onPlayerRightClick(Player player) {}

    public abstract CompoundTag writeToNBT(CompoundTag nbt, HolderLookup.Provider registries);

    public abstract void readFromNBT(CompoundTag nbt, HolderLookup.Provider registries);

    public void postReadFromNbt() {}

    public abstract void toBytes(PacketBufferBC buf);

    public abstract void fromBytes(PacketBufferBC buf) throws IOException;
}
