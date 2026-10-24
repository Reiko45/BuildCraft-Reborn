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
import buildcraftreborn.energy.tile.TileEngineStone_BC8;
import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;

public class ContainerEngineStone_BC8 extends ContainerBCTile<TileEngineStone_BC8> {
    public ContainerEngineStone_BC8(int windowId, Player player, TileEngineStone_BC8 engine) {
        super(BCEnergyBlocks.ENGINE_STONE_MENU.get(), windowId, player, engine);
        addFullPlayerInventory(84);
        addSlot(new SlotBase(engine.invFuel, 0, 80, 41));
    }

    public static ContainerEngineStone_BC8 fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerEngineStone_BC8(windowId, inventory.player,
            readTile(inventory, buf, TileEngineStone_BC8.class));
    }
}
