/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.tile;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.lib.marker.MarkerCache;
import buildcraftreborn.lib.marker.MarkerConnection;
import buildcraftreborn.lib.marker.MarkerSubCache;

/** A marker block's tile. It only tells its level's {@link MarkerSubCache} when it loads, unloads or is removed; the
 * cache holds the connections. */
public abstract class TileMarker<C extends MarkerConnection<C>> extends TileBC_Neptune implements IDebuggable {

    public TileMarker(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract MarkerCache<? extends MarkerSubCache<C>> getCache();

    public MarkerSubCache<C> getLocalCache() {
        return getCache().getSubCache(level);
    }

    /** @return True if this has lasers being emitted, or any other reason you want. */
    public abstract boolean isActiveForRender();

    @Nullable
    public C getCurrentConnection() {
        return getLocalCache().getConnection(getBlockPos());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        getLocalCache().loadMarker(getBlockPos(), this);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        getLocalCache().unloadMarker(getBlockPos());
    }

    @Override
    public void onRemove() {
        super.onRemove();
        getLocalCache().removeMarker(getBlockPos());
    }

    protected void disconnectFromOthers() {
        C currentConnection = getCurrentConnection();
        if (currentConnection != null) {
            currentConnection.removeMarker(getBlockPos());
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        C current = getCurrentConnection();
        MarkerSubCache<C> cache = getLocalCache();
        left.add("Exists = " + (cache.getMarker(getBlockPos()) == this));
        if (current == null) {
            left.add("Connection = null");
        } else {
            left.add("Connection:");
            current.getDebugInfo(getBlockPos(), left);
        }
    }
}
