/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.Unpooled;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.gui.slot.IPhantomSlot;
import buildcraftreborn.lib.gui.slot.SlotPhantom;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.net.ContainerPayload;
import buildcraftreborn.lib.net.IPayloadWriter;
import buildcraftreborn.lib.net.PacketBufferBC;

/** Base menu for BuildCraft GUIs: player inventory helpers, phantom slots, shift-click, widgets and BuildCraft's own
 * menu messages ({@link ContainerPayload}). */
public abstract class ContainerBC_Neptune extends AbstractContainerMenu {
    public static final boolean DEBUG = Boolean.getBoolean("buildcraftreborn.debug.container");

    protected static final IdAllocator IDS = new IdAllocator("container");
    /** Generic "data" id. Used by all containers which only have 1 id to write out (no point in making EVERY container
     * have an {@link IdAllocator} if they only allocate one. */
    public static final int NET_DATA = IDS.allocId("DATA");
    public static final int NET_WIDGET = IDS.allocId("WIDGET");
    public static final int NET_SET_PHANTOM = IDS.allocId("SET_PHANTOM");
    public static final int NET_SET_PHANTOM_MULTI = IDS.allocId("NET_SET_PHANTOM_MULTI");

    private static final int PLAYER_INVENTORY_SIZE = 36;

    public final Player player;
    private final List<Widget_Neptune<?>> widgets = new ArrayList<>();

    protected ContainerBC_Neptune(@Nullable MenuType<?> type, int windowId, Player player) {
        super(type, windowId);
        this.player = player;
    }

