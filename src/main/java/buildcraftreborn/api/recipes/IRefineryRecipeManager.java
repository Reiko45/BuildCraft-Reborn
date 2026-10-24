/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.recipes;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.neoforged.neoforge.fluids.FluidStack;

/** Distiller and heat exchanger recipes.
 * <p>
 * 1.21: the built-in recipes are data maps (see {@code buildcraftreborn.lib.recipe.RefineryRecipeRegistry}), so data
 * packs can change them; recipes added through this interface are used for fluids the data maps don't cover. A
 * recipe with no output uses {@link FluidStack#EMPTY} rather than null. */
public interface IRefineryRecipeManager {
    IHeatableRecipe createHeatingRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo);

    default IHeatableRecipe addHeatableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
        return getHeatableRegistry().addRecipe(createHeatingRecipe(in, out, heatFrom, heatTo));
    }

    ICoolableRecipe createCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo);

    default ICoolableRecipe addCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
        return getCoolableRegistry().addRecipe(createCoolableRecipe(in, out, heatFrom, heatTo));
    }

    IDistillationRecipe createDistillationRecipe(FluidStack in, FluidStack outGas, FluidStack outLiquid,
        long powerRequired);

    default IDistillationRecipe addDistillationRecipe(FluidStack in, FluidStack outGas, FluidStack outLiquid,
        long powerRequired) {
        return getDistillationRegistry().addRecipe(createDistillationRecipe(in, outGas, outLiquid, powerRequired));
    }

    IRefineryRegistry<IHeatableRecipe> getHeatableRegistry();

    IRefineryRegistry<ICoolableRecipe> getCoolableRegistry();

    IRefineryRegistry<IDistillationRecipe> getDistillationRegistry();

    interface IRefineryRegistry<R extends IRefineryRecipe> {
        /** @return an unmodifiable collection containing all of the distillation recipes that satisfy the given
         *         predicate. All of the recipe objects are guaranteed to never be null. */
        Stream<R> getRecipes(Predicate<R> toReturn);

        /** @return an unmodifiable set containing all of the distillation recipes. */
        Collection<R> getAllRecipes();

        @Nullable
        R getRecipeForInput(@Nullable FluidStack fluid);

        Collection<R> removeRecipes(Predicate<R> toRemove);

        /** Adds the given recipe to the registry. Note that this will remove any existing recipes for the passed
         * recipe's {@link IRefineryRecipe#in()}
         *
         * @param recipe The recipe to add.
         * @return The input recipe. */
        R addRecipe(R recipe);
    }

    interface IRefineryRecipe {
        FluidStack in();
    }

    interface IHeatExchangerRecipe extends IRefineryRecipe {
        /** @return The fluid this turns into, or {@link FluidStack#EMPTY} if it is used up (water and lava). */
        FluidStack out();

        int heatFrom();

        int heatTo();
    }

    interface IHeatableRecipe extends IHeatExchangerRecipe {}

    interface ICoolableRecipe extends IHeatExchangerRecipe {}

    interface IDistillationRecipe extends IRefineryRecipe {
        long powerRequired();

        FluidStack outGas();

        FluidStack outLiquid();
    }
}
