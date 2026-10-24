/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;
import buildcraftreborn.lib.gui.slot.SlotDisplay;
import buildcraftreborn.lib.gui.slot.SlotOutput;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.robotics.tile.TileProgrammingTable;

/** The programming table: the input and output on the left, the recipe's options in the middle. */
public class ContainerProgrammingTable extends ContainerBCTile<TileProgrammingTable> {
    public static final int OPTIONS_X = 43, OPTIONS_Y = 36;

    public ContainerProgrammingTable(int windowId, Player player, TileProgrammingTable tile) {
        super(BCRoboticsBlocks.PROGRAMMING_TABLE_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(123);

        addSlot(new SlotBase(tile.invInput, 0, 8, 36));
        addSlot(new SlotOutput(tile.invOutput, 0, 8, 90));

        for (int y = 0; y < TileProgrammingTable.HEIGHT; y++) {
            for (int x = 0; x < TileProgrammingTable.WIDTH; x++) {
                addSlot(new SlotDisplay(this::getOption, x + y * TileProgrammingTable.WIDTH, OPTIONS_X + x * 18,
                    OPTIONS_Y + y * 18));
            }
        }
    }

    public static ContainerProgrammingTable fromNetwork(int windowId, Inventory inventory,
        RegistryFriendlyByteBuf buf) {
        return new ContainerProgrammingTable(windowId, inventory.player,
            readTile(inventory, buf, TileProgrammingTable.class));
    }

    private ItemStack getOption(int index) {
        return index < tile.options.size() ? tile.options.get(index) : ItemStack.EMPTY;
    }
}
