/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.emi;

import java.util.List;

import net.minecraft.world.inventory.Slot;

import buildcraftreborn.silicon.container.ContainerAssemblyTable;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;

/** Moves an assembly recipe's ingredients from the player's inventory (slots 0-35) into the assembly table's 12 input
 * slots (36-47), as JEI's transfer does. */
public class AssemblyRecipeHandler implements StandardRecipeHandler<ContainerAssemblyTable> {
    private static final int PLAYER_SLOTS = 36, INPUT_SLOTS = EmiAssemblyRecipe.MAX_INPUTS;

    @Override
    public List<Slot> getInputSources(ContainerAssemblyTable container) {
        return container.slots.subList(0, PLAYER_SLOTS + INPUT_SLOTS);
    }

    @Override
    public List<Slot> getCraftingSlots(ContainerAssemblyTable container) {
        return container.slots.subList(PLAYER_SLOTS, PLAYER_SLOTS + INPUT_SLOTS);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == BCPluginEMI.ASSEMBLY && recipe.getInputs().size() <= INPUT_SLOTS;
    }
}
