/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.recipes;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

public interface IProgrammingRecipeManager {
    void addRecipe(IProgrammingRecipe recipe);

    void removeRecipe(String id);

    void removeRecipe(IProgrammingRecipe recipe);

    @Nullable
    IProgrammingRecipe getRecipe(String id);

    Collection<IProgrammingRecipe> getRecipes();
}
