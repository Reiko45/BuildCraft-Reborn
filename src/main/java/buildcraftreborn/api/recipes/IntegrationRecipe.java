/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.recipes;

import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** A recipe for the integration table. 1.21: integration recipes are data-pack recipes of type
 * {@code buildcraftreborn:integration} (BuildCraft 8.0 itself adds none). The name is set by BuildCraft from the
 * recipe id. */
public abstract class IntegrationRecipe implements Recipe<IntegrationRecipe.Input> {
    /** Set by BuildCraft Silicon when it registers the recipe type. */
    public static Supplier<RecipeType<IntegrationRecipe>> type = () -> {
        throw new IllegalStateException("BuildCraft Silicon isn't loaded!");
    };

    public ResourceLocation name;

    public IntegrationRecipe(ResourceLocation name) {
        this.name = name;
    }

    /** Determines the output of this recipe
     * 
     * @param target the stack in the middle to integrate the components into
     * @param toIntegrate All available stacks to integrate (not all have to be used up in this recipe)
     * @return The output to produce based on the inputs provided or an empty stack if the recipe isn't valid */
    public abstract ItemStack getOutput(ItemStack target, NonNullList<ItemStack> toIntegrate);

    /** Determines the components to use when crafting finishes
     * 
     * @param output The generated outputted, determined by getOutput
     * @return The components to use up */
    public abstract ImmutableList<IngredientStack> getRequirements(ItemStack output);

    /** Determines the amount of MJ required to integrate
     * 
     * @param output The output that would be generated
     * @return The powercost in microjoules */
    public abstract long getRequiredMicroJoules(ItemStack output);

    public abstract IngredientStack getCenterStack();

    // Recipe

    @Override
    public boolean matches(Input input, Level level) {
        return !getOutput(input.target(), input.toIntegrate()).isEmpty();
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return getOutput(input.target(), input.toIntegrate()).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeType<?> getType() {
        return type.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IntegrationRecipe that = (IntegrationRecipe) o;
        return name != null && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name == null ? System.identityHashCode(this) : name.hashCode();
    }

    /** The centre stack and the stacks around it. */
    public record Input(ItemStack target, NonNullList<ItemStack> toIntegrate) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return index == 0 ? target : toIntegrate.get(index - 1);
        }

        @Override
        public int size() {
            return 1 + toIntegrate.size();
        }
    }
}
