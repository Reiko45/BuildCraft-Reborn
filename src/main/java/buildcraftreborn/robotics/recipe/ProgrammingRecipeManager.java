/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.recipe;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import buildcraftreborn.api.recipes.IProgrammingRecipe;
import buildcraftreborn.api.recipes.IProgrammingRecipeManager;

/** The programming table's recipes, by id, in the order they were added. */
public enum ProgrammingRecipeManager implements IProgrammingRecipeManager {
    INSTANCE;

    private final Map<String, IProgrammingRecipe> recipes = new LinkedHashMap<>();

    @Override
    public void addRecipe(IProgrammingRecipe recipe) {
        recipes.put(recipe.getId(), recipe);
    }

    @Override
    public void removeRecipe(String id) {
        recipes.remove(id);
    }

    @Override
    public void removeRecipe(IProgrammingRecipe recipe) {
        recipes.remove(recipe.getId());
    }

    @Override
    @Nullable
    public IProgrammingRecipe getRecipe(String id) {
        return recipes.get(id);
    }

    @Override
    public Collection<IProgrammingRecipe> getRecipes() {
        return Collections.unmodifiableCollection(recipes.values());
    }
}