    /** @return The {@link IdAllocator} that allocates all ID's for this class, and its parent classes. */
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    protected void addFullPlayerInventory(int startX, int startY) {
        Inventory inventory = player.getInventory();
        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlot(new Slot(inventory, sx + sy * 9 + 9, startX + sx * 18, startY + sy * 18));
            }
        }
        for (int sx = 0; sx < 9; sx++) {
            addSlot(new Slot(inventory, sx, startX + sx * 18, startY + 58));
        }
    }

    protected void addFullPlayerInventory(int startY) {
        addFullPlayerInventory(8, startY);
    }

    protected <W extends Widget_Neptune<?>> W addWidget(W widget) {
        if (widget == null) throw new NullPointerException("widget");
        widgets.add(widget);
        return widget;
    }

    public List<Widget_Neptune<?>> getWidgets() {
        return List.copyOf(widgets);
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        Slot slot = slotId < 0 || slotId >= slots.size() ? null : slots.get(slotId);
        if (slot instanceof IPhantomSlot phantom) {
            ItemStack playerStack = getCarried();
            if (playerStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else if (phantom.copiesWholeStack()) {
                slot.set(playerStack.copy());
            } else if (!ItemStack.isSameItemSameComponents(playerStack, slot.getItem())) {
                slot.set(playerStack.copyWithCount(1));
            } else if (phantom.canAdjustCount()) {
                ItemStack stack = slot.getItem().copy();
                if (stack.getCount() < stack.getMaxStackSize()) {
                    stack.grow(1);
                    slot.set(stack);
                }
            }
            return;
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        Slot firstSlot = slots.get(0);
        boolean playerInventoryFirst = firstSlot.container instanceof Inventory;

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (slots.size() == PLAYER_INVENTORY_SIZE) return ItemStack.EMPTY;
            if (playerInventoryFirst) {
                if (index < PLAYER_INVENTORY_SIZE) {
                    if (!moveItemStackTo(itemstack1, PLAYER_INVENTORY_SIZE, slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(itemstack1, 0, PLAYER_INVENTORY_SIZE, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (index < slots.size() - PLAYER_INVENTORY_SIZE) {
                    if (!moveItemStackTo(itemstack1, slots.size() - PLAYER_INVENTORY_SIZE, slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(itemstack1, 0, slots.size() - PLAYER_INVENTORY_SIZE, true)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    // Package-private so that the widget itself can send this
    void sendWidgetData(Widget_Neptune<?> widget, IPayloadWriter writer) {
        int widgetId = widgets.indexOf(widget);
        if (widgetId == -1) {
            if (DEBUG) {
                throw new IllegalArgumentException("Invalid Widget Request! (" + widget.getClass() + ")");
            }
            BuildCraftReborn.LOGGER.warn("[lib.container] Received an invalid widget sending request! ({} in {})",
                widget.getClass(), getClass());
        } else {
            sendMessage(NET_WIDGET, (buffer) -> {
                buffer.writeShort(widgetId);
                writer.write(buffer);
            });
        }
    }

    public final void sendMessage(int id) {
        LogicalSide side = player.level().isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER;
        sendMessage(id, (buffer) -> writeMessage(id, buffer, side));
    }

    public final void sendMessage(int id, IPayloadWriter writer) {
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer(), player.level().registryAccess());
        ContainerPayload message;
        try {
            writer.write(buffer);
            message = new ContainerPayload(containerId, id, buffer.toByteArray());
        } finally {
            buffer.release();
        }
        if (player.level().isClientSide) {
            PacketDistributor.sendToServer(message);
        } else if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, message);
        }
    }

    public void writeMessage(int id, PacketBufferBC buffer, LogicalSide side) {}

    public void readMessage(int id, PacketBufferBC buffer, LogicalSide side, IPayloadContext ctx) throws IOException {
        if (id == NET_WIDGET) {
            int widgetId = buffer.readUnsignedShort();
            if (widgetId < 0 || widgetId >= widgets.size()) {
                if (DEBUG) {
                    BuildCraftReborn.LOGGER.warn("Received unknown or invalid widget ID {} on side {}", widgetId, side);
                }
            } else {
                Widget_Neptune<?> widget = widgets.get(widgetId);
                if (side == LogicalSide.SERVER) {
                    widget.handleWidgetDataServer(ctx, buffer);
                } else {
                    widget.handleWidgetDataClient(ctx, buffer);
                }
            }
        } else if (side == LogicalSide.SERVER) {
            if (id == NET_SET_PHANTOM) {
                readSingleSetPhantom(buffer);
            } else if (id == NET_SET_PHANTOM_MULTI) {
                int count = buffer.readUnsignedByte();
                for (int i = 0; i < count; i++) {
                    readSingleSetPhantom(buffer);
                }
            }
        }
    }

    private void readSingleSetPhantom(PacketBufferBC buffer) {
        int idx = buffer.readVarInt();
        ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        if (idx >= 0 && idx < slots.size() && slots.get(idx) instanceof SlotPhantom ph) {
            if (ph.itemHandler instanceof IItemHandlerModifiable handler && ph.itemHandler.canSet(ph.handlerIndex, stack)) {
                handler.setStackInSlot(ph.handlerIndex, stack);
                onPhantomSlotSet(ph, stack);
                return;
            }
        }
        // log rather than throw an exception because of bugged/naughty clients
        BuildCraftReborn.LOGGER.warn("[lib.container] Received an illegal phantom slot setting request! "
            + "(Client = {}, slot_index = {}, stack = {})", player.getName().getString(), idx, stack);
    }

    /** Called on the server after a client (a recipe viewer, usually) has set a phantom slot. */
    protected void onPhantomSlotSet(SlotPhantom slot, ItemStack stack) {}

    /** Asks the server to set a phantom slot (client side). */
    public void sendSetPhantomSlot(SlotPhantom slot, ItemStack to) {
        int index = slots.indexOf(slot);
        if (index == -1) {
            throw new IllegalArgumentException("Couldn't find a slot for " + slot + " in " + getClass());
        }
        sendMessage(NET_SET_PHANTOM, (buffer) -> {
            buffer.writeVarInt(index);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, to);
        });
    }

    /** Asks the server to set several phantom slots of one inventory at once (client side): stack {@code i} goes in
     * the phantom slot showing slot {@code i} of the handler. Used by recipe viewers to fill a crafting blueprint.
     *
     * @throws IllegalArgumentException if a stack has no phantom slot for it. */
    public void sendSetPhantomSlots(IItemHandler handler, List<ItemStack> stacks) {
        if (stacks.size() > 255) {
            throw new IllegalArgumentException("Too many stacks: " + stacks.size());
        }
        int[] indexes = new int[stacks.size()];
        for (int i = 0; i < stacks.size(); i++) {
            indexes[i] = findPhantomSlot(handler, i);
        }
        sendMessage(NET_SET_PHANTOM_MULTI, buffer -> {
            buffer.writeByte(indexes.length);
            for (int i = 0; i < indexes.length; i++) {
                buffer.writeVarInt(indexes[i]);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stacks.get(i));
            }
        });
    }

    private int findPhantomSlot(IItemHandler handler, int index) {
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i) instanceof SlotPhantom ph && ph.itemHandler == handler && ph.handlerIndex == index) {
                return i;
            }
        }
        throw new IllegalArgumentException("Couldn't find a phantom slot for " + handler + " slot " + index);
    }
}
