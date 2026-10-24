/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.recipes;

import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** A recipe for the assembly table.
 * <p>
 * 1.21: assembly recipes are ordinary data-pack recipes of type {@code buildcraftreborn:assembly} (1.12 kept them in
 * a Forge registry). The {@link Recipe} methods are implemented on top of the 1.12 ones, so a recipe still works out
 * all its possible outputs from what is in the table. The registry name is set by BuildCraft from the recipe's id. */
public abstract class AssemblyRecipe implements Recipe<AssemblyRecipe.Input>, Comparable<AssemblyRecipe> {
    /** Set by BuildCraft Silicon when it registers the recipe type. */
    public static Supplier<RecipeType<AssemblyRecipe>> type = () -> {
        throw new IllegalStateException("BuildCraft Silicon isn't loaded!");
    };

    private ResourceLocation name;

    /** The outputs this recipe can generate with any of the given inputs
     * 
     * @param inputs Current ingredients in the assembly table
     * @return A Set containing all possible outputs given the given inputs or an empty one if nothing can be assembled
     *         from the given inputs */
    public abstract Set<ItemStack> getOutputs(NonNullList<ItemStack> inputs);

    /** Used to determine all outputs from this recipe for recipe previews (guide book and/or JEI) */
    public abstract Set<ItemStack> getOutputPreviews();

    /** Used to determine what items to use up for the given output
     * 
     * @param output The output we want to know the inputs for, only ever called using stacks obtained from getOutputs
     *            or getOutputPreviews */
    public abstract Set<IngredientStack> getInputsFor(ItemStack output);

    /** Used to determine how much MJ is required to asemble the given output item
     * 
     * @param output The output we want to know the MJ cost for, only ever called using stacks obtained from getOutputs
     *            or getOutputPreviews */
    public abstract long getRequiredMicroJoulesFor(ItemStack output);

    // Recipe

    @Override
    public boolean matches(Input input, Level level) {
        return !getOutputs(input.items()).isEmpty();
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        Set<ItemStack> outputs = getOutputs(input.items());
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.iterator().next().copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        Set<ItemStack> previews = getOutputPreviews();
        return previews.isEmpty() ? ItemStack.EMPTY : previews.iterator().next();
    }

    @Override
    public boolean isSpecial() {
        // Not shown in the recipe book
        return true;
    }

    @Override
    public RecipeType<?> getType() {
        return type.get();
    }

    // Identity

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AssemblyRecipe that = (AssemblyRecipe) o;
        return name != null && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name == null ? System.identityHashCode(this) : name.hashCode();
    }

    @Override
    public int compareTo(AssemblyRecipe o) {
        return String.valueOf(name).compareTo(String.valueOf(o.name));
    }

    public AssemblyRecipe setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
    public ResourceLocation getRegistryName() {
        return name;
    }

    /** What is in an assembly table, as a {@link RecipeInput}. */
    public record Input(NonNullList<ItemStack> items) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return items.get(index);
        }

        @Override
        public int size() {
            return items.size();
        }
    }
}
