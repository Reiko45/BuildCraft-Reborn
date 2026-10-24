/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;
import buildcraftreborn.lib.gui.widget.WidgetFluidTank;

/** Menu for the test machine: its item slot, its tank, and the player inventory. Laid out on the tank GUI texture. */
public class ContainerTestMachine extends ContainerBCTile<TileTestMachine> {
    public static final int SLOT_X = 44, SLOT_Y = 41;

    public final WidgetFluidTank widgetTank;

    public ContainerTestMachine(int windowId, Player player, TileTestMachine tile) {
        super(BCTestContent.TEST_MACHINE_MENU.get(), windowId, player, tile);
        addSlot(new SlotBase(tile.inventory, 0, SLOT_X, SLOT_Y));
        addFullPlayerInventory(99);
        widgetTank = addWidget(new WidgetFluidTank(this, tile.tank));
    }

    /** Client-side factory: the server sent the tile's position. */
    public static ContainerTestMachine fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerTestMachine(windowId, inventory.player,
            readTile(inventory, buf, TileTestMachine.class));
    }
}
