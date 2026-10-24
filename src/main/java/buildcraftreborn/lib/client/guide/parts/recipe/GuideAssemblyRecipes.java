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
import net.minecraft.world.level.Level;

import buildcraftreborn.api.recipes.AssemblyRecipe;
import buildcraftreborn.api.recipes.IngredientStack;
import buildcraftreborn.lib.client.guide.parts.GuidePartFactory;
import buildcraftreborn.lib.recipe.AssemblyRecipeRegistry;
import buildcraftreborn.lib.recipe.ChangingItemStack;
import buildcraftreborn.lib.recipe.ChangingObject;
import buildcraftreborn.silicon.BCSiliconBlocks;

/** Assembly table recipes. (The assembly table's page lists them all.) */
public enum GuideAssemblyRecipes implements IStackRecipes {
    INSTANCE;

    @Override
    public List<GuidePartFactory> getUsages(ItemStack stack) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        List<GuidePartFactory> usages = new ArrayList<>();
        boolean all = stack.is(BCSiliconBlocks.ASSEMBLY_TABLE.get().asItem());
        for (AssemblyRecipe recipe : AssemblyRecipeRegistry.getAll(level).values()) {
            for (ItemStack output : recipe.getOutputPreviews()) {
                if (all || recipe.getInputsFor(output).stream().anyMatch(def -> def.ingredient.test(stack))) {
                    usages.add(getFactory(recipe, output));
                }
            }
        }
        return usages;
    }

    @Override
    public List<GuidePartFactory> getRecipes(ItemStack stack) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        List<GuidePartFactory> recipes = new ArrayList<>();
        for (AssemblyRecipe recipe : AssemblyRecipeRegistry.getAll(level).values()) {
            for (ItemStack output : recipe.getOutputPreviews()) {
                if (ItemStack.isSameItem(output, stack)) {
                    recipes.add(getFactory(recipe, output));
                }
            }
        }
        return recipes;
    }

    private static GuideAssemblyFactory getFactory(AssemblyRecipe recipe, ItemStack output) {
        ChangingItemStack[] stacks = recipe.getInputsFor(output).stream().map(GuideAssemblyRecipes::stacksOf)
            .toArray(ChangingItemStack[]::new);
        return new GuideAssemblyFactory(stacks, new ChangingItemStack(output),
            new ChangingObject<>(new Long[] { recipe.getRequiredMicroJoulesFor(output) }));
    }

    private static ChangingItemStack stacksOf(IngredientStack definition) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack stack : definition.ingredient.getItems()) {
            items.add(stack.copyWithCount(definition.count));
        }
        return new ChangingItemStack(items);
    }
}
