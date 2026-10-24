/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import java.util.Collection;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.items.IList;

/** Provides various utils for interacting with {@link ItemStack}, and multiples.
 * <p>
 * 1.21: item metadata and NBT are data components, so "matching NBT" compares components, and the 1.12 wildcard
 * damage value has no equivalent. Only the methods used so far are ported. */
public final class StackUtil {

    private StackUtil() {}

    /** Checks to see if the two input stacks are equal in all but stack size. Note that this doesn't check anything
     * todo with stack size, so if you pass in two stacks of 64 cobblestone this will return true. If you pass in null
     * (at all) then this will only return true if both are null. */
    public static boolean canMerge(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b);
    }

    /** Checks to see if the given required stack is contained fully in the given container stack. */
    public static boolean contains(ItemStack required, ItemStack container) {
        if (canMerge(required, container)) {
            return container.getCount() >= required.getCount();
        }
        return false;
    }

    /** Checks to see if the given required stack is contained fully in a single stack in a list. */
    public static boolean contains(ItemStack required, Collection<ItemStack> containers) {
        for (ItemStack possible : containers) {
            if (contains(required, possible)) {
                return true;
            }
        }
        return false;
    }

    /** Checks to see if every required stack is contained fully in a single stack in a list. */
    public static boolean containsAll(Collection<ItemStack> required, Collection<ItemStack> containers) {
        for (ItemStack req : required) {
            if (req.isEmpty()) continue;
            if (!contains(req, containers)) {
                return false;
            }
        }
        return true;
    }

    /** Merges mergeSource into mergeTarget
     *
     * @param mergeSource - The stack to merge into mergeTarget, this stack is not modified
     * @param mergeTarget - The target merge, this stack is modified if doMerge is set
     * @param doMerge - To actually do the merge
     * @return The number of items that was successfully merged. */
    public static int mergeStacks(ItemStack mergeSource, ItemStack mergeTarget, boolean doMerge) {
        if (!canMerge(mergeSource, mergeTarget)) {
            return 0;
        }
        int mergeCount = Math.min(mergeTarget.getMaxStackSize() - mergeTarget.getCount(), mergeSource.getCount());
        if (mergeCount < 1) {
            return 0;
        }
        if (doMerge) {
            mergeTarget.setCount(mergeTarget.getCount() + mergeCount);
        }
        return mergeCount;
    }

    /** Compares item and components. A base stack without any changed components matches every stack of the same
     * item (as a 1.12 stack without NBT did).
     *
     * @return true if the item and components match. */
    public static boolean isMatchingItem(ItemStack base, ItemStack comparison) {
        return isMatchingItem(base, comparison, true);
    }

    /** As {@link #isMatchingItem(ItemStack, ItemStack)}, but either stack may be a list item, which then matches
     * whatever the list accepts. */
    public static boolean isMatchingItemOrList(ItemStack base, ItemStack comparison) {
        if (base.isEmpty() || comparison.isEmpty()) {
            return false;
        }
        if (base.getItem() instanceof IList list) {
            return list.matches(base, comparison);
        } else if (comparison.getItem() instanceof IList list) {
            return list.matches(comparison, base);
        }
        return isMatchingItem(base, comparison, true);
    }

    /** @return true if {@code test} matches the filter: the filter's list, if it is a list item, otherwise the same
     *         item with the same components (1.12's {@code matchesStackOrList}). */
    public static boolean matchesStackOrList(ItemStack filterOrList, ItemStack test) {
        if (filterOrList.isEmpty() || test.isEmpty()) {
            return false;
        }
        if (filterOrList.getItem() instanceof IList list) {
            return list.matches(filterOrList, test);
        }
        return ItemStack.isSameItemSameComponents(filterOrList, test);
    }

    /** Compares item, and optionally components. */
    public static boolean isMatchingItem(ItemStack base, ItemStack comparison, boolean matchComponents) {
        if (base.isEmpty() || comparison.isEmpty()) {
            return false;
        }
        if (base.getItem() != comparison.getItem()) {
            return false;
        }
        if (matchComponents && !base.getComponentsPatch().isEmpty()) {
            return ItemStack.isSameItemSameComponents(base, comparison);
        }
        return true;
    }

    /** @return An empty, nonnull list that cannot be modified (as it cannot be expanded and it has a size of 0) */
    public static NonNullList<ItemStack> listOf() {
        return NonNullList.withSize(0, ItemStack.EMPTY);
    }

    /** Creates a {@link NonNullList} of {@link ItemStack}'s with the elements given in the order that they are
     * given. The returned list is of a fixed size and cannot be expanded. */
    public static NonNullList<ItemStack> listOf(ItemStack... stacks) {
        NonNullList<ItemStack> list = NonNullList.withSize(stacks.length, ItemStack.EMPTY);
        for (int i = 0; i < stacks.length; i++) {
            list.set(i, stacks[i]);
        }
        return list;
    }

    /** Merges every stack of the same item and components into one (ignoring the maximum stack size). */
    public static NonNullList<ItemStack> mergeSameItems(List<ItemStack> items) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        for (ItemStack toAdd : items) {
            boolean found = false;
            for (ItemStack stack : stacks) {
                if (canMerge(stack, toAdd)) {
                    stack.grow(toAdd.getCount());
                    found = true;
                }
            }
            if (!found) {
                stacks.add(toAdd.copy());
            }
        }
        return stacks;
    }
}
