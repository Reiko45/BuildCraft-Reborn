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
import buildcraftreborn.energy.tile.TileEngineRF;
import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;

public class ContainerEngineRF extends ContainerBCTile<TileEngineRF> {
    public ContainerEngineRF(int windowId, Player player, TileEngineRF tile) {
        super(BCEnergyBlocks.ENGINE_RF_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(95);
        for (int slot = 0; slot < 4; slot++) {
            addSlot(new SlotBase(tile.invUpgrades, slot, 62 + 18 * slot, 44));
        }
    }

    public static ContainerEngineRF fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerEngineRF(windowId, inventory.player, readTile(inventory, buf, TileEngineRF.class));
    }
}
