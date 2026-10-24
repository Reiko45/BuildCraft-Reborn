/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.tile.TileDynamoMJ;
import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;

public class ContainerDynamoMJ extends ContainerBCTile<TileDynamoMJ> {
    public ContainerDynamoMJ(int windowId, Player player, TileDynamoMJ tile) {
        super(BCEnergyBlocks.MJ_DYNAMO_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(95);
        for (int slot = 0; slot < 4; slot++) {
            addSlot(new SlotBase(tile.invUpgrades, slot, 44 + 18 * slot, 44));
        }
    }

    public static ContainerDynamoMJ fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerDynamoMJ(windowId, inventory.player, readTile(inventory, buf, TileDynamoMJ.class));
    }
}
