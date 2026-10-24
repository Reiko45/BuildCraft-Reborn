/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Item tags replacing 1.12's ore dictionary names. {@code gearWood} becomes {@code c:gears/wood}, following the
 * NeoForge and Fabric common-tag convention, so other mods' gears work in BuildCraft recipes and the reverse.
 */
public final class BCCoreTags {
    public static final TagKey<Item> GEARS = common("gears");
    public static final TagKey<Item> GEARS_WOOD = common("gears/wood");
    public static final TagKey<Item> GEARS_STONE = common("gears/stone");
    public static final TagKey<Item> GEARS_IRON = common("gears/iron");
    public static final TagKey<Item> GEARS_GOLD = common("gears/gold");
    public static final TagKey<Item> GEARS_DIAMOND = common("gears/diamond");

    private BCCoreTags() {}

    private static TagKey<Item> common(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
