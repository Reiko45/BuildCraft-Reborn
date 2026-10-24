/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.recipes;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;

/** Provides a way of registering complex recipes without needing to register every possible variant. If you want the
 * recipes to be viewable in JEI and the guide book then you will *also* need to implement the bc lib class
 * IIntegrationRecipeViewable. */
public interface IIntegrationRecipeProvider {
    /** Gets an integration recipe for the given ingredients.
     * 
     * @param target The center itemstack.
     * @param toIntegrate A list of stacks to try to integrate to the
     * @return */
    @Nullable
    IntegrationRecipe getRecipeFor(ItemStack target, NonNullList<ItemStack> toIntegrate);

    /**
     * Returns recipe by it's name
     */
    IntegrationRecipe getRecipe(ResourceLocation name);
}
