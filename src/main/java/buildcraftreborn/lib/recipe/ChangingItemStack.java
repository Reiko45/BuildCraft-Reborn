/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.recipe;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import buildcraftreborn.lib.misc.ItemStackKey;

/** An {@link ItemStack} that changes between a list of stacks: the choices of an ingredient (for example any plank),
 * or the outputs of a recipe that depends on its inputs.
 * <p>
 * 1.21: tags replace the ore dictionary, and there are no wildcard damage values to expand. */
public final class ChangingItemStack extends ChangingObject<ItemStackKey> {
    /** Iterates through all of the given stacks. */
    public ChangingItemStack(List<ItemStack> stacks) {
        super(makeStackArray(stacks.toArray(new ItemStack[0])));
    }

    public ChangingItemStack(Ingredient ingredient) {
        super(makeStackArray(ingredient.getItems()));
    }

    public ChangingItemStack(ItemStack stack) {
        super(new ItemStackKey[] { stack.isEmpty() ? ItemStackKey.EMPTY : new ItemStackKey(stack) });
    }

    private static ItemStackKey[] makeStackArray(ItemStack[] stacks) {
        if (stacks.length == 0) {
            return new ItemStackKey[] { ItemStackKey.EMPTY };
        }
        ItemStackKey[] arr = new ItemStackKey[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            arr[i] = new ItemStackKey(stacks[i]);
        }
        return arr;
    }

    public boolean matches(ItemStack target) {
        for (ItemStackKey s : options) {
            if (ItemStack.isSameItem(s.baseStack, target)) {
                return true;
            }
        }
        return false;
    }
}
