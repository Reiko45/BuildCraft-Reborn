/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.tile.item.IItemHandlerAdv;

/** A "ghost" slot: clicking copies the held item into it without taking it. Used by filters. */
public class SlotPhantom extends SlotBase implements IPhantomSlot {
    private final boolean canAdjustCount;

    public SlotPhantom(IItemHandlerAdv itemHandler, int slotIndex, int posX, int posY, boolean adjustableCount) {
        super(itemHandler, slotIndex, posX, posY);
        this.canAdjustCount = adjustableCount;
    }

    public SlotPhantom(IItemHandlerAdv itemHandler, int slotIndex, int posX, int posY) {
        this(itemHandler, slotIndex, posX, posY, true);
    }

    @Override
    public boolean canAdjustCount() {
        return canAdjustCount;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
