/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.viewer;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** A programming or integration table recipe: what goes in (each slot can show several items), what comes out, and
 * the power it takes. */
public record LaserTableDisplay(ResourceLocation id, List<List<ItemStack>> inputs, ItemStack output,
    long microJoules) {}
