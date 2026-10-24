/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.client.guide.parts.GuidePartFactory;

/** Finds the recipes (that make) and usages (that use) a stack, from every registered kind of recipe. */
public final class RecipeLookupHelper {
    public static final Map<String, IStackRecipes> handlerTypes = new LinkedHashMap<>();

    static {
        handlerTypes.put("crafting", GuideCraftingRecipes.INSTANCE);
        handlerTypes.put("smelting", GuideSmeltingRecipes.INSTANCE);
        handlerTypes.put("assembling", GuideAssemblyRecipes.INSTANCE);
    }

    private RecipeLookupHelper() {}

    public static List<GuidePartFactory> getAllUsages(ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();
        for (IStackRecipes handler : handlerTypes.values()) {
            List<GuidePartFactory> recipes = handler.getUsages(stack);
            if (recipes != null) {
                list.addAll(recipes);
            }
        }
        return list;
    }

    public static List<GuidePartFactory> getAllRecipes(ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();
        for (IStackRecipes handler : handlerTypes.values()) {
            List<GuidePartFactory> recipes = handler.getRecipes(stack);
            if (recipes != null) {
                list.addAll(recipes);
            }
        }
        return list;
    }
}
