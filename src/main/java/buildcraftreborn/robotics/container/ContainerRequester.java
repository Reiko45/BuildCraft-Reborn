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

import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;
import buildcraftreborn.lib.gui.slot.SlotPhantom;
import buildcraftreborn.lib.tile.item.IItemHandlerAdv;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.robotics.tile.TileRequester;

/** The requester: the request templates on the left (click with a stack to ask for that many, with an empty hand to
 * clear) and what has arrived on the right. */
public class ContainerRequester extends ContainerBCTile<TileRequester> {
    public ContainerRequester(int windowId, Player player, TileRequester tile) {
        super(BCRoboticsBlocks.REQUESTER_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(19, 101);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 5; y++) {
                addSlot(new SlotRequest(tile.requests, x * 5 + y, 9 + x * 18, 7 + y * 18));
            }
        }
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 5; y++) {
                addSlot(new SlotBase(tile.inv, x * 5 + y, 117 + x * 18, 7 + y * 18));
            }
        }
    }

    public static ContainerRequester fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerRequester(windowId, inventory.player, readTile(inventory, buf, TileRequester.class));
    }

    /** A request template: takes a copy of the whole held stack (7.1). */
    public static class SlotRequest extends SlotPhantom {
        public SlotRequest(IItemHandlerAdv handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public boolean copiesWholeStack() {
            return true;
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }
    }
}
