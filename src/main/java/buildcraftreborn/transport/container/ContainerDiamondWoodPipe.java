/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.container;

import java.io.IOException;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraftreborn.lib.gui.ContainerPipe;
import buildcraftreborn.lib.gui.slot.SlotPhantom;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.transport.BCTransportBlocks;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourWoodDiamond.FilterMode;

/** The emerald (diamond wood) pipe's 9 filters, and its filter mode buttons. */
public class ContainerDiamondWoodPipe extends ContainerPipe {
    public final PipeBehaviourWoodDiamond behaviour;

    public ContainerDiamondWoodPipe(int windowId, Player player, PipeBehaviourWoodDiamond behaviour) {
        super(BCTransportBlocks.PIPE_DIAMOND_WOOD_MENU.get(), windowId, player, behaviour.pipe.getHolder());
        this.behaviour = behaviour;
        addFullPlayerInventory(79);

        for (int i = 0; i < 9; i++) {
            addSlot(new SlotPhantom(behaviour.filters, i, 8 + i * 18, 18));
        }
    }

    public static ContainerDiamondWoodPipe fromNetwork(int windowId, Inventory inventory,
        RegistryFriendlyByteBuf buf) {
        return new ContainerDiamondWoodPipe(windowId, inventory.player,
            readBehaviour(inventory, buf, PipeBehaviourWoodDiamond.class));
    }

    public void sendNewFilterMode(FilterMode newFilterMode) {
        this.sendMessage(NET_DATA, (buffer) -> buffer.writeEnum(newFilterMode));
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, LogicalSide side, IPayloadContext ctx)
        throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (id == NET_DATA && side == LogicalSide.SERVER) {
            behaviour.filterMode = buffer.readEnum(FilterMode.class);
            behaviour.pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }
}
