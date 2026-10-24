/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport;

import net.minecraft.world.item.crafting.RecipeSerializer;

import net.neoforged.neoforge.registries.DeferredHolder;

import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.transport.recipe.PipeUpgradeRecipe;

/** Transport's recipe serializers. The recipes themselves are data ({@code BCRecipes} makes them). */
public final class BCTransportRecipes {
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PipeUpgradeRecipe>> PIPE_UPGRADE =
        BCRegistries.RECIPE_SERIALIZERS.register("pipe_upgrade",
            () -> new RecipeSerializer<>() {
                @Override
                public com.mojang.serialization.MapCodec<PipeUpgradeRecipe> codec() {
                    return PipeUpgradeRecipe.CODEC;
                }

                @Override
                public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf,
                    PipeUpgradeRecipe> streamCodec() {
                    return PipeUpgradeRecipe.STREAM_CODEC;
                }
            });

    private BCTransportRecipes() {}

    static void init() {}
}
