/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.lists;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/** Decides whether two stacks are "the same type" or "the same material" for a list line with those options on. */
public abstract class ListMatchHandler {
    public enum Type {
        TYPE,
        MATERIAL,
        CLASS
    }

    public abstract boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise);

    public abstract boolean isValidSource(Type type, ItemStack stack);

    /** Get custom client examples.
     *
     * @return A List (even empty!) if the examples satisfy this handler, null if iteration and .matches should be used
     *         instead. */
    @Nullable
    public NonNullList<ItemStack> getClientExamples(Type type, ItemStack stack) {
        return null;
    }
}
