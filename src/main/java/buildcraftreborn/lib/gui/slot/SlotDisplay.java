/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.slot;

import java.util.function.IntFunction;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** A slot that only shows an item (from a getter), and can't be clicked into or taken from. */
public class SlotDisplay extends Slot {
    private static final SimpleContainer EMPTY_INVENTORY = new SimpleContainer(0);
    private final IntFunction<ItemStack> getter;

    public SlotDisplay(IntFunction<ItemStack> getter, int index, int xPosition, int yPosition) {
        super(EMPTY_INVENTORY, index, xPosition, yPosition);
        this.getter = getter;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {}

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return getter.apply(getContainerSlot()).copy();
    }

    @Override
    public boolean hasItem() {
        return !getItem().isEmpty();
    }

    @Override
    public void set(ItemStack stack) {}

    @Override
    public void setByPlayer(ItemStack stack) {}

    @Override
    public void setChanged() {}

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return getItem().getCount();
    }

    @Override
    public int getMaxStackSize() {
        return getItem().getCount();
    }
}
