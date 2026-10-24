/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.recipe;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.recipes.AssemblyRecipe;
import buildcraftreborn.api.recipes.IngredientStack;
import buildcraftreborn.silicon.BCSilicon;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.item.ItemPluggableFacade;
import buildcraftreborn.silicon.plug.FacadeBlockStateInfo;
import buildcraftreborn.silicon.plug.FacadeInstance;
import buildcraftreborn.silicon.plug.FacadePhasedState;
import buildcraftreborn.silicon.plug.FacadeStateManager;
import buildcraftreborn.transport.BCTransportItems;
import buildcraftreborn.transport.BCTransportPipes;

/** The assembly recipe for facades: 3 structure pipes and a block make 6 facades of that block (solid or hollow), for
 * 64 MJ. Every block works, so the outputs are worked out from what's in the table. Its JSON file
 * ({@code assembly/facades}) only names its type. */
public final class FacadeAssemblyRecipe extends AssemblyRecipe {
    public static final FacadeAssemblyRecipe INSTANCE = new FacadeAssemblyRecipe();

    private static final long MJ_COST = 64 * MjAPI.MJ;

    private FacadeAssemblyRecipe() {}

    public static ItemStack createFacadeStack(FacadeBlockStateInfo info, boolean isHollow) {
        ItemStack stack = BCSiliconItems.PLUG_FACADE.get().createItemStack(FacadeInstance.createSingle(info, isHollow));
        stack.setCount(6);
        return stack;
    }

    private static Item structurePipe() {
        return BCTransportItems.PIPES.get(BCTransportPipes.structure).get();
    }

    @Override
    public Set<ItemStack> getOutputs(NonNullList<ItemStack> inputs) {
        int pipes = 0;
        for (ItemStack stack : inputs) {
            if (stack.is(structurePipe())) {
                pipes += stack.getCount();
            }
        }
        if (pipes < 3) {
            return Collections.emptySet();
        }
        Set<ItemStack> stacks = new LinkedHashSet<>();
        for (ItemStack stack : inputs) {
            if (stack.isEmpty()) {
                continue;
            }
            List<FacadeBlockStateInfo> infos = FacadeStateManager.stackFacades.get(stack.getItem());
            if (infos == null) {
                continue;
            }
            for (FacadeBlockStateInfo info : infos) {
                stacks.add(createFacadeStack(info, false));
                stacks.add(createFacadeStack(info, true));
            }
        }
        return Collections.unmodifiableSet(stacks);
    }

    @Override
    public Set<ItemStack> getOutputPreviews() {
        return Collections.emptySet();
    }

    @Override
    public Set<IngredientStack> getInputsFor(ItemStack output) {
        FacadePhasedState state = ItemPluggableFacade.getStates(output).getCurrentStateForStack();
        ItemStack stateRequirement = state.stateInfo.requiredStack;
        return ImmutableSet.of(new IngredientStack(Ingredient.of(stateRequirement.getItem())),
            new IngredientStack(Ingredient.of(structurePipe()), 3));
    }

    @Override
    public long getRequiredMicroJoulesFor(ItemStack output) {
        return MJ_COST;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BCSilicon.FACADE_ASSEMBLY_SERIALIZER.get();
    }
}
