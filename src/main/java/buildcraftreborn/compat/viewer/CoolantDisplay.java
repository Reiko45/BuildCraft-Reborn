/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.viewer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.fluids.FluidStack;

/** A combustion engine coolant: a fluid, or an item that melts into one.
 *
 * @param solid The item that melts into {@code fluid}, or null for a fluid coolant.
 * @param degreesPerMb How much each mB of the fluid cools the engine. */
public record CoolantDisplay(@Nullable ItemStack solid, FluidStack fluid, float degreesPerMb) {
    public ResourceLocation id() {
        if (solid != null) {
            ResourceLocation item = BuiltInRegistries.ITEM.getKey(solid.getItem());
            return ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "solid_coolant/" + item.getPath());
        }
        return RecipeDisplays.fluidRecipeId("combustion_coolant", fluid);
    }
}
