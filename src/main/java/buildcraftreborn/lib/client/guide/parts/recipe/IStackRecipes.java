/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.client.guide.parts.GuidePartFactory;

/** Defines a stack recipe lookup - implementations should register with {@link RecipeLookupHelper} to be used by the
 * guide for usages and recipes. */
public interface IStackRecipes {
    @Nullable
    List<GuidePartFactory> getUsages(ItemStack stack);

    @Nullable
    List<GuidePartFactory> getRecipes(ItemStack stack);
}
