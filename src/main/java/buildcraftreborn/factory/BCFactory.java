/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory;

import net.neoforged.neoforge.common.NeoForge;

import buildcraftreborn.api.recipes.BuildcraftRecipeRegistry;
import buildcraftreborn.factory.tile.TileMiningWell;
import buildcraftreborn.lib.recipe.RefineryRecipeRegistry;

/** Factory module (1.12's {@code buildcraftfactory} mod): tanks, pumps, flood gates, mining wells, chutes, the auto
 * workbench, the distiller and the heat exchanger. Initialised from {@code BuildCraftReborn}'s constructor. */
public final class BCFactory {
    private BCFactory() {}

    public static void init() {
        BuildcraftRecipeRegistry.refineryRecipes = RefineryRecipeRegistry.INSTANCE;
        BCFactoryBlocks.init();
        BCFactoryItems.init();
        NeoForge.EVENT_BUS.addListener(TileMiningWell::onNeighbourNotify);
    }
}
