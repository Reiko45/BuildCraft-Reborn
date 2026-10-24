/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.recipe;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.transport.BCTransportRecipes;

/** Turns one pipe into another, keeping its paint: an item pipe and sealant make a fluid pipe, an item pipe and
 * redstone a kinesis pipe, and a fluid or kinesis pipe on its own turns back into the item pipe (the "undo"
 * recipes).
 * <p>
 * 1.12 registered one shapeless recipe per colour for each of these. Here one recipe covers every colour by copying
 * the {@code colour} component across. */
public record PipeUpgradeRecipe(String group, Item from, Optional<Ingredient> additive, Item result)
    implements CraftingRecipe {

    public static final MapCodec<PipeUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        com.mojang.serialization.Codec.STRING.optionalFieldOf("group", "").forGetter(PipeUpgradeRecipe::group),
        BuiltInRegistries.ITEM.byNameCodec().fieldOf("from").forGetter(PipeUpgradeRecipe::from),
        Ingredient.CODEC_NONEMPTY.optionalFieldOf("additive").forGetter(PipeUpgradeRecipe::additive),
        BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(PipeUpgradeRecipe::result))
        .apply(inst, PipeUpgradeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PipeUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, PipeUpgradeRecipe::group,
        ByteBufCodecs.registry(Registries.ITEM), PipeUpgradeRecipe::from,
        ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), PipeUpgradeRecipe::additive,
        ByteBufCodecs.registry(Registries.ITEM), PipeUpgradeRecipe::result,
        PipeUpgradeRecipe::new);

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findPipe(input) != null;
    }

    /** @return The pipe being changed, or null if the grid isn't exactly that pipe (and the additive). */
    private ItemStack findPipe(CraftingInput input) {
        ItemStack pipe = null;
        boolean foundAdditive = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (pipe == null && stack.is(from)) {
                pipe = stack;
            } else if (!foundAdditive && additive.isPresent() && additive.get().test(stack)) {
                foundAdditive = true;
            } else {
                return null;
            }
        }
        if (pipe == null || foundAdditive != additive.isPresent()) {
            return null;
        }
        return pipe;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack pipe = findPipe(input);
        ItemStack out = new ItemStack(result);
        if (pipe != null) {
            DyeColor colour = pipe.get(BCItemComponents.COLOUR.get());
            if (colour != null) {
                out.set(BCItemComponents.COLOUR.get(), colour);
            }
        }
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= (additive.isPresent() ? 2 : 1);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(result);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(Ingredient.of(from));
        additive.ifPresent(list::add);
        return list;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BCTransportRecipes.PIPE_UPGRADE.get();
    }
}
