/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.tile;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.tiles.ITileAreaProvider;
import buildcraftreborn.api.tiles.TilesAPI;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.core.marker.VolumeCache;
import buildcraftreborn.core.marker.VolumeConnection;
import buildcraftreborn.lib.marker.MarkerSubCache;
import buildcraftreborn.lib.misc.PositionUtil;
import buildcraftreborn.lib.misc.data.Box;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.TileMarker;

/** A volume marker ("Land Mark"). Markers in a line connect, and a box of them marks out an area for quarries and
 * builders. With a redstone signal it shows lasers along each axis it could still connect along. */
public class TileMarkerVolume extends TileMarker<VolumeConnection> implements ITileAreaProvider {
    public static final IdAllocator IDS = TileMarker.IDS.makeChild("marker_volume");
    public static final int NET_SIGNALS_ON = IDS.allocId("SIGNALS_ON");
    public static final int NET_SIGNALS_OFF = IDS.allocId("SIGNALS_OFF");

    private boolean showSignals = false;

    public TileMarkerVolume(BlockPos pos, BlockState state) {
        super(BCCoreBlocks.MARKER_VOLUME_TILE.get(), pos, state);
        caps.addCapabilityInstance(TilesAPI.CAP_TILE_AREA_PROVIDER, this, EnumPipePart.VALUES);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public boolean isShowingSignals() {
        return showSignals;
    }

    @Override
    public VolumeCache getCache() {
        return VolumeCache.INSTANCE;
    }

    @Override
    public boolean isActiveForRender() {
        return showSignals || getCurrentConnection() != null;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putBoolean("showSignals", showSignals);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        showSignals = nbt.getBoolean("showSignals");
    }

    public void switchSignals() {
        if (level != null && !level.isClientSide) {
            showSignals = !showSignals;
            setChanged();
            sendNetworkUpdate(showSignals ? NET_SIGNALS_ON : NET_SIGNALS_OFF);
        }
    }

    private void readNewSignalState(boolean shouldShow) {
        boolean before = isActiveForRender();
        showSignals = shouldShow;
        if (before != isActiveForRender()) {
            redrawBlock();
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeBoolean(showSignals);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_SIGNALS_ON) {
                readNewSignalState(true);
            } else if (id == NET_SIGNALS_OFF) {
                readNewSignalState(false);
            } else if (id == NET_RENDER_DATA) {
                readNewSignalState(buffer.readBoolean());
            }
        }
    }

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        if (level != null && !level.isClientSide) {
            onManualConnectionAttempt(player);
        }
        return true;
    }

    public void onManualConnectionAttempt(Player player) {
        MarkerSubCache<VolumeConnection> cache = this.getLocalCache();
        for (BlockPos other : cache.getValidConnections(getBlockPos())) {
            cache.tryConnect(getBlockPos(), other);
        }
        VolumeConnection c = getCurrentConnection();
        if (c != null) {
            for (BlockPos corner : PositionUtil.getCorners(c.getBox().min(), c.getBox().max())) {
                if (!c.getMarkerPositions().contains(corner) && cache.hasLoadedOrUnloadedMarker(corner)) {
                    c.addMarker(corner);
                }
            }
        }
    }

    @Override
    public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (level == null || level.isClientSide) {
            return;
        }
        // Check if we are the corner of an existing box
        MarkerSubCache<VolumeConnection> cache = this.getLocalCache();
        for (BlockPos other : cache.getValidConnections(getBlockPos())) {
            VolumeConnection c = cache.getConnection(other);
            if (c != null && c.getBox().isCorner(getBlockPos())) {
                if (c.addMarker(getBlockPos())) {
                    // In theory we can't be the corner for multiple boxes
                    break;
                }
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("Min = " + min());
        left.add("Max = " + max());
        left.add("Signals = " + showSignals);
    }

    // ITileAreaProvider

    @Override
    public BlockPos min() {
        VolumeConnection connection = getCurrentConnection();
        return connection == null ? getBlockPos() : connection.getBox().min();
    }

    @Override
    public BlockPos max() {
        VolumeConnection connection = getCurrentConnection();
        return connection == null ? getBlockPos() : connection.getBox().max();
    }

    @Override
    public void removeFromWorld() {
        if (level == null || level.isClientSide) {
            return;
        }
        VolumeConnection connection = getCurrentConnection();
        if (connection != null) {
            // Copy the list over because the iterator doesn't like it if you change the connection while using it
            List<BlockPos> allPositions = ImmutableList.copyOf(connection.getMarkerPositions());
            for (BlockPos p : allPositions) {
                level.destroyBlock(p, true);
            }
        }
    }

    @Override
    public boolean isValidFromLocation(BlockPos pos) {
        VolumeConnection connection = getCurrentConnection();
        if (connection == null) {
            return false;
        }
        Box box = connection.getBox();
        if (box.contains(pos)) {
            return false;
        }
        for (BlockPos p : PositionUtil.getCorners(box.min(), box.max())) {
            if (PositionUtil.isNextTo(p, pos)) {
                return true;
            }
        }
        return false;
    }
}
