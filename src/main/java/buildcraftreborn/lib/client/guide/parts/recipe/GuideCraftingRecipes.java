/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import buildcraftreborn.lib.client.guide.parts.GuidePartFactory;

/** Crafting table recipes, indexed by the items they use and make.
 * <p>
 * 1.21: recipes come from the world (the client's copy of the server's recipes), so the index is made again when they
 * change. Special recipes (with no fixed ingredients, like 1.12's {@code IRecipeViewable}) aren't shown. */
public enum GuideCraftingRecipes implements IStackRecipes {
    INSTANCE;

    private Map<Item, Set<CraftingRecipe>> inputIndexMap, outputIndexMap;

    /** Forgets the index (when recipes change). */
    public void invalidate() {
        inputIndexMap = null;
        outputIndexMap = null;
    }

    @Nullable
    private static Level level() {
        return Minecraft.getInstance().level;
    }

    private void generateIndices(Level level) {
        if (inputIndexMap != null) {
            return;
        }
        inputIndexMap = new IdentityHashMap<>();
        outputIndexMap = new IdentityHashMap<>();
        RegistryAccess registries = level.registryAccess();
        for (RecipeHolder<CraftingRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            CraftingRecipe recipe = holder.value();
            for (Ingredient ing : recipe.getIngredients()) {
                for (ItemStack stack : ing.getItems()) {
                    appendIndex(stack, recipe, inputIndexMap);
                }
            }
            ItemStack output = recipe.getResultItem(registries);
            if (!output.isEmpty()) {
                appendIndex(output, recipe, outputIndexMap);
            }
        }
    }

    private static void appendIndex(ItemStack stack, CraftingRecipe recipe, Map<Item, Set<CraftingRecipe>> indexMap) {
        indexMap.computeIfAbsent(stack.getItem(), i -> new LinkedHashSet<>()).add(recipe);
    }

    @Override
    public List<GuidePartFactory> getUsages(ItemStack target) {
        Level level = level();
        if (level == null) {
            return List.of();
        }
        generateIndices(level);
        Set<CraftingRecipe> recipes = inputIndexMap.get(target.getItem());
        if (recipes == null) {
            return List.of();
        }
        List<GuidePartFactory> list = new ArrayList<>();
        for (CraftingRecipe recipe : recipes) {
            if (recipe.getIngredients().stream().anyMatch(ing -> ing.test(target))) {
                GuidePartFactory factory = GuideCraftingFactory.getFactory(recipe, level.registryAccess());
                if (factory != null) {
                    list.add(factory);
                }
            }
        }
        return list;
    }

    @Override
    public List<GuidePartFactory> getRecipes(ItemStack target) {
        Level level = level();
        if (level == null) {
            return List.of();
        }
        generateIndices(level);
        Set<CraftingRecipe> recipes = outputIndexMap.get(target.getItem());
        if (recipes == null) {
            return List.of();
        }
        List<GuidePartFactory> list = new ArrayList<>();
        for (CraftingRecipe recipe : recipes) {
            if (ItemStack.isSameItem(recipe.getResultItem(level.registryAccess()), target)) {
                GuidePartFactory factory = GuideCraftingFactory.getFactory(recipe, level.registryAccess());
                if (factory != null) {
                    list.add(factory);
                }
            }
        }
        return list;
    }
}
