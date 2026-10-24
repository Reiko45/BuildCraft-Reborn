/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.recipes;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/** An ingredient with a count. 1.21: has a codec (for recipe JSON) and a stream codec (recipes are synced to
 * clients). 1.12's {@code IngredientStack.of(Object)} took ore dictionary names; use {@link #of(Ingredient)} with an
 * item tag ingredient instead. */
public final class IngredientStack {
    public static final Codec<IngredientStack> CODEC = RecordCodecBuilder.create(i -> i.group(
        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(s -> s.ingredient),
        Codec.intRange(1, 64).optionalFieldOf("count", 1).forGetter(s -> s.count)
    ).apply(i, IngredientStack::new));

    public static final Codec<List<IngredientStack>> LIST_CODEC = CODEC.listOf();

    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientStack> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, s -> s.ingredient,
        ByteBufCodecs.VAR_INT, s -> s.count,
        IngredientStack::new);

    public final Ingredient ingredient;
    public final int count;

    public IngredientStack(Ingredient ingredient, int count) {
        this.ingredient = ingredient;
        this.count = count;
    }

    public IngredientStack(Ingredient ingredient) {
        this(ingredient, 1);
    }

    public static IngredientStack of(Ingredient ingredient) {
        return new IngredientStack(ingredient);
    }

    public static IngredientStack of(ItemLike item) {
        return new IngredientStack(Ingredient.of(item));
    }

    public static IngredientStack of(ItemStack stack) {
        return new IngredientStack(Ingredient.of(stack));
    }
}
