/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.tile;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.api.core.IPathProvider;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.core.marker.PathCache;
import buildcraftreborn.core.marker.PathConnection;
import buildcraftreborn.lib.tile.TileMarker;

/** A path marker ("Path Mark"). Connected with the marker connector into a path for builders; right click one to turn
 * its path around. */
public class TileMarkerPath extends TileMarker<PathConnection> implements IPathProvider {

    public TileMarkerPath(BlockPos pos, BlockState state) {
        super(BCCoreBlocks.MARKER_PATH_TILE.get(), pos, state);
    }

    @Override
    public ImmutableList<BlockPos> getPath() {
        PathConnection connection = getCurrentConnection();
        if (connection == null) {
            return ImmutableList.of();
        }
        return connection.getMarkerPositions();
    }

    @Override
    public void removeFromWorld() {
        if (level == null) {
            return;
        }
        for (BlockPos pos : getPath()) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public PathCache getCache() {
        return PathCache.INSTANCE;
    }

    @Override
    public boolean isActiveForRender() {
        PathConnection connection = getCurrentConnection();
        return connection != null;
    }

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        reverseDirection();
        return true;
    }

    public void reverseDirection() {
        if (level == null || level.isClientSide) {
            return;
        }
        PathConnection connection = getCurrentConnection();
        if (connection == null) {
            return;
        }
        connection.reverseDirection();
    }
}
