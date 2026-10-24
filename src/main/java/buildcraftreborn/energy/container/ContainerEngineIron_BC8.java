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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.tile.TileEngineIron_BC8;
import buildcraftreborn.lib.fluid.Tank;
import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.widget.WidgetFluidTank;

public class ContainerEngineIron_BC8 extends ContainerBCTile<TileEngineIron_BC8> {
    public final WidgetFluidTank widgetTankFuel;
    public final WidgetFluidTank widgetTankCoolant;
    public final WidgetFluidTank widgetTankResidue;

    public ContainerEngineIron_BC8(int windowId, Player player, TileEngineIron_BC8 engine) {
        super(BCEnergyBlocks.ENGINE_IRON_MENU.get(), windowId, player, engine);
        addFullPlayerInventory(95);

        widgetTankFuel = addWidget(new WidgetFluidTank(this, engine.tankFuel));
        widgetTankCoolant = addWidget(new WidgetFluidTank(this, engine.tankCoolant));
        widgetTankResidue = addWidget(new WidgetFluidTank(this, engine.tankResidue));
    }

    public static ContainerEngineIron_BC8 fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerEngineIron_BC8(windowId, inventory.player,
            readTile(inventory, buf, TileEngineIron_BC8.class));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // The only slots are player slots -- try to interact with all of the tanks
        if (!player.level().isClientSide) {
            Slot slot = slots.get(index);
            ItemStack stack = slot.getItem();
            ItemStack original = stack.copy();
            for (Tank tank : new Tank[] { tile.tankFuel, tile.tankCoolant, tile.tankResidue }) {
                stack = tank.transferStackToTank(player, stack);
                if (!ItemStack.matches(stack, original)) {
                    slot.setByPlayer(stack);
                    broadcastChanges();
                    return ItemStack.EMPTY;
                }
            }
        }
        return super.quickMoveStack(player, index);
    }
}
