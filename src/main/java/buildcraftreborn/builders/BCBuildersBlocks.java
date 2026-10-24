/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

import buildcraftreborn.builders.block.BlockArchitectTable;
import buildcraftreborn.builders.block.BlockBuilder;
import buildcraftreborn.builders.block.BlockConstructionMarker;
import buildcraftreborn.builders.block.BlockElectronicLibrary;
import buildcraftreborn.builders.block.BlockFiller;
import buildcraftreborn.builders.block.BlockFrame;
import buildcraftreborn.builders.block.BlockQuarry;
import buildcraftreborn.builders.block.BlockReplacer;
import buildcraftreborn.builders.container.ContainerArchitectTable;
import buildcraftreborn.builders.container.ContainerBuilder;
import buildcraftreborn.builders.container.ContainerElectronicLibrary;
import buildcraftreborn.builders.container.ContainerFiller;
import buildcraftreborn.builders.container.ContainerFillerPlanner;
import buildcraftreborn.builders.container.ContainerReplacer;
import buildcraftreborn.builders.tile.TileArchitectTable;
import buildcraftreborn.builders.tile.TileBuilder;
import buildcraftreborn.builders.tile.TileConstructionMarker;
import buildcraftreborn.builders.tile.TileElectronicLibrary;
import buildcraftreborn.builders.tile.TileFiller;
import buildcraftreborn.builders.tile.TileQuarry;
import buildcraftreborn.builders.tile.TileReplacer;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.lib.block.BlockMarkerBase;

/** Builders blocks (1.12's {@code buildcraftbuilders} mod), with their tile types and menus. Registry paths are
 * 1.12's, in the {@code buildcraftreborn} namespace. */
public final class BCBuildersBlocks {
    public static final DeferredBlock<BlockFiller> FILLER = BCRegistries.BLOCKS.register("filler",
        () -> new BlockFiller(BlockBCBase_Neptune.defaultProperties()));
    public static final DeferredBlock<BlockBuilder> BUILDER = BCRegistries.BLOCKS.register("builder",
        () -> new BlockBuilder(BlockBCBase_Neptune.defaultProperties()));
    public static final DeferredBlock<BlockArchitectTable> ARCHITECT = BCRegistries.BLOCKS.register("architect",
        () -> new BlockArchitectTable(BlockBCBase_Neptune.defaultProperties()));
    public static final DeferredBlock<BlockElectronicLibrary> LIBRARY = BCRegistries.BLOCKS.register("library",
        () -> new BlockElectronicLibrary(BlockBCBase_Neptune.defaultProperties()));
    public static final DeferredBlock<BlockReplacer> REPLACER = BCRegistries.BLOCKS.register("replacer",
        () -> new BlockReplacer(BlockBCBase_Neptune.defaultProperties()));

    /** The quarry's frame. It drops nothing (the quarry builds it, and takes it down when broken). */
    public static final DeferredBlock<BlockFrame> FRAME = BCRegistries.BLOCKS.register("frame",
        () -> new BlockFrame(BlockBCBase_Neptune.defaultProperties().noOcclusion().noLootTable()));
    public static final DeferredBlock<BlockQuarry> QUARRY = BCRegistries.BLOCKS.register("quarry",
        () -> new BlockQuarry(BlockBCBase_Neptune.defaultProperties().sound(SoundType.ANVIL)));
    /** 7.1's construction marker, for builder robots. */
    public static final DeferredBlock<BlockConstructionMarker> CONSTRUCTION_MARKER = BCRegistries.BLOCKS.register(
        "construction_marker", () -> new BlockConstructionMarker(BlockMarkerBase.markerProperties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileFiller>> FILLER_TILE =
        BCRegistries.registerTile("filler", TileFiller::new, FILLER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileBuilder>> BUILDER_TILE =
        BCRegistries.registerTile("builder", TileBuilder::new, BUILDER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileArchitectTable>> ARCHITECT_TILE =
        BCRegistries.registerTile("architect", TileArchitectTable::new, ARCHITECT);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileElectronicLibrary>> LIBRARY_TILE =
        BCRegistries.registerTile("library", TileElectronicLibrary::new, LIBRARY);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileReplacer>> REPLACER_TILE =
        BCRegistries.registerTile("replacer", TileReplacer::new, REPLACER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileQuarry>> QUARRY_TILE =
        BCRegistries.registerTile("quarry", TileQuarry::new, QUARRY);
    public static final DeferredHolder<BlockEntityType<?>,
        BlockEntityType<TileConstructionMarker>> CONSTRUCTION_MARKER_TILE = BCRegistries.registerTile(
            "construction_marker", TileConstructionMarker::new, CONSTRUCTION_MARKER);

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerFiller>> FILLER_MENU =
        BCRegistries.MENU_TYPES.register("filler", () -> IMenuTypeExtension.create(ContainerFiller::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerBuilder>> BUILDER_MENU =
        BCRegistries.MENU_TYPES.register("builder", () -> IMenuTypeExtension.create(ContainerBuilder::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerArchitectTable>> ARCHITECT_MENU =
        BCRegistries.MENU_TYPES.register("architect",
            () -> IMenuTypeExtension.create(ContainerArchitectTable::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerElectronicLibrary>> LIBRARY_MENU =
        BCRegistries.MENU_TYPES.register("library",
            () -> IMenuTypeExtension.create(ContainerElectronicLibrary::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerReplacer>> REPLACER_MENU =
        BCRegistries.MENU_TYPES.register("replacer", () -> IMenuTypeExtension.create(ContainerReplacer::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerFillerPlanner>> FILLER_PLANNER_MENU =
        BCRegistries.MENU_TYPES.register("filler_planner",
            () -> IMenuTypeExtension.create(ContainerFillerPlanner::fromNetwork));

    static {
        BCRegistries.registerBlockItem(FILLER);
        BCRegistries.registerBlockItem(BUILDER);
        BCRegistries.registerBlockItem(ARCHITECT);
        BCRegistries.registerBlockItem(LIBRARY);
        BCRegistries.registerBlockItem(REPLACER);
        BCRegistries.registerBlockItem(FRAME);
        BCRegistries.registerBlockItem(QUARRY);
        BCRegistries.registerBlockItem(CONSTRUCTION_MARKER);
    }

    private BCBuildersBlocks() {}

    static void init() {}
}
