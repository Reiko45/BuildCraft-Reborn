/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.inventory;

import java.util.Arrays;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.inventory.IItemTransactor;

/** Designates an {@link IItemTransactor} that is backed by a simple, static, array based inventory. */
public abstract class AbstractInvItemTransactor implements IItemTransactor {
    /** Returns {@link ItemStack#EMPTY} if it was empty, or the input stack if it was not. */
    public static ItemStack asValid(ItemStack stack) {
        return stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    protected abstract ItemStack insert(int slot, ItemStack stack, boolean simulate);

    protected abstract ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate);

    protected abstract int getSlotCount();

    protected abstract boolean isEmpty(int slot);

    @Override
    public ItemStack insert(ItemStack stack, boolean allAtOnce, boolean simulate) {
        if (allAtOnce) {
            return insertAllAtOnce(stack, simulate);
        } else {
            return insertAnyAmount(stack, simulate);
        }
    }

    private ItemStack insertAnyAmount(ItemStack stack, boolean simulate) {
        int slotCount = getSlotCount();
        IntArrayList emptySlots = new IntArrayList(slotCount);
        for (int slot = 0; slot < slotCount; slot++) {
            if (isEmpty(slot)) {
                emptySlots.add(slot);
            } else {
                stack = insert(slot, stack, simulate);
                if (stack.isEmpty()) return ItemStack.EMPTY;
            }
        }
        for (int slot : emptySlots) {
            stack = insert(slot, stack, simulate);
            if (stack.isEmpty()) return ItemStack.EMPTY;
        }
        return stack;
    }

    private ItemStack insertAllAtOnce(ItemStack stack, boolean simulate) {
        ItemStack before = asValid(stack);
        IntArrayList insertedSlots = new IntArrayList(getSlotCount());
        IntArrayList emptySlots = new IntArrayList(getSlotCount());
        for (int slot = 0; slot < getSlotCount(); slot++) {
            if (isEmpty(slot)) {
                emptySlots.add(slot);
            } else {
                stack = insert(slot, stack, true);
                insertedSlots.add(slot);
                if (stack.isEmpty()) break;
            }
        }
        for (int slot : emptySlots) {
            stack = insert(slot, stack, true);
            insertedSlots.add(slot);
            if (stack.isEmpty()) break;
        }
        if (!stack.isEmpty()) {
            return stack;
        }
        if (simulate) return ItemStack.EMPTY;
        for (int slot : insertedSlots) {
            before = insert(slot, before, false);
        }
        if (!before.isEmpty()) {
            // We have a bad implementation that doesn't respect simulation properly- we are in an invalid state at this
            // point with no chance of recovery
            throw new IllegalStateException(
                "Somehow inserting a lot of items at once failed when we thought it shouldn't! (" + getClass() + ")");
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> insert(NonNullList<ItemStack> stacks, boolean simulate) {
        // WARNING: SLOW IMPL (1.12 returned the input unchanged here; the default loop actually inserts)
        return IItemTransactor.super.insert(stacks, simulate);
    }

    @Override
    public ItemStack extract(@Nullable IStackFilter filter, int min, int max, boolean simulate) {
        if (min < 1) min = 1;
        if (min > max) return ItemStack.EMPTY;
        if (max < 0) return ItemStack.EMPTY;
        if (filter == null) {
            filter = stack -> true;
        }
        int slots = getSlotCount();
        IntArrayList valids = new IntArrayList();
        int totalSize = 0;
        ItemStack toExtract = ItemStack.EMPTY;
        for (int slot = 0; slot < slots; slot++) {
            ItemStack possible = extract(slot, filter, 1, max - totalSize, true);
            if (!possible.isEmpty()) {
                if (toExtract.isEmpty()) {
                    toExtract = possible.copy();
                }
                if (ItemStack.isSameItemSameComponents(toExtract, possible)) {
                    totalSize += possible.getCount();
                    valids.add(slot);
                    if (totalSize >= max) {
                        break;
                    }
                }
            }
        }
        ItemStack total = ItemStack.EMPTY;
        if (min <= totalSize) {
            for (int slot : valids) {
                ItemStack extracted = extract(slot, filter, 1, max - total.getCount(), simulate);
                if (total.isEmpty()) {
                    total = extracted.copy();
                } else {
                    total.grow(extracted.getCount());
                }
            }
        }
        return total;
    }

    @Override
    public String toString() {
        ItemStack[] stacks = new ItemStack[getSlotCount()];
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = extract(i, stack -> true, 1, Integer.MAX_VALUE, true);
        }
        return Arrays.toString(stacks);
    }
}
