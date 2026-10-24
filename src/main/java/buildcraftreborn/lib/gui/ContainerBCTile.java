/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraftreborn.lib.tile.TileBC_Neptune;

/** A menu for a single tile. Tells the tile who is looking (so it can send GUI updates) and sends
 * {@link TileBC_Neptune#NET_GUI_TICK} every tick. */
public abstract class ContainerBCTile<T extends TileBC_Neptune> extends ContainerBC_Neptune {
    public final T tile;

    public ContainerBCTile(@Nullable MenuType<?> type, int windowId, Player player, T tile) {
        super(type, windowId, player);
        this.tile = tile;
        if (!player.level().isClientSide) {
            tile.onPlayerOpen(player);
        }
    }

    /** Reads the block position written by {@code BlockBCTile_Neptune.openGui} and finds the client's copy of the tile.
     *
     * @throws IllegalStateException if the tile is missing or of the wrong type (the client and server disagree) */
    public static <T extends TileBC_Neptune> T readTile(Inventory inventory, RegistryFriendlyByteBuf buf,
        Class<T> type) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity tile = inventory.player.level().getBlockEntity(pos);
        if (type.isInstance(tile)) {
            return type.cast(tile);
        }
        throw new IllegalStateException("Expected a " + type.getSimpleName() + " at " + pos + ", found " + tile);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        tile.onPlayerClose(player);
    }

    @Override
    public final boolean stillValid(Player player) {
        return tile.canInteractWith(player);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        tile.sendNetworkGuiTick(this.player);
    }
}
