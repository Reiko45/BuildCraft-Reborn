/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import buildcraftreborn.silicon.BCSilicon;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.item.ItemPluggableFacade;

/** A facade on its own in a crafting grid turns into the hollow version (or back). */
public class FacadeSwapRecipe extends CustomRecipe {
    public FacadeSwapRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static ItemStack findFacade(CraftingInput input) {
        ItemStack found = ItemStack.EMPTY;
        for (int s = 0; s < input.size(); s++) {
            ItemStack stack = input.getItem(s);
            if (!stack.isEmpty()) {
                if (!found.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                found = stack;
            }
        }
        return found.is(BCSiliconItems.PLUG_FACADE.get()) ? found : ItemStack.EMPTY;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !findFacade(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack facade = findFacade(input);
        if (facade.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return BCSiliconItems.PLUG_FACADE.get().createItemStack(
            ItemPluggableFacade.getStates(facade).withSwappedIsHollow());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 1 && height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BCSilicon.FACADE_SWAP_SERIALIZER.get();
    }
}
