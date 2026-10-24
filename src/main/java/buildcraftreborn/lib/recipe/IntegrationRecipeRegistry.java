/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.recipe;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import buildcraftreborn.api.recipes.IIntegrationRecipeRegistry;
import buildcraftreborn.api.recipes.IntegrationRecipe;

/** Integration table recipes: ones added from code, plus data-pack recipes of type
 * {@code buildcraftreborn:integration} (pass a level to see those). BuildCraft 8.0 has none (its integration table says
 * "No recipes use this!"); addons can add some. */
public enum IntegrationRecipeRegistry implements IIntegrationRecipeRegistry {
    INSTANCE;

    private final List<IntegrationRecipe> recipes = new ArrayList<>();

    @Override
    public void addRecipe(IntegrationRecipe recipe) {
        recipes.add(recipe);
    }

    @Override
    public Iterable<IntegrationRecipe> getAllRecipes() {
        return recipes;
    }

    @Override
    @Nullable
    public IntegrationRecipe getRecipeFor(ItemStack target, NonNullList<ItemStack> toIntegrate) {
        return getRecipeFor(null, target, toIntegrate);
    }

    @Override
    @Nullable
    public IntegrationRecipe getRecipe(ResourceLocation name) {
        return getRecipe(null, name);
    }

    /** @return The code recipes, then the level's data-pack recipes (if there's a level). */
    public List<IntegrationRecipe> getAll(@Nullable Level level) {
        List<IntegrationRecipe> all = new ArrayList<>(recipes);
        if (level != null) {
            for (RecipeHolder<IntegrationRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(IntegrationRecipe.type.get())) {
                IntegrationRecipe recipe = holder.value();
                if (recipe.name == null) {
                    recipe.name = holder.id();
                }
                all.add(recipe);
            }
        }
        return all;
    }

    @Nullable
    public IntegrationRecipe getRecipeFor(@Nullable Level level, ItemStack target, NonNullList<ItemStack> toIntegrate) {
        for (IntegrationRecipe recipe : getAll(level)) {
            if (!recipe.getOutput(target, toIntegrate).isEmpty()) {
                return recipe;
            }
        }
        return null;
    }

    @Nullable
    public IntegrationRecipe getRecipe(@Nullable Level level, ResourceLocation name) {
        for (IntegrationRecipe recipe : getAll(level)) {
            if (name.equals(recipe.name)) {
                return recipe;
            }
        }
        return null;
    }
}
