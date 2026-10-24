/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.recipes;

import java.util.Iterator;

public interface IIntegrationRecipeRegistry extends IIntegrationRecipeProvider {
    void addRecipe(IntegrationRecipe recipe);

    /**
     * Gets all of the simple recipes that are registered. Note that you *can* use the returned iterator's
     * {@link Iterator#remove()} method to remove recipes from this registry.
     */
    Iterable<IntegrationRecipe> getAllRecipes();

}
