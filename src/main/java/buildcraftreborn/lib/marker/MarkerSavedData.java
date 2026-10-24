/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.marker;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

/** Saves every marker position and connection of one level (1.12: per-world {@code WorldSavedData}, now in each
 * dimension's {@code data} folder). Markers in unloaded chunks still count, so connections survive chunk unloads. */
public abstract class MarkerSavedData<S extends MarkerSubCache<C>, C extends MarkerConnection<C>> extends SavedData {
    protected final List<BlockPos> markerPositions = new ArrayList<>();
    protected final List<List<BlockPos>> markerConnections = new ArrayList<>();
    private S subCache;

    protected void load(CompoundTag nbt) {
        markerPositions.clear();
        markerConnections.clear();
        ListTag positionList = nbt.getList("positions", Tag.TAG_INT_ARRAY);
        for (Tag tag : positionList) {
            NbtUtils.readBlockPos(wrap(tag), "p").ifPresent(markerPositions::add);
        }
        ListTag connectionList = nbt.getList("connections", Tag.TAG_LIST);
        for (Tag inner : connectionList) {
            List<BlockPos> list = new ArrayList<>();
            markerConnections.add(list);
            for (Tag tag : (ListTag) inner) {
                NbtUtils.readBlockPos(wrap(tag), "p").ifPresent(list::add);
            }
        }
    }

    private static CompoundTag wrap(Tag tag) {
        CompoundTag nbt = new CompoundTag();
        nbt.put("p", tag);
        return nbt;
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
        markerPositions.clear();
        markerConnections.clear();
        markerPositions.addAll(subCache.getAllMarkers());
        for (C connection : subCache.getConnections()) {
            markerConnections.add(new ArrayList<>(connection.getMarkerPositions()));
        }

        ListTag positionList = new ListTag();
        for (BlockPos p : markerPositions) {
            positionList.add(NbtUtils.writeBlockPos(p));
        }
        nbt.put("positions", positionList);

        ListTag connectionList = new ListTag();
        for (List<BlockPos> connection : markerConnections) {
            ListTag inner = new ListTag();
            for (BlockPos p : connection) {
                inner.add(NbtUtils.writeBlockPos(p));
            }
            connectionList.add(inner);
        }
        nbt.put("connections", connectionList);
        return nbt;
    }

    @Override
    public boolean isDirty() {
        // The cache changes without telling us, so always save its current state.
        return true;
    }

    public final void setCache(S subCache) {
        this.subCache = subCache;
    }
}
