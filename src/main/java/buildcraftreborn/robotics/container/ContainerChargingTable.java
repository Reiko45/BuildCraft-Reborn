/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.robotics.tile.TileChargingTable;

public class ContainerChargingTable extends ContainerBCTile<TileChargingTable> {
    public ContainerChargingTable(int windowId, Player player, TileChargingTable tile) {
        super(BCRoboticsBlocks.CHARGING_TABLE_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(50);
        addSlot(new SlotBase(tile.inv, 0, 80, 18));
    }

    public static ContainerChargingTable fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerChargingTable(windowId, inventory.player,
            readTile(inventory, buf, TileChargingTable.class));
    }
}
