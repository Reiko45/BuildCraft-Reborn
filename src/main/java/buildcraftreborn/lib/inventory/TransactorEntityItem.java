/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.inventory;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.inventory.IItemTransactor.IItemExtractable;
import buildcraftreborn.lib.inventory.filter.StackFilter;

/** Takes items out of an item entity lying in the world. */
public class TransactorEntityItem implements IItemExtractable {

    private final ItemEntity entity;

    public TransactorEntityItem(ItemEntity entity) {
        this.entity = entity;
    }

    @Override
    public ItemStack extract(@Nullable IStackFilter filter, int min, int max, boolean simulate) {
        if (!entity.isAlive()) {
            return ItemStack.EMPTY;
        }
        if (filter == null) {
            filter = StackFilter.ALL;
        }
        if (min < 1) {
            min = 1;
        }
        if (max < min) {
            return ItemStack.EMPTY;
        }
        ItemStack current = entity.getItem();
        if (current.isEmpty() || current.getCount() < min) {
            return ItemStack.EMPTY;
        }
        if (filter.matches(current)) {
            current = current.copy();
            ItemStack extracted = current.split(max);
            if (!simulate) {
                if (current.getCount() == 0) {
                    entity.discard();
                } else {
                    entity.setItem(current);
                }
            }
            return extracted;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public String toString() {
        return entity.toString();
    }
}
