/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.container;

import java.io.IOException;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.tile.TileArchitectTable;
import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.ContainerBC_Neptune;
import buildcraftreborn.lib.gui.slot.SlotBase;
import buildcraftreborn.lib.gui.slot.SlotOutput;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.net.PacketBufferBC;

public class ContainerArchitectTable extends ContainerBCTile<TileArchitectTable> {
    private static final IdAllocator IDS = ContainerBC_Neptune.IDS.makeChild("architect_table");
    private static final int ID_NAME = IDS.allocId("NAME");

    public ContainerArchitectTable(int windowId, Player player, TileArchitectTable tile) {
        super(BCBuildersBlocks.ARCHITECT_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(88, 84);

        addSlot(new SlotBase(tile.invSnapshotIn, 0, 135, 35));
        addSlot(new SlotOutput(tile.invSnapshotOut, 0, 194, 35));
    }

    public static ContainerArchitectTable fromNetwork(int windowId, Inventory inventory,
        RegistryFriendlyByteBuf buf) {
        return new ContainerArchitectTable(windowId, inventory.player,
            readTile(inventory, buf, TileArchitectTable.class));
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public void sendNameToServer(String name) {
        sendMessage(ID_NAME, buffer -> buffer.writeUtf(name));
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, LogicalSide side, IPayloadContext ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == LogicalSide.SERVER) {
            if (id == ID_NAME) {
                tile.setName(buffer.readUtf(256));
            }
        }
    }
}
