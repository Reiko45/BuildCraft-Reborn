/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.silicon.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;
import buildcraftreborn.lib.gui.slot.SlotOutput;
import buildcraftreborn.silicon.BCSiliconPackaging;
import buildcraftreborn.silicon.tile.TileStampingTable;

/** The stamping table: the input on the left, the result on the right, and the three leftover slots below it. */
public class ContainerStampingTable extends ContainerBCTile<TileStampingTable> {
    public ContainerStampingTable(int windowId, Player player, TileStampingTable tile) {
        super(BCSiliconPackaging.STAMPING_TABLE_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(69);
        addSlot(new SlotBase(tile.invInput, 0, 15, 18));
        addSlot(new SlotOutput(tile.invOutput, 0, 143, 18));
        addSlot(new SlotOutput(tile.invOutput, 1, 111, 45));
        addSlot(new SlotOutput(tile.invOutput, 2, 129, 45));
        addSlot(new SlotOutput(tile.invOutput, 3, 147, 45));
    }

    public static ContainerStampingTable fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerStampingTable(windowId, inventory.player,
            readTile(inventory, buf, TileStampingTable.class));
    }
}
