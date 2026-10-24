/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.recipe;

import java.util.Arrays;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.parts.GuidePartFactory;
import buildcraftreborn.lib.recipe.ChangingItemStack;

/** Makes a {@link GuideCrafting} for a crafting recipe. */
public class GuideCraftingFactory implements GuidePartFactory {
    private final ChangingItemStack[][] input;
    private final ChangingItemStack output;
    private final int hash;

    public GuideCraftingFactory(Ingredient[][] input, ItemStack output) {
        this.input = new ChangingItemStack[input.length][];
        for (int x = 0; x < input.length; x++) {
            this.input[x] = new ChangingItemStack[input[x].length];
            for (int y = 0; y < input[x].length; y++) {
                this.input[x][y] = new ChangingItemStack(input[x][y]);
            }
        }
        this.output = new ChangingItemStack(output);
        this.hash = Arrays.deepHashCode(new Object[] { this.input, this.output });
    }

    /** @return A factory for the recipe, or null if it can't be shown (a special recipe with no ingredients). */
    @Nullable
    public static GuidePartFactory getFactory(Recipe<?> recipe, RegistryAccess registries) {
        ItemStack output = recipe.getResultItem(registries);
        NonNullList<Ingredient> input = recipe.getIngredients();
        if (input.isEmpty() || output.isEmpty()) {
            return null;
        }
        Ingredient[][] matrix = new Ingredient[3][3];
        int maxX = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 3;
        int maxY = recipe instanceof ShapedRecipe shaped ? shaped.getHeight() : 3;
        int offsetX = maxX == 1 ? 1 : 0;
        int offsetY = maxY == 1 ? 1 : 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (x < offsetX || y < offsetY) {
                    matrix[x][y] = Ingredient.EMPTY;
                    continue;
                }
                int i = x - offsetX + (y - offsetY) * maxX;
                if (i >= input.size() || x - offsetX >= maxX) {
                    matrix[x][y] = Ingredient.EMPTY;
                } else {
                    matrix[x][y] = input.get(i);
                }
            }
        }
        return new GuideCraftingFactory(matrix, output);
    }

    @Override
    public GuideCrafting createNew(GuiGuide gui) {
        return new GuideCrafting(gui, input, output);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        GuideCraftingFactory other = (GuideCraftingFactory) obj;
        return hash == other.hash && Arrays.deepEquals(input, other.input) && output.equals(other.output);
    }
}
