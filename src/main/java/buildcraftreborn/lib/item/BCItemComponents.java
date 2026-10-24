/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.lib.item;

import java.util.function.Supplier;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import net.neoforged.neoforge.fluids.SimpleFluidContent;

import buildcraftreborn.core.item.MapLocationData;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.list.ListHandler;

/** Item data components shared by several modules (1.12 kept these in item NBT). */
public final class BCItemComponents {
    /** The fluid an item holds (1.12: the item's {@code fluid} NBT tag). */
    public static final Supplier<DataComponentType<SimpleFluidContent>> FLUID = BCRegistries.DATA_COMPONENTS
        .registerComponentType("fluid", b -> b.persistent(SimpleFluidContent.CODEC)
            .networkSynchronized(SimpleFluidContent.STREAM_CODEC));

    /** How many times a paintbrush has painted since it was dipped (1.12: its {@code damage} NBT byte). */
    public static final Supplier<DataComponentType<Integer>> PAINT_USED = BCRegistries.DATA_COMPONENTS
        .registerComponentType("paint_used", b -> b.persistent(com.mojang.serialization.Codec.intRange(0, 64))
            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT));
    /** The dye colour of a coloured item, such as a painted pipe (1.12: the item's metadata). */
    public static final Supplier<DataComponentType<DyeColor>> COLOUR = BCRegistries.DATA_COMPONENTS
        .registerComponentType("colour", b -> b.persistent(DyeColor.CODEC).networkSynchronized(DyeColor.STREAM_CODEC));

    /** A list item's label and lines (1.12: its {@code label}, {@code written} and {@code lines} NBT). */
    public static final Supplier<DataComponentType<ListHandler.Data>> LIST = BCRegistries.DATA_COMPONENTS
        .registerComponentType("list", b -> b.persistent(ListHandler.Data.CODEC)
            .networkSynchronized(ListHandler.Data.STREAM_CODEC).cacheEncoding());

    /** What a map location remembers (1.12: its damage value and NBT). */
    public static final Supplier<DataComponentType<MapLocationData>> MAP_LOCATION = BCRegistries.DATA_COMPONENTS
        .registerComponentType("map_location", b -> b.persistent(MapLocationData.CODEC)
            .networkSynchronized(MapLocationData.STREAM_CODEC).cacheEncoding());

    /** Which book a guide book is (1.12: its {@code BookName} NBT). Without it, it's the main book. */
    public static final Supplier<DataComponentType<ResourceLocation>> GUIDE_BOOK = BCRegistries.DATA_COMPONENTS
        .registerComponentType("guide_book", b -> b.persistent(ResourceLocation.CODEC)
            .networkSynchronized(ResourceLocation.STREAM_CODEC));

    private BCItemComponents() {}

    public static void init() {}
}
