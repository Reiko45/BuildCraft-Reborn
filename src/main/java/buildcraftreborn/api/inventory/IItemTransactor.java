/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.inventory;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.IStackFilter;

public interface IItemTransactor {
    /** @param stack The stack to insert. Must not be null!
     * @param allOrNone If true then either the entire stack will be used or none of it.
     * @param simulate If true then the in-world state of this will not be changed.
     * @return The overflow stack. Will be {@link ItemStack#EMPTY} if all of it was accepted. */
    ItemStack insert(ItemStack stack, boolean allOrNone, boolean simulate);

    /** Similar to {@link #insert(ItemStack, boolean, boolean)} but probably be more efficient at inserting lots of
     * items.
     *
     * @param stacks The stacks to insert. Must not be null!
     * @param simulate If true then the in-world state of this will not be changed.
     * @return The overflow stacks. Will be an empty list if all of it was accepted. */
    default NonNullList<ItemStack> insert(NonNullList<ItemStack> stacks, boolean simulate) {
        NonNullList<ItemStack> leftOver = NonNullList.create();
        for (ItemStack stack : stacks) {
            ItemStack leftOverStack = insert(stack, false, simulate);
            if (!leftOverStack.isEmpty()) {
                leftOver.add(leftOverStack);
            }
        }
        return leftOver;
    }

    /** Extracts a number of items that match the given filter
     *
     * @param filter The filter that MUST be met by the extracted stack. Null means no filter - it can be any item.
     * @param min The minimum number of items to extract, or 0 if not enough items can be extracted
     * @param max The maximum number of items to extract.
     * @param simulate If true then the in-world state of this will not be changed.
     * @return The stack that was extracted, or {@link ItemStack#EMPTY} if it could not be. */
    ItemStack extract(@Nullable IStackFilter filter, int min, int max, boolean simulate);

    default boolean canFullyAccept(ItemStack stack) {
        return insert(stack, true, true).isEmpty();
    }

    default boolean canPartiallyAccept(ItemStack stack) {
        return insert(stack, false, true).getCount() < stack.getCount();
    }

    @FunctionalInterface
    interface IItemInsertable extends IItemTransactor {
        @Override
        default ItemStack extract(@Nullable IStackFilter filter, int min, int max, boolean simulate) {
            return ItemStack.EMPTY;
        }
    }

    @FunctionalInterface
    interface IItemExtractable extends IItemTransactor {
        @Override
        default ItemStack insert(ItemStack stack, boolean allOrNone, boolean simulate) {
            return stack;
        }
    }
}
