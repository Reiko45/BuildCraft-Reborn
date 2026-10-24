/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.container;

import java.util.ArrayList;

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
import buildcraftreborn.silicon.tile.TileAssemblyTable;

public class ContainerAssemblyTable extends ContainerBCTile<TileAssemblyTable> {
    public ContainerAssemblyTable(int windowId, Player player, TileAssemblyTable tile) {
        super(BCSiliconBlocks.ASSEMBLY_TABLE_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(123);

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new SlotBase(tile.inv, x + y * 3, 8 + x * 18, 36 + y * 18));
            }
        }

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new SlotDisplay(this::getDisplay, x + y * 3, 116 + x * 18, 36 + y * 18));
            }
        }
    }

    public static ContainerAssemblyTable fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerAssemblyTable(windowId, inventory.player,
            readTile(inventory, buf, TileAssemblyTable.class));
    }

    private ItemStack getDisplay(int index) {
        return index < tile.recipesStates.size()
            ? new ArrayList<>(tile.recipesStates.keySet()).get(index).output
            : ItemStack.EMPTY;
    }
}
