/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.jei;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.recipes.AssemblyRecipe;
import buildcraftreborn.api.recipes.IngredientStack;
import buildcraftreborn.silicon.plug.FacadeBlockStateInfo;
import buildcraftreborn.silicon.plug.FacadeStateManager;
import buildcraftreborn.silicon.recipe.FacadeAssemblyRecipe;

/** One output of an assembly recipe, as JEI shows it: what goes in, what comes out and the power it takes. An
 * assembly recipe can have several outputs (each with its own inputs), so it can make several of these. */
public record AssemblyDisplay(ResourceLocation id, List<IngredientStack> inputs, ItemStack output, long microJoules) {
    /** Adds a display for each output {@link AssemblyRecipe#getOutputPreviews()} lists. */
    static void addAll(ResourceLocation id, AssemblyRecipe recipe, List<AssemblyDisplay> to) {
        int i = 0;
        for (ItemStack output : recipe.getOutputPreviews()) {
            ResourceLocation displayId = i == 0 ? id : id.withSuffix("/" + i);
            to.add(new AssemblyDisplay(displayId, List.copyOf(recipe.getInputsFor(output)), output,
                recipe.getRequiredMicroJoulesFor(output)));
            i++;
        }
    }

    /** The facade recipe has no previews (it makes a facade of whatever block is put in), so this adds one display
     * for each facade, normal and hollow. */
    static void addFacades(List<AssemblyDisplay> to) {
        for (FacadeBlockStateInfo info : FacadeStateManager.validFacadeStates.values()) {
            if (!info.isVisible || info.requiredStack.isEmpty()) {
                continue;
            }
            ResourceLocation block = BuiltInRegistries.BLOCK.getKey(info.state.getBlock());
            String path = "facade/" + block.getNamespace() + "/" + block.getPath() + "/" + Block.getId(info.state);
            for (boolean hollow : new boolean[] { false, true }) {
                ItemStack output = FacadeAssemblyRecipe.createFacadeStack(info, hollow);
                to.add(new AssemblyDisplay(BuildCraftReborn.id(path + (hollow ? "_hollow" : "")),
                    List.copyOf(FacadeAssemblyRecipe.INSTANCE.getInputsFor(output)), output,
                    FacadeAssemblyRecipe.INSTANCE.getRequiredMicroJoulesFor(output)));
            }
        }
    }
}
