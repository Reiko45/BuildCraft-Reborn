/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.recipes.AssemblyRecipe;
import buildcraftreborn.api.recipes.AssemblyRecipeBasic;
import buildcraftreborn.api.recipes.IntegrationRecipe;
import buildcraftreborn.api.facades.FacadeAPI;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.item.IItemSubtypes;
import buildcraftreborn.silicon.plug.FacadeInstance;
import buildcraftreborn.silicon.plug.FacadeStateManager;
import buildcraftreborn.silicon.recipe.FacadeAssemblyRecipe;
import buildcraftreborn.silicon.recipe.FacadeSwapRecipe;
import buildcraftreborn.transport.BCTransportItems;

/** BuildCraft Silicon: lasers, laser tables, gates and their statements, lenses, pulsars, timers, light sensors and
 * facades. 1.12 was a separate mod ({@code buildcraftsilicon}); here it's part of the one mod and needs transport
 * (its pluggables go on pipes), so {@link #init} runs after transport's. */
public final class BCSilicon {
    public static final DeferredHolder<RecipeType<?>, RecipeType<AssemblyRecipe>> ASSEMBLY_RECIPE_TYPE =
        BCRegistries.RECIPE_TYPES.register("assembly", () -> RecipeType.simple(BuildCraftReborn.id("assembly")));
    public static final DeferredHolder<RecipeType<?>, RecipeType<IntegrationRecipe>> INTEGRATION_RECIPE_TYPE =
        BCRegistries.RECIPE_TYPES.register("integration", () -> RecipeType.simple(BuildCraftReborn.id("integration")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AssemblyRecipeBasic>> ASSEMBLY_SERIALIZER =
        BCRegistries.RECIPE_SERIALIZERS.register("assembly", () -> new RecipeSerializer<>() {
            @Override
            public MapCodec<AssemblyRecipeBasic> codec() {
                return AssemblyRecipeBasic.CODEC;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, AssemblyRecipeBasic> streamCodec() {
                return AssemblyRecipeBasic.STREAM_CODEC;
            }
        });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FacadeAssemblyRecipe>>
        FACADE_ASSEMBLY_SERIALIZER = BCRegistries.RECIPE_SERIALIZERS.register("assembly_facade",
            () -> new RecipeSerializer<>() {
                @Override
                public MapCodec<FacadeAssemblyRecipe> codec() {
                    return MapCodec.unit(FacadeAssemblyRecipe.INSTANCE);
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, FacadeAssemblyRecipe> streamCodec() {
                    return StreamCodec.unit(FacadeAssemblyRecipe.INSTANCE);
                }
            });
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FacadeSwapRecipe>> FACADE_SWAP_SERIALIZER =
        BCRegistries.RECIPE_SERIALIZERS.register("facade_swap",
            () -> new SimpleCraftingRecipeSerializer<>(FacadeSwapRecipe::new));

    public static final String TAB_FACADES_KEY = "itemGroup.buildcraftreborn.facades";

    /** 1.12's "buildcraft.facades" tab: every facade, shown by a brick facade. */
    public static final Supplier<CreativeModeTab> TAB_FACADES = BCRegistries.CREATIVE_TABS.register("facades",
        () -> CreativeModeTab.builder()
            .title(Component.translatable(TAB_FACADES_KEY))
            .icon(() -> BCSiliconItems.PLUG_FACADE.get().createItemStack(
                FacadeInstance.createSingle(FacadeStateManager.previewState(), false)))
            .displayItems((params, output) -> IItemSubtypes.accept(output, BCSiliconItems.PLUG_FACADE.get()))
            .build());

    private BCSilicon() {}

    public static void init(IEventBus modBus) {
        AssemblyRecipe.type = ASSEMBLY_RECIPE_TYPE;
        AssemblyRecipeBasic.serializer = ASSEMBLY_SERIALIZER;
        IntegrationRecipe.type = INTEGRATION_RECIPE_TYPE;

        BCSiliconBlocks.init();
        BCSiliconPlugs.init();
        BCSiliconItems.init();
        BCSiliconStatements.init();

        // 1.12's "buildcraft.plugs" tab: transport's plugs first, then silicon's
        BCTransportItems.addToPlugTab(BCSiliconItems.PLUG_GATE);
        BCTransportItems.addToPlugTab(BCSiliconItems.PLUG_LENS);
        BCTransportItems.addToPlugTab(BCSiliconItems.PLUG_PULSAR);
        BCTransportItems.addToPlugTab(BCSiliconItems.PLUG_LIGHT_SENSOR);
        BCTransportItems.addToPlugTab(BCSiliconItems.PLUG_TIMER);
        BCRegistries.NOT_IN_MAIN_TAB.add(BCSiliconItems.PLUG_FACADE.getId());

        FacadeAPI.registry = FacadeStateManager.INSTANCE;
        modBus.addListener(BCSilicon::onCommonSetup);
        // Facades are worked out once every mod has sent its facade messages
        modBus.addListener(BCSilicon::onInterModProcess);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        FacadeAPI.facadeItem = BCSiliconItems.PLUG_FACADE.get();
    }

    private static void onInterModProcess(InterModProcessEvent event) {
        event.enqueueWork(() -> {
            FacadeStateManager.receiveInterModComms();
            FacadeStateManager.init();
        });
    }
}
