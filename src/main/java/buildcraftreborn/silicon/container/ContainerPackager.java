/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.silicon.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;
import buildcraftreborn.lib.gui.slot.SlotOutput;
import buildcraftreborn.lib.gui.slot.SlotPhantom;
import buildcraftreborn.lib.tile.item.IItemHandlerAdv;
import buildcraftreborn.silicon.BCSiliconPackaging;
import buildcraftreborn.silicon.tile.TilePackager;

/** The recipe packager: the pattern grid, paper in (top right), the package out (below it), the nine storage slots
 * along the bottom, then the player's inventory. */
public class ContainerPackager extends ContainerBCTile<TilePackager> {
    public static final int PATTERN_X = 30, PATTERN_Y = 17;

    public ContainerPackager(int windowId, Player player, TilePackager tile) {
        super(BCSiliconPackaging.PACKAGER_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(115);

        addSlot(new SlotBase(tile.invPaper, 0, 124, 7));
        for (int x = 0; x < 9; x++) {
            addSlot(new SlotBase(tile.invStorage, x, 8 + x * 18, 84));
        }
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new SlotPattern(tile.invPattern, x + y * 3, PATTERN_X + x * 18, PATTERN_Y + y * 18));
            }
        }
        addSlot(new SlotOutput(tile.invOutput, 0, 123, 59));

        // Which pattern slots are in use
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return tile.patternSet;
            }

            @Override
            public void set(int value) {
                tile.patternSet = value;
            }
        });
    }

    public static ContainerPackager fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerPackager(windowId, inventory.player, readTile(inventory, buf, TilePackager.class));
    }

    /** A pattern slot. With an item in hand, clicking sets the slot to that item. With an empty hand, clicking a slot
     * that names an item clears it, and clicking an empty slot switches "anything" on or off (7.1's left click). */
    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        Slot slot = slotId < 0 || slotId >= slots.size() ? null : slots.get(slotId);
        if (slot instanceof SlotPattern pattern) {
            int index = pattern.handlerIndex;
            ItemStack held = getCarried();
            if (!held.isEmpty()) {
                slot.set(held.copyWithCount(1));
                tile.setPatternSlot(index, true);
            } else if (slot.hasItem()) {
                slot.set(ItemStack.EMPTY);
                tile.setPatternSlot(index, false);
            } else {
                tile.setPatternSlot(index, !tile.isPatternSlotSet(index));
            }
            return;
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    /** A recipe viewer putting an item in a pattern slot sets the slot to that item, as clicking with it does (and
     * emptying one switches it off). */
    @Override
    protected void onPhantomSlotSet(SlotPhantom slot, ItemStack stack) {
        if (slot instanceof SlotPattern pattern) {
            tile.setPatternSlot(pattern.handlerIndex, !stack.isEmpty());
        }
    }

    public static class SlotPattern extends SlotPhantom {
        public SlotPattern(IItemHandlerAdv handler, int index, int x, int y) {
            super(handler, index, x, y, false);
        }
    }
}
