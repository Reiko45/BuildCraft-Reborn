/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory;

import net.minecraft.world.item.Item;

import net.neoforged.neoforge.registries.DeferredItem;

import buildcraftreborn.factory.item.ItemWaterGel;
import buildcraftreborn.lib.BCRegistries;

/** Factory items. 1.12's plastic sheet was only registered in development builds, so it isn't ported. */
public final class BCFactoryItems {
    public static final DeferredItem<ItemWaterGel> WATER_GEL_SPAWN =
        BCRegistries.ITEMS.registerItem("water_gel_spawn", ItemWaterGel::new);
    /** Gelled water: what a set water gel block drops. Crafts back into a water bucket. */
    public static final DeferredItem<Item> GEL = BCRegistries.ITEMS.registerSimpleItem("gel");

    private BCFactoryItems() {}

    static void init() {}
}
