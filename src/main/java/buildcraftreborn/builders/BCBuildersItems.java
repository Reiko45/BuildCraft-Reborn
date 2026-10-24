/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders;

import java.util.function.Supplier;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;

import net.neoforged.neoforge.registries.DeferredItem;

import buildcraftreborn.api.enums.EnumSnapshotType;
import buildcraftreborn.builders.item.ItemFillerPlanner;
import buildcraftreborn.builders.item.ItemSchematicSingle;
import buildcraftreborn.builders.item.ItemSnapshot;
import buildcraftreborn.lib.BCRegistries;

/** Builders items. 1.21: 1.12's {@code snapshot} item (four metadata values) is split into {@code blueprint} and
 * {@code template}, each blank or used (with a {@code snapshot_header} component). */
public final class BCBuildersItems {
    /** The header of the scan a blueprint or template holds (1.12: its {@code header} NBT tag). */
    public static final Supplier<DataComponentType<CompoundTag>> SNAPSHOT_HEADER = BCRegistries.DATA_COMPONENTS
        .registerComponentType("snapshot_header", b -> b.persistent(CompoundTag.CODEC)
            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG));
    /** The block a single schematic holds (1.12: its {@code schematic} NBT tag). */
    public static final Supplier<DataComponentType<CompoundTag>> SCHEMATIC = BCRegistries.DATA_COMPONENTS
        .registerComponentType("schematic", b -> b.persistent(CompoundTag.CODEC)
            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

    public static final DeferredItem<ItemSnapshot> BLUEPRINT = BCRegistries.ITEMS.registerItem("blueprint",
        p -> new ItemSnapshot(EnumSnapshotType.BLUEPRINT, p));
    public static final DeferredItem<ItemSnapshot> TEMPLATE = BCRegistries.ITEMS.registerItem("template",
        p -> new ItemSnapshot(EnumSnapshotType.TEMPLATE, p));
    public static final DeferredItem<ItemSchematicSingle> SCHEMATIC_SINGLE =
        BCRegistries.ITEMS.registerItem("schematic_single", ItemSchematicSingle::new);
    public static final DeferredItem<ItemFillerPlanner> FILLER_PLANNER =
        BCRegistries.ITEMS.registerItem("filler_planner", ItemFillerPlanner::new);

    private BCBuildersItems() {}

    static void init() {}
}
