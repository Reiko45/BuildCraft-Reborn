/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.container;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.enums.EnumSnapshotType;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.tile.TileBuilder;
import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;
import buildcraftreborn.lib.gui.slot.SlotDisplay;
import buildcraftreborn.lib.gui.widget.WidgetFluidTank;

public class ContainerBuilder extends ContainerBCTile<TileBuilder> {
    public final List<WidgetFluidTank> widgetTanks;

    public ContainerBuilder(int windowId, Player player, TileBuilder tile) {
        super(BCBuildersBlocks.BUILDER_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(140);

        addSlot(new SlotBase(tile.invSnapshot, 0, 80, 27));

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlot(new SlotBase(tile.invResources, sx + sy * 9, 8 + sx * 18, 72 + sy * 18));
            }
        }

        widgetTanks = tile.getTankManager().stream().map(tank -> new WidgetFluidTank(this, tank))
            .map(this::addWidget).collect(Collectors.toList());

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 4; x++) {
                addSlot(new SlotDisplay(this::getDisplay, x + y * 4, 179 + x * 18, 18 + y * 18));
            }
        }
    }

    public static ContainerBuilder fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerBuilder(windowId, inventory.player, readTile(inventory, buf, TileBuilder.class));
    }

    private ItemStack getDisplay(int index) {
        return tile.snapshotType == EnumSnapshotType.BLUEPRINT
            && index < tile.blueprintBuilder.remainingDisplayRequired.size()
                ? tile.blueprintBuilder.remainingDisplayRequired.get(index) : ItemStack.EMPTY;
    }
}
