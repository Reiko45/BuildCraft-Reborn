/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import buildcraftreborn.api.mj.MjAPI;

/** An assembly recipe with a fixed set of ingredients, one output and a fixed MJ cost.
 * <p>
 * 1.21: written in recipe JSON as {@code {"type": "buildcraftreborn:assembly", "mj": 500, "ingredients": [{"ingredient":
 * ..., "count": 2}], "result": {...}}}. {@code mj} is in whole MJ. */
public class AssemblyRecipeBasic extends AssemblyRecipe {
    /** Set by BuildCraft Silicon when it registers the serializer. */
    public static Supplier<RecipeSerializer<AssemblyRecipeBasic>> serializer = () -> {
        throw new IllegalStateException("BuildCraft Silicon isn't loaded!");
    };

    public static final MapCodec<AssemblyRecipeBasic> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.LONG.fieldOf("mj").forGetter(r -> r.requiredMicroJoules / MjAPI.MJ),
        IngredientStack.LIST_CODEC.fieldOf("ingredients").forGetter(r -> List.copyOf(r.requiredStacks)),
        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.output.iterator().next())
    ).apply(i, (mj, ingredients, result) -> new AssemblyRecipeBasic(mj * MjAPI.MJ, ingredients, result)));

    public static final StreamCodec<RegistryFriendlyByteBuf, AssemblyRecipeBasic> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG, r -> r.requiredMicroJoules,
        IngredientStack.STREAM_CODEC.apply(ByteBufCodecs.list()), r -> List.copyOf(r.requiredStacks),
        ItemStack.STREAM_CODEC, r -> r.output.iterator().next(),
        AssemblyRecipeBasic::new);

    private final long requiredMicroJoules;
    private final ImmutableSet<IngredientStack> requiredStacks;
    private final ImmutableSet<ItemStack> output;

    public AssemblyRecipeBasic(long requiredMicroJoules, Iterable<IngredientStack> requiredStacks, ItemStack output) {
        this.requiredMicroJoules = requiredMicroJoules;
        this.requiredStacks = ImmutableSet.copyOf(requiredStacks);
        this.output = ImmutableSet.of(output);
    }

    public AssemblyRecipeBasic(ResourceLocation name, long requiredMicroJoules, Iterable<IngredientStack> requiredStacks,
        ItemStack output) {
        this(requiredMicroJoules, requiredStacks, output);
        setRegistryName(name);
    }

    @Override
    public Set<ItemStack> getOutputs(NonNullList<ItemStack> inputs) {
        if (requiredStacks.stream().allMatch((definition) -> inputs.stream().anyMatch(
            (stack) -> !stack.isEmpty() && definition.ingredient.test(stack) && stack.getCount() >= definition.count))) {
            return output;
        }
        return Collections.emptySet();
    }

    @Override
    public Set<ItemStack> getOutputPreviews() {
        return output;
    }

    @Override
    public Set<IngredientStack> getInputsFor(ItemStack output) {
        return requiredStacks;
    }

    @Override
    public long getRequiredMicroJoulesFor(ItemStack output) {
        return requiredMicroJoules;
    }

    public long getRequiredMicroJoules() {
        return requiredMicroJoules;
    }

    public ItemStack getOutput() {
        return output.iterator().next();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer.get();
    }
}
