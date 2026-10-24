/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.slot;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.tile.item.IItemHandlerAdv;

/** A slot items can only be taken out of. */
public class SlotOutput extends SlotBase {
    public SlotOutput(IItemHandlerAdv handler, int slotIndex, int posX, int posY) {
        super(handler, slotIndex, posX, posY);
    }

    @Override
    public boolean mayPlace(ItemStack itemstack) {
        return false;
    }
}
