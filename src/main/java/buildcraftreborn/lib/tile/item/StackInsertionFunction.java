/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.tile.item;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface StackInsertionFunction {
    /** @param addingTo The existing stack. Modifications are lost.
     * @param toInsert The stacks to insert. Modifications are lost.
     * @return The result of attempting to insert it. */
    InsertionResult modifyForInsertion(int slot, ItemStack addingTo, ItemStack toInsert);

    /** 1.21: the most this puts in one slot. NeoForge's menu slots only ask the inventory's slot limit (not a
     * simulated insertion, as 1.12 did), so {@link ItemHandlerSimple#getSlotLimit(int)} returns this. */
    default int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }

    /** Gets a stack insertion function that will insert items up to a given stack size. The stack size of the items
     * themselves IS taken into account, so this has an effective upper limit of 64. */
    static StackInsertionFunction getInsertionFunction(int maxStackSize) {
        StackInsertionFunction function = (slot, addingTo, toInsert) -> {
            if (toInsert.isEmpty()) {
                return new InsertionResult(addingTo, ItemStack.EMPTY);
            }
            if (addingTo.isEmpty()) {
                int maxSize = Math.min(maxStackSize, toInsert.getMaxStackSize());
                if (toInsert.getCount() <= maxSize) {
                    return new InsertionResult(toInsert, ItemStack.EMPTY);
                } else {
                    ItemStack inserted = toInsert.split(maxSize);
                    return new InsertionResult(inserted, toInsert);
                }
            } else if (addingTo.getCount() == maxStackSize) {
                return new InsertionResult(addingTo, toInsert);
            } else if (ItemStack.isSameItemSameComponents(addingTo, toInsert)) {
                ItemStack complete = addingTo.copy();
                int count = addingTo.getCount() + toInsert.getCount();
                int maxSize = Math.min(maxStackSize, complete.getMaxStackSize());
                if (count <= maxSize) {
                    complete.setCount(count);
                    return new InsertionResult(complete, ItemStack.EMPTY);
                } else {
                    complete.setCount(maxSize);
                    ItemStack leftOver = toInsert.copy();
                    leftOver.setCount(count - maxSize);
                    return new InsertionResult(complete, leftOver);
                }
            }
            return new InsertionResult(addingTo, toInsert);
        };
        return new StackInsertionFunction() {
            @Override
            public InsertionResult modifyForInsertion(int slot, ItemStack addingTo, ItemStack toInsert) {
                return function.modifyForInsertion(slot, addingTo, toInsert);
            }

            @Override
            public int getMaxStackSize() {
                return maxStackSize;
            }
        };
    }

    /** Gets a stack insertion function that will insert up to full stacks into a given slot. This is just
     * {@link #getInsertionFunction(int)} with an argument of {@link Integer#MAX_VALUE}. */
    static StackInsertionFunction getDefaultInserter() {
        return getInsertionFunction(Integer.MAX_VALUE);
    }

    /** The result of an attempted insertion. */
    record InsertionResult(ItemStack toSet, ItemStack toReturn) {
        public static final InsertionResult EMPTY_STACKS = new InsertionResult(ItemStack.EMPTY, ItemStack.EMPTY);
    }
}
