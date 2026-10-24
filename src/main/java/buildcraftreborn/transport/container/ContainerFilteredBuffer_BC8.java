/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;
import buildcraftreborn.lib.gui.slot.SlotPhantom;
import buildcraftreborn.transport.BCTransportBlocks;
import buildcraftreborn.transport.tile.TileFilteredBuffer;

/** The filtered buffer: a row of filter (phantom) slots above the matching storage slots. The empty filter slot
 * background is drawn by the screen. */
public class ContainerFilteredBuffer_BC8 extends ContainerBCTile<TileFilteredBuffer> {
    public ContainerFilteredBuffer_BC8(int windowId, Player player, TileFilteredBuffer tile) {
        super(BCTransportBlocks.FILTERED_BUFFER_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(86);

        for (int i = 0; i < 9; i++) {
            // Filter slots
            addSlot(new SlotPhantom(tile.invFilter, i, 8 + i * 18, 27, false));
            // Inventory slots
            addSlot(new SlotBase(tile.invMain, i, 8 + i * 18, 61));
        }
    }

    public static ContainerFilteredBuffer_BC8 fromNetwork(int windowId, Inventory inventory,
        RegistryFriendlyByteBuf buf) {
        return new ContainerFilteredBuffer_BC8(windowId, inventory.player,
            readTile(inventory, buf, TileFilteredBuffer.class));
    }
}
