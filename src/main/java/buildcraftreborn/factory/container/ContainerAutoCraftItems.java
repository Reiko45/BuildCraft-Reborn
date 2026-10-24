/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.tile.TileAutoWorkbenchItems;
import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;
import buildcraftreborn.lib.gui.slot.SlotDisplay;
import buildcraftreborn.lib.gui.slot.SlotOutput;
import buildcraftreborn.lib.gui.slot.SlotPhantom;

public class ContainerAutoCraftItems extends ContainerBCTile<TileAutoWorkbenchItems> {
    public final SlotBase[] materialSlots;

    public ContainerAutoCraftItems(int windowId, Player player, TileAutoWorkbenchItems tile) {
        super(BCFactoryBlocks.AUTO_WORKBENCH_ITEMS_MENU.get(), windowId, player, tile);

        addSlot(new SlotOutput(tile.invResult, 0, 124, 35));

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new SlotPhantom(tile.invBlueprint, x + y * 3, 30 + x * 18, 17 + y * 18, false));
            }
        }

        materialSlots = new SlotBase[9];
        for (int x = 0; x < 9; x++) {
            // hide the filter slots, but still sync them
            addSlot(new SlotPhantom(tile.invMaterialFilter, x, -1000000, -1000000));
            addSlot(materialSlots[x] = new SlotBase(tile.invMaterials, x, 8 + x * 18, 84));
        }

        addSlot(new SlotDisplay(i -> tile.resultClient, 0, 93, 27));

        addFullPlayerInventory(115);
    }

    public static ContainerAutoCraftItems fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerAutoCraftItems(windowId, inventory.player,
            readTile(inventory, buf, TileAutoWorkbenchItems.class));
    }
}
