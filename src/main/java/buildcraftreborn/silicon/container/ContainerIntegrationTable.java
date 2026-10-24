/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;
import buildcraftreborn.lib.gui.slot.SlotDisplay;
import buildcraftreborn.lib.gui.slot.SlotOutput;
import buildcraftreborn.lib.gui.slot.SlotPhantom;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.tile.TileIntegrationTable;

public class ContainerIntegrationTable extends ContainerBCTile<TileIntegrationTable> {
    public ContainerIntegrationTable(int windowId, Player player, TileIntegrationTable tile) {
        super(BCSiliconBlocks.INTEGRATION_TABLE_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(109);

        int[] indexes = { 0, 1, 2, 3, 0, 4, 5, 6, 7 };

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new SlotBase((x == 1 && y == 1) ? tile.invTarget : tile.invToIntegrate,
                    indexes[x + y * 3], 19 + x * 25, 24 + y * 25));
            }
        }

        addSlot(new SlotDisplay(i -> tile.getOutput(), 0, 101, 36));

        addSlot(new SlotOutput(tile.invResult, 0, 138, 49));
    }

    public static ContainerIntegrationTable fromNetwork(int windowId, Inventory inventory,
        RegistryFriendlyByteBuf buf) {
        return new ContainerIntegrationTable(windowId, inventory.player,
            readTile(inventory, buf, TileIntegrationTable.class));
    }
}
