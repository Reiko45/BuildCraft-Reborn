/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.list;

import java.io.IOException;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.lists.ListMatchHandler;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.core.item.ItemList_BC8;
import buildcraftreborn.lib.gui.ContainerBC_Neptune;
import buildcraftreborn.lib.gui.slot.SlotPhantom;
import buildcraftreborn.lib.list.ListHandler;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;

/** The list's menu: its two lines as phantom slots, the option buttons and the label. Edits go straight into the held
 * list.
 * <p>
 * 1.21: the lines are ordinary (phantom) menu slots synced by the menu, instead of 1.12's phantom slot widgets. */
public class ContainerList extends ContainerBC_Neptune {
    protected static final IdAllocator IDS = ContainerBC_Neptune.IDS.makeChild("list");
    private static final int ID_LABEL = IDS.allocId("LABEL");
    private static final int ID_BUTTON = IDS.allocId("BUTTON");

    public static final int MAX_LABEL_LENGTH = 32;

    public final ListHandler.Line[] lines;
    private final ItemHandlerSimple lineSlots;
    private boolean loading = true;

    public ContainerList(int windowId, Player player) {
        super(BCCoreItems.LIST_MENU.get(), windowId, player);
        lines = ListHandler.getLines(getListItemStack());
        lineSlots = new ItemHandlerSimple(ListHandler.WIDTH * lines.length, this::onSlotChange);
        for (int line = 0; line < lines.length; line++) {
            for (int slot = 0; slot < ListHandler.WIDTH; slot++) {
                int index = line * ListHandler.WIDTH + slot;
                lineSlots.setStackInSlot(index, lines[line].getStack(slot));
                addSlot(new SlotPhantom(lineSlots, index, 8 + slot * 18, 32 + line * 34, false));
            }
        }
        loading = false;
        addFullPlayerInventory(103);
    }

    public static ContainerList fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerList(windowId, inventory.player);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Override
    public boolean stillValid(Player player) {
        return !getListItemStack().isEmpty();
    }

    public ItemStack getListItemStack() {
        ItemStack toTry = player.getMainHandItem();
        if (toTry.getItem() instanceof ItemList_BC8) {
            return toTry;
        }
        toTry = player.getOffhandItem();
        if (toTry.getItem() instanceof ItemList_BC8) {
            return toTry;
        }
        return ItemStack.EMPTY;
    }

    private void onSlotChange(IItemHandlerModifiable handler, int index, ItemStack before, ItemStack after) {
        if (loading) {
            return;
        }
        int lineIndex = index / ListHandler.WIDTH;
        int slotIndex = index % ListHandler.WIDTH;
        ListHandler.Line line = lines[lineIndex];
        if (slotIndex != 0 && line.isOneStackMode() && !after.isEmpty()) {
            // Only the first slot is used in "same type" and "same material" mode
            loading = true;
            lineSlots.setStackInSlot(index, ItemStack.EMPTY);
            loading = false;
            return;
        }
        setStack(lineIndex, slotIndex, after);
    }

    void setStack(int lineIndex, int slotIndex, ItemStack stack) {
        lines[lineIndex].setStack(slotIndex, stack);
        ItemStack list = getListItemStack();
        if (!list.isEmpty()) {
            ListHandler.saveLines(list, lines);
        }
    }

    public void switchButton(int lineIndex, int button) {
        if (lineIndex < 0 || lineIndex >= lines.length || button < 0 || button > 2) {
            return;
        }
        lines[lineIndex].toggleOption(button);

        if (player.level().isClientSide) {
            sendMessage(ID_BUTTON, buffer -> {
                buffer.writeByte(lineIndex);
                buffer.writeByte(button);
            });
        } else if (button == 1 || button == 2) {
            ListMatchHandler.Type type = lines[lineIndex].getSortingType();
            if (type == ListMatchHandler.Type.MATERIAL || type == ListMatchHandler.Type.TYPE) {
                loading = true;
                for (int i = 1; i < ListHandler.WIDTH; i++) {
                    lineSlots.setStackInSlot(lineIndex * ListHandler.WIDTH + i, ItemStack.EMPTY);
                }
                loading = false;
            }
        }

        ItemStack list = getListItemStack();
        if (!list.isEmpty()) {
            ListHandler.saveLines(list, lines);
        }
    }

    public String getLabel() {
        ItemStack list = getListItemStack();
        return list.getItem() instanceof ItemList_BC8 item ? item.getLabel(list) : "";
    }

    public void setLabel(String text) {
        if (text.length() > MAX_LABEL_LENGTH) {
            text = text.substring(0, MAX_LABEL_LENGTH);
        }
        ItemStack list = getListItemStack();
        if (list.getItem() instanceof ItemList_BC8 item) {
            item.setLabel(list, text);
        }
        if (player.level().isClientSide) {
            String sent = text;
            sendMessage(ID_LABEL, buffer -> buffer.writeUtf(sent, MAX_LABEL_LENGTH));
        }
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, LogicalSide side, IPayloadContext ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == LogicalSide.SERVER) {
            if (id == ID_BUTTON) {
                int lineIndex = buffer.readUnsignedByte();
                int button = buffer.readUnsignedByte();
                switchButton(lineIndex, button);
            } else if (id == ID_LABEL) {
                setLabel(buffer.readUtf(MAX_LABEL_LENGTH));
            }
        }
    }
}
