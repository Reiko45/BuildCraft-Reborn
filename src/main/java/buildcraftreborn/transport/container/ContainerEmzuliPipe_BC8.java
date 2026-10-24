/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.container;

import java.io.IOException;
import java.util.EnumMap;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraftreborn.lib.gui.ContainerPipe;
import buildcraftreborn.lib.gui.Widget_Neptune;
import buildcraftreborn.lib.gui.slot.SlotPhantom;
import buildcraftreborn.lib.misc.MessageUtil;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.transport.BCTransportBlocks;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourEmzuli;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;

/** The emzuli pipe: four filter slots, each with a paint colour for the items it extracts. */
public class ContainerEmzuliPipe_BC8 extends ContainerPipe {
    public final PipeBehaviourEmzuli behaviour;
    public final EnumMap<SlotIndex, PaintWidget> paintWidgets = new EnumMap<>(SlotIndex.class);

    public ContainerEmzuliPipe_BC8(int windowId, Player player, PipeBehaviourEmzuli behaviour) {
        super(BCTransportBlocks.PIPE_EMZULI_MENU.get(), windowId, player, behaviour.pipe.getHolder());
        this.behaviour = behaviour;
        addFullPlayerInventory(84);

        addSlot(new SlotPhantom(behaviour.invFilters, 0, 25, 21));
        addSlot(new SlotPhantom(behaviour.invFilters, 1, 25, 49));
        addSlot(new SlotPhantom(behaviour.invFilters, 2, 134, 21));
        addSlot(new SlotPhantom(behaviour.invFilters, 3, 134, 49));

        for (SlotIndex index : SlotIndex.VALUES) {
            PaintWidget widget = new PaintWidget(this, index);
            addWidget(widget);
            paintWidgets.put(index, widget);
        }
    }

    public static ContainerEmzuliPipe_BC8 fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerEmzuliPipe_BC8(windowId, inventory.player,
            readBehaviour(inventory, buf, PipeBehaviourEmzuli.class));
    }

    public static class PaintWidget extends Widget_Neptune<ContainerEmzuliPipe_BC8> {
        public final SlotIndex index;

        public PaintWidget(ContainerEmzuliPipe_BC8 container, SlotIndex index) {
            super(container);
            this.index = index;
        }

        public void setColour(DyeColor colour) {
            sendWidgetData((buffer) -> MessageUtil.writeEnumOrNull(buffer, colour));
        }

        @Override
        public void handleWidgetDataServer(IPayloadContext ctx, PacketBufferBC buffer) throws IOException {
            DyeColor colour = MessageUtil.readEnumOrNull(buffer, DyeColor.class);
            if (colour == null) {
                container.behaviour.slotColours.remove(index);
            } else {
                container.behaviour.slotColours.put(index, colour);
            }
            container.behaviour.pipe.getHolder().scheduleNetworkGuiUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }
}
