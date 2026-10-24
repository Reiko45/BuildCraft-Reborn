/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.recipes;

import java.util.List;

import net.minecraft.world.item.ItemStack;

/** A programming table recipe: an input that can be turned into one of several options. */
public interface IProgrammingRecipe {
    String getId();

    /** Get a list (size at least width*height) of ItemStacks representing options.
     *
     * @param width The width of the Programming Table panel.
     * @param height The height of the Programming Table panel. */
    List<ItemStack> getOptions(int width, int height);

    /** Get the power cost (in micro MJ) of a given option ItemStack. */
    long getPowerCost(ItemStack option);

    /** @param input The input stack.
     * @return Whether this recipe applies to the given input stack. */
    boolean canCraft(ItemStack input);

    /** Craft the input ItemStack with the given option into an output ItemStack. */
    ItemStack craft(ItemStack input, ItemStack option);
}
