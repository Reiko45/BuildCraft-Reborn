/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

import buildcraftreborn.lib.client.guide.parts.GuidePartFactory;

/** Furnace recipes. (A furnace's page lists them all.) */
public enum GuideSmeltingRecipes implements IStackRecipes {
    INSTANCE;

    @Override
    public List<GuidePartFactory> getUsages(ItemStack stack) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        boolean all = stack.is(Items.FURNACE);
        List<GuidePartFactory> list = new ArrayList<>();
        for (RecipeHolder<SmeltingRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
            SmeltingRecipe recipe = holder.value();
            if (recipe.getIngredients().isEmpty()) {
                continue;
            }
            if (all || recipe.getIngredients().get(0).test(stack)) {
                list.add(new GuideSmeltingFactory(recipe.getIngredients().get(0),
                    recipe.getResultItem(level.registryAccess())));
            }
        }
        return list;
    }

    @Override
    public List<GuidePartFactory> getRecipes(ItemStack stack) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        List<GuidePartFactory> list = new ArrayList<>();
        for (RecipeHolder<SmeltingRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
            SmeltingRecipe recipe = holder.value();
            ItemStack output = recipe.getResultItem(level.registryAccess());
            if (!recipe.getIngredients().isEmpty() && ItemStack.isSameItem(output, stack)) {
                list.add(new GuideSmeltingFactory(recipe.getIngredients().get(0), output));
            }
        }
        return list;
    }
}
