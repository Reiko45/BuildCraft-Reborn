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
import buildcraftreborn.factory.tile.TileTank;
import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.widget.WidgetFluidTank;

public class ContainerTank extends ContainerBCTile<TileTank> {
    public final WidgetFluidTank widgetTank;

    public ContainerTank(int windowId, Player player, TileTank tank) {
        super(BCFactoryBlocks.TANK_MENU.get(), windowId, player, tank);
        addFullPlayerInventory(99);
        widgetTank = addWidget(new WidgetFluidTank(this, tank.tank));
    }

    public static ContainerTank fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerTank(windowId, inventory.player, readTile(inventory, buf, TileTank.class));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // The only slots are player slots -- try to interact with the tank
        if (!player.level().isClientSide) {
            Slot slot = slots.get(index);
            ItemStack stack = slot.getItem();
            ItemStack original = stack.copy();
            stack = tile.tank.transferStackToTank(player, stack);
            tile.balanceTankFluids();
            if (!ItemStack.matches(stack, original)) {
                slot.set(stack);
                broadcastChanges();
                return ItemStack.EMPTY;
            }
        }
        return super.quickMoveStack(player, index);
    }
}
