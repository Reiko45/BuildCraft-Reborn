/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import buildcraftreborn.lib.gui.ContainerPipe;
import buildcraftreborn.lib.gui.slot.SlotPhantom;
import buildcraftreborn.transport.BCTransportBlocks;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourDiamond;

/** The diamond pipe's filters: one row of 9 per side. */
public class ContainerDiamondPipe extends ContainerPipe {
    public final PipeBehaviourDiamond behaviour;

    public ContainerDiamondPipe(int windowId, Player player, PipeBehaviourDiamond behaviour) {
        super(BCTransportBlocks.PIPE_DIAMOND_MENU.get(), windowId, player, behaviour.pipe.getHolder());
        this.behaviour = behaviour;

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new SlotPhantom(behaviour.filters, x + y * 9, 8 + x * 18, 18 + y * 18));
            }
        }
        for (int l = 0; l < 3; l++) {
            for (int k1 = 0; k1 < 9; k1++) {
                addSlot(new Slot(player.getInventory(), k1 + l * 9 + 9, 8 + k1 * 18, 140 + l * 18));
            }
        }
        for (int i1 = 0; i1 < 9; i1++) {
            addSlot(new Slot(player.getInventory(), i1, 8 + i1 * 18, 198));
        }
    }

    public static ContainerDiamondPipe fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerDiamondPipe(windowId, inventory.player,
            readBehaviour(inventory, buf, PipeBehaviourDiamond.class));
    }
}
