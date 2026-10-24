/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.container;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.snapshot.Snapshot;
import buildcraftreborn.builders.tile.TileElectronicLibrary;
import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.ContainerBC_Neptune;
import buildcraftreborn.lib.gui.slot.SlotBase;
import buildcraftreborn.lib.gui.slot.SlotOutput;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.TileBC_Neptune;

public class ContainerElectronicLibrary extends ContainerBCTile<TileElectronicLibrary> {
    private static final IdAllocator IDS = ContainerBC_Neptune.IDS.makeChild("electronic_library");
    private static final int ID_SELECTED = IDS.allocId("SELECTED");

    public ContainerElectronicLibrary(int windowId, Player player, TileElectronicLibrary tile) {
        super(BCBuildersBlocks.LIBRARY_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(138);

        addSlot(new SlotOutput(tile.invDownOut, 0, 175, 57));
        addSlot(new SlotBase(tile.invDownIn, 0, 219, 57));

        addSlot(new SlotBase(tile.invUpIn, 0, 175, 79));
        addSlot(new SlotOutput(tile.invUpOut, 0, 219, 79));
    }

    public static ContainerElectronicLibrary fromNetwork(int windowId, Inventory inventory,
        RegistryFriendlyByteBuf buf) {
        return new ContainerElectronicLibrary(windowId, inventory.player,
            readTile(inventory, buf, TileElectronicLibrary.class));
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public void sendSelectedToServer(@Nullable Snapshot.Key selected) {
        sendMessage(ID_SELECTED, buffer -> {
            buffer.writeBoolean(selected != null);
            if (selected != null) {
                selected.writeToByteBuf(buffer);
            }
        });
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, LogicalSide side, IPayloadContext ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == LogicalSide.SERVER) {
            if (id == ID_SELECTED) {
                tile.selected = buffer.readBoolean() ? new Snapshot.Key(buffer) : null;
                tile.sendNetworkUpdate(TileBC_Neptune.NET_RENDER_DATA);
            }
        }
    }
}
