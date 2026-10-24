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
import buildcraftreborn.factory.tile.TileChute;
import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;

public class ContainerChute extends ContainerBCTile<TileChute> {
    public ContainerChute(int windowId, Player player, TileChute tile) {
        super(BCFactoryBlocks.CHUTE_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(71);

        addSlot(new SlotBase(tile.inv, 0, 62, 18));
        addSlot(new SlotBase(tile.inv, 1, 80, 18));
        addSlot(new SlotBase(tile.inv, 2, 98, 18));
        addSlot(new SlotBase(tile.inv, 3, 80, 36));
    }

    public static ContainerChute fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerChute(windowId, inventory.player, readTile(inventory, buf, TileChute.class));
    }
}
