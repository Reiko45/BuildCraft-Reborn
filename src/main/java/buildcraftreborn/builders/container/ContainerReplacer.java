/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.tile.TileReplacer;
import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;

public class ContainerReplacer extends ContainerBCTile<TileReplacer> {
    public ContainerReplacer(int windowId, Player player, TileReplacer tile) {
        super(BCBuildersBlocks.REPLACER_MENU.get(), windowId, player, tile);
        addSlot(new SlotBase(tile.invSnapshot, 0, 8, 115));
        addSlot(new SlotBase(tile.invSchematicFrom, 0, 8, 137));
        addSlot(new SlotBase(tile.invSchematicTo, 0, 56, 137));
        addFullPlayerInventory(159);
    }

    public static ContainerReplacer fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerReplacer(windowId, inventory.player, readTile(inventory, buf, TileReplacer.class));
    }
}
