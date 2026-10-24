/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.lib.gui.ContainerBC_Neptune;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

/** Moves a crafting recipe into a 3x3 phantom blueprint (the auto workbench's and the advanced crafting table's): the
 * items shown in JEI's grid become the blueprint. Nothing is taken from the player. */
public class BlueprintTransferHandler<C extends ContainerBC_Neptune>
    implements IRecipeTransferHandler<C, RecipeHolder<CraftingRecipe>> {

    private final Class<C> containerClass;
    private final MenuType<C> menuType;
    private final Function<C, IItemHandler> blueprint;

    public BlueprintTransferHandler(Class<C> containerClass, MenuType<C> menuType, Function<C, IItemHandler> blueprint) {
        this.containerClass = containerClass;
        this.menuType = menuType;
        this.blueprint = blueprint;
    }

    @Override
    public Class<? extends C> getContainerClass() {
        return containerClass;
    }

    @Override
    public Optional<MenuType<C>> getMenuType() {
        return Optional.of(menuType);
    }

    @Override
    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(C container, RecipeHolder<CraftingRecipe> recipe,
        IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            List<IRecipeSlotView> inputs = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
            List<ItemStack> stacks = new ArrayList<>();
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = slot < inputs.size() ? inputs.get(slot).getDisplayedItemStack()
                    .orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
                stacks.add(stack.copyWithCount(stack.isEmpty() ? 0 : 1));
            }
            container.sendSetPhantomSlots(blueprint.apply(container), stacks);
        }
        return null;
    }
}
