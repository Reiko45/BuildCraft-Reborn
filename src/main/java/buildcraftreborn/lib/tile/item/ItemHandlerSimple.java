/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.tile.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.inventory.IItemTransactor;
import buildcraftreborn.lib.inventory.AbstractInvItemTransactor;
import buildcraftreborn.lib.tile.item.StackInsertionFunction.InsertionResult;

/** A simple inventory with per-slot insertion checks and a change callback. Also an {@link IItemTransactor}. */
public class ItemHandlerSimple extends AbstractInvItemTransactor
    implements IItemHandlerModifiable, IItemHandlerAdv, INBTSerializable<CompoundTag> {
    // Function-called stuff (helpers etc)
    private StackInsertionChecker checker;
    private StackInsertionFunction inserter;

    @Nullable
    private StackChangeCallback callback;

    // Actual item stacks used
    public final NonNullList<ItemStack> stacks;

    public ItemHandlerSimple(int size) {
        this(size, (slot, stack) -> true, StackInsertionFunction.getDefaultInserter(), null);
    }

    public ItemHandlerSimple(int size, int maxStackSize) {
        this(size);
        setLimitedInsertor(maxStackSize);
    }

    public ItemHandlerSimple(int size, @Nullable StackChangeCallback callback) {
        this(size, (slot, stack) -> true, StackInsertionFunction.getDefaultInserter(), callback);
    }

    public ItemHandlerSimple(int size, StackInsertionChecker checker, StackInsertionFunction insertionFunction,
        @Nullable StackChangeCallback callback) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        this.checker = checker;
        this.inserter = insertionFunction;
        this.callback = callback;
    }

    public void setChecker(StackInsertionChecker checker) {
        this.checker = checker;
    }

    public void setInsertor(StackInsertionFunction insertor) {
        this.inserter = insertor;
    }

    public void setLimitedInsertor(int maxStackSize) {
        setInsertor(StackInsertionFunction.getInsertionFunction(maxStackSize));
    }

    public void setCallback(@Nullable StackChangeCallback callback) {
        this.callback = callback;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        ListTag list = new ListTag();
        for (ItemStack stack : stacks) {
            list.add(stack.saveOptional(registries));
        }
        nbt.put("items", list);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
        ListTag list = nbt.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size() && i < getSlots(); i++) {
            setStackInternal(i, ItemStack.parseOptional(registries, list.getCompound(i)));
        }
        for (int i = list.size(); i < getSlots(); i++) {
            setStackInternal(i, ItemStack.EMPTY);
        }
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    private boolean badSlotIndex(int slot) {
        return slot < 0 || slot >= stacks.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (badSlotIndex(slot)) return ItemStack.EMPTY;
        return stacks.get(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (badSlotIndex(slot) || stack.isEmpty()) {
            return stack;
        }
        if (canSet(slot, stack)) {
            ItemStack current = stacks.get(slot);
            if (!canSet(slot, current)) {
                // A bit odd, but can happen if the filter changed
                return stack;
            }
            InsertionResult result = inserter.modifyForInsertion(slot, current.copy(), stack.copy());
            if (!canSet(slot, result.toSet())) {
                // We have a bad inserter or checker, as they should not be conflicting
                CrashReport report = new CrashReport("Inserting an item (buildcraftreborn:ItemHandlerSimple)",
                    new IllegalStateException("Conflicting Insertion!"));
                CrashReportCategory cat = report.addCategory("Inventory details");
                cat.setDetail("Existing Item", current);
                cat.setDetail("Inserting Item", stack);
                cat.setDetail("To Set", result.toSet());
                cat.setDetail("To Return", result.toReturn());
                cat.setDetail("Slot", slot);
                cat.setDetail("Checker", checker.getClass());
                cat.setDetail("Inserter", inserter.getClass());
                throw new ReportedException(report);
            } else if (!simulate) {
                setStackInternal(slot, result.toSet());
                if (callback != null) {
                    callback.onStackChange(this, slot, current, result.toSet());
                }
            }
            return result.toReturn();
        } else {
            return stack;
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (badSlotIndex(slot) || amount <= 0) return ItemStack.EMPTY;
        // You can ALWAYS extract. if you couldn't then you could never take out items from anywhere
        ItemStack current = stacks.get(slot);
        if (current.isEmpty()) return ItemStack.EMPTY;
        if (current.getCount() <= amount) {
            if (simulate) {
                return current.copy();
            }
            setStackInternal(slot, ItemStack.EMPTY);
            if (callback != null) {
                callback.onStackChange(this, slot, current, ItemStack.EMPTY);
            }
            // no need to copy as we no longer have it
            return current;
        } else {
            ItemStack before = current;
            current = current.copy();
            ItemStack split = current.split(amount);
            if (!simulate) {
                setStackInternal(slot, current);
                if (callback != null) {
                    callback.onStackChange(this, slot, before, current);
                }
            }
            return split;
        }
    }

    @Override
    protected boolean isEmpty(int slot) {
        if (badSlotIndex(slot)) return true;
        return stacks.get(slot).isEmpty();
    }

    @Override
    protected int getSlotCount() {
        return getSlots();
    }

    @Override
    protected ItemStack insert(int slot, ItemStack stack, boolean simulate) {
        return insertItem(slot, stack, simulate);
    }

    @Override
    protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
        if (badSlotIndex(slot)) return ItemStack.EMPTY;
        if (min <= 0) min = 1;
        if (max < min) return ItemStack.EMPTY;
        ItemStack current = stacks.get(slot);
        ItemStack before = current.copy();
        if (current.getCount() < min) return ItemStack.EMPTY;
        if (filter.matches(asValid(current))) {
            if (simulate) {
                ItemStack copy = current.copy();
                return copy.split(max);
            }
            ItemStack split = current.split(max);
            if (current.getCount() <= 0) {
                stacks.set(slot, ItemStack.EMPTY);
            }
            if (callback != null) {
                callback.onStackChange(this, slot, before, stacks.get(slot));
            }
            return split;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (badSlotIndex(slot)) {
            // Its safe to throw here
            throw new IndexOutOfBoundsException("Slot index out of range: " + slot);
        }
        ItemStack before = stacks.get(slot);
        setStackInternal(slot, stack);
        if (callback != null) {
            callback.onStackChange(this, slot, before, stack);
        }
    }

    @Override
    public final boolean canSet(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        return checker.canSet(slot, stack);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return canSet(slot, stack);
    }

    private void setStackInternal(int slot, ItemStack stack) {
        stacks.set(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        // (menus use this to decide how much of a stack a click puts in)
        return Math.min(64, inserter.getMaxStackSize());
    }

    @Override
    public String toString() {
        return "ItemHandlerSimple " + stacks;
    }
}
