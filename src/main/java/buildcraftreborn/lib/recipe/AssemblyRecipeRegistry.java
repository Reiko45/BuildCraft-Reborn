/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.recipe;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import buildcraftreborn.api.recipes.AssemblyRecipe;

/** Finds assembly recipes. 1.21: they're data-pack recipes (type {@code buildcraftreborn:assembly}), so this reads the
 * level's recipe manager (which clients get a copy of) and names each recipe after its id. */
public final class AssemblyRecipeRegistry {
    private AssemblyRecipeRegistry() {}

    /** @return Every assembly recipe, by id, in a stable order. */
    public static Map<ResourceLocation, AssemblyRecipe> getAll(Level level) {
        Map<ResourceLocation, AssemblyRecipe> map = new LinkedHashMap<>();
        for (RecipeHolder<AssemblyRecipe> holder : level.getRecipeManager().getAllRecipesFor(
            AssemblyRecipe.type.get())) {
            AssemblyRecipe recipe = holder.value();
            if (recipe.getRegistryName() == null) {
                recipe.setRegistryName(holder.id());
            }
            map.put(holder.id(), recipe);
        }
        return map;
    }

    @Nullable
    public static AssemblyRecipe get(Level level, ResourceLocation id) {
        return level.getRecipeManager().byKey(id).map(holder -> holder.value() instanceof AssemblyRecipe recipe
            ? (AssemblyRecipe) recipe.setRegistryName(holder.id()) : null).orElse(null);
    }
}
