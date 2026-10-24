/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.recipes;

/** Holds BuildCraft's recipe managers. The refinery manager is set when the factory module initialises, the
 * integration registry when the silicon module does. */
public final class BuildcraftRecipeRegistry {

    public static IIntegrationRecipeRegistry integrationRecipes;
    public static IRefineryRecipeManager refineryRecipes;
    public static IProgrammingRecipeManager programmingRecipes;

    private BuildcraftRecipeRegistry() {}
}
