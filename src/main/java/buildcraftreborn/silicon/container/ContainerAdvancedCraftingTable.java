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
import buildcraftreborn.silicon.tile.TileAdvancedCraftingTable;

public class ContainerAdvancedCraftingTable extends ContainerBCTile<TileAdvancedCraftingTable> {
    public ContainerAdvancedCraftingTable(int windowId, Player player, TileAdvancedCraftingTable tile) {
        super(BCSiliconBlocks.ADVANCED_CRAFTING_TABLE_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(153);

        addSlot(new SlotDisplay(i -> tile.resultClient, 0, 127, 33));

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 5; x++) {
                addSlot(new SlotBase(tile.invMaterials, x + y * 5, 15 + x * 18, 85 + y * 18));
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new SlotOutput(tile.invResults, x + y * 3, 109 + x * 18, 85 + y * 18));
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new SlotPhantom(tile.invBlueprint, x + y * 3, 33 + x * 18, 16 + y * 18, false));
            }
        }
    }

    public static ContainerAdvancedCraftingTable fromNetwork(int windowId, Inventory inventory,
        RegistryFriendlyByteBuf buf) {
        return new ContainerAdvancedCraftingTable(windowId, inventory.player,
            readTile(inventory, buf, TileAdvancedCraftingTable.class));
    }
}
