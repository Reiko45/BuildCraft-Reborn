/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.tile.craft;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.tile.item.ItemHandlerSimple;

/** Used by compat to provide information aboue the current recipe that is being made in an auto-crafter. */
public interface IAutoCraft {
    ItemStack getCurrentRecipeOutput();

    ItemHandlerSimple getInvBlueprint();
}
