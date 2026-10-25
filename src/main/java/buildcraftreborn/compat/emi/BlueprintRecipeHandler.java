/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.emi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.lib.gui.ContainerBC_Neptune;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

/** Fills a 3x3 phantom grid from a crafting recipe: the blueprint of the auto workbench and the advanced crafting
 * table, and the recipe packager's pattern. Nothing is taken from the player, so any crafting recipe can be filled,
 * whatever the player has; for each slot this picks an item the player has, if they have one of those that fit. */
public class BlueprintRecipeHandler<C extends ContainerBC_Neptune> implements EmiRecipeHandler<C> {
    private static final int SLOTS = 9;

    private final Function<C, IItemHandler> grid;

    public BlueprintRecipeHandler(Function<C, IItemHandler> grid) {
        this.grid = grid;
    }

    /** The player's own inventory (EmiPlayerInventory.of would ask this handler again). */
    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<C> screen) {
        List<EmiStack> stacks = new ArrayList<>();
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty()) {
                    stacks.add(EmiStack.of(stack));
                }
            }
        }
        return new EmiPlayerInventory(stacks);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.getInputs().size() <= SLOTS;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<C> context) {
        return true;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<C> context) {
        Player player = Minecraft.getInstance().player;
        List<EmiIngredient> inputs = recipe.getInputs();
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < SLOTS; slot++) {
            stacks.add(slot < inputs.size() ? pick(inputs.get(slot), player) : ItemStack.EMPTY);
        }
        C container = context.getScreenHandler();
        container.sendSetPhantomSlots(grid.apply(container), stacks);
        return true;
    }

    private static ItemStack pick(EmiIngredient ingredient, Player player) {
        ItemStack first = ItemStack.EMPTY;
        for (EmiStack option : ingredient.getEmiStacks()) {
            ItemStack stack = option.getItemStack();
            if (stack.isEmpty()) {
                continue;
            }
            if (player != null && player.getInventory().contains(stack)) {
                return stack.copyWithCount(1);
            }
            if (first.isEmpty()) {
                first = stack.copyWithCount(1);
            }
        }
        return first;
    }
}
