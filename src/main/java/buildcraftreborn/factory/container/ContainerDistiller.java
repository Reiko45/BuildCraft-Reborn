/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.tile.TileDistiller_BC8;
import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.widget.WidgetFluidTank;

public class ContainerDistiller extends ContainerBCTile<TileDistiller_BC8> {
    public final WidgetFluidTank widgetInputTank;
    public final WidgetFluidTank widgetOutputGasTank;
    public final WidgetFluidTank widgetOutputLiquidTank;

    public ContainerDistiller(int windowId, Player player, TileDistiller_BC8 tile) {
        super(BCFactoryBlocks.DISTILLER_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(79);

        widgetInputTank = addWidget(new WidgetFluidTank(this, tile.tankIn));
        widgetOutputGasTank = addWidget(new WidgetFluidTank(this, tile.tankGasOut));
        widgetOutputLiquidTank = addWidget(new WidgetFluidTank(this, tile.tankLiquidOut));
    }

    public static ContainerDistiller fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerDistiller(windowId, inventory.player, readTile(inventory, buf, TileDistiller_BC8.class));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // The only slots are player slots -- try to interact with the tank
        if (!player.level().isClientSide) {
            Slot slot = slots.get(index);
            ItemStack stack = slot.getItem();
            ItemStack original = stack.copy();
            stack = tile.tankIn.transferStackToTank(player, stack);
            if (!ItemStack.matches(stack, original)) {
                slot.set(stack);
                broadcastChanges();
                return ItemStack.EMPTY;
            }
        }
        return super.quickMoveStack(player, index);
    }
}
