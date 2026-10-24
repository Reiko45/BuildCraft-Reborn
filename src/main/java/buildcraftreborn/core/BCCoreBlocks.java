/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

import buildcraftreborn.api.enums.EnumDecoratedBlock;
import buildcraftreborn.api.enums.EnumEngineType;
import buildcraftreborn.api.enums.EnumSpring;
import buildcraftreborn.core.block.BlockDecoration;
import buildcraftreborn.core.block.BlockMarkerPath;
import buildcraftreborn.core.block.BlockMarkerVolume;
import buildcraftreborn.core.block.BlockSpring;
import buildcraftreborn.core.tile.TileEngineCreative;
import buildcraftreborn.core.tile.TileEngineRedstone_BC8;
import buildcraftreborn.core.tile.TileMarkerPath;
import buildcraftreborn.core.tile.TileMarkerVolume;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.lib.block.BlockMarkerBase;
import buildcraftreborn.lib.engine.BlockEngineBase_BC8;

/** Core blocks. 1.12's single {@code buildcraftcore:engine} block (with the type as metadata) is split into one block
 * per engine: {@code engine_wood} (redstone engine) and {@code engine_creative} here, {@code engine_stone} and
 * {@code engine_iron} in the energy module. */
public final class BCCoreBlocks {
    public static final DeferredBlock<BlockEngineBase_BC8> ENGINE_WOOD = BCRegistries.BLOCKS.register("engine_wood",
        () -> new BlockEngineBase_BC8(BlockBCBase_Neptune.defaultProperties(), EnumEngineType.WOOD,
            BCCoreBlocks.ENGINE_WOOD_TILE));
    public static final DeferredBlock<BlockEngineBase_BC8> ENGINE_CREATIVE = BCRegistries.BLOCKS.register(
        "engine_creative", () -> new BlockEngineBase_BC8(BlockBCBase_Neptune.defaultProperties(),
            EnumEngineType.CREATIVE, BCCoreBlocks.ENGINE_CREATIVE_TILE));

    /** 1.12's {@code buildcraftcore:spring} with {@code type=water}. Water springs never generate in 8.0 (its
     * populator was not registered), so this is creative-only, as it was. */
    public static final DeferredBlock<BlockSpring> WATER_SPRING = BCRegistries.BLOCKS.register("water_spring",
        () -> new BlockSpring(EnumSpring.WATER, BlockSpring.springProperties()));

    /** The volume marker ("Land Mark", 1.12's {@code block.marker.volume}). */
    public static final DeferredBlock<BlockMarkerVolume> MARKER_VOLUME = BCRegistries.BLOCKS.register("marker_volume",
        () -> new BlockMarkerVolume(BlockMarkerBase.markerProperties()));
    /** The path marker ("Path Mark"). */
    public static final DeferredBlock<BlockMarkerPath> MARKER_PATH = BCRegistries.BLOCKS.register("marker_path",
        () -> new BlockMarkerPath(BlockMarkerBase.markerProperties()));

    /** 1.12's {@code decorated} block, one block per type. */
    public static final Map<EnumDecoratedBlock, DeferredBlock<BlockDecoration>> DECORATED = new EnumMap<>(
        EnumDecoratedBlock.class);

    static {
        for (EnumDecoratedBlock type : EnumDecoratedBlock.VALUES) {
            DECORATED.put(type, BCRegistries.BLOCKS.register("decorated_" + type.getSerializedName(),
                () -> new BlockDecoration(type)));
        }
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEngineRedstone_BC8>> ENGINE_WOOD_TILE =
        BCRegistries.registerTile("engine_wood", TileEngineRedstone_BC8::new, ENGINE_WOOD);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEngineCreative>> ENGINE_CREATIVE_TILE =
        BCRegistries.registerTile("engine_creative", TileEngineCreative::new, ENGINE_CREATIVE);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileMarkerVolume>> MARKER_VOLUME_TILE =
        BCRegistries.registerTile("marker_volume", TileMarkerVolume::new, MARKER_VOLUME);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileMarkerPath>> MARKER_PATH_TILE =
        BCRegistries.registerTile("marker_path", TileMarkerPath::new, MARKER_PATH);

    static {
        BCRegistries.registerBlockItem(ENGINE_WOOD);
        BCRegistries.registerBlockItem(ENGINE_CREATIVE);
        BCRegistries.registerBlockItem(WATER_SPRING);
        BCRegistries.registerBlockItem(MARKER_VOLUME);
        BCRegistries.registerBlockItem(MARKER_PATH);
        DECORATED.values().forEach(BCRegistries::registerBlockItem);
    }

    private BCCoreBlocks() {}

    static void init() {}
}
