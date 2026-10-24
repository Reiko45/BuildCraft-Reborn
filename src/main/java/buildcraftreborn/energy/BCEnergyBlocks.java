/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;

import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

import buildcraftreborn.api.enums.EnumEngineType;
import buildcraftreborn.core.block.BlockSpring;
import buildcraftreborn.energy.block.BlockDynamoMJ;
import buildcraftreborn.energy.block.BlockSpringOil;
import buildcraftreborn.energy.container.ContainerDynamoMJ;
import buildcraftreborn.energy.container.ContainerEngineIron_BC8;
import buildcraftreborn.energy.container.ContainerEngineRF;
import buildcraftreborn.energy.container.ContainerEngineStone_BC8;
import buildcraftreborn.energy.generation.OilWellFeature;
import buildcraftreborn.energy.tile.TileDynamoMJ;
import buildcraftreborn.energy.tile.TileEngineIron_BC8;
import buildcraftreborn.energy.tile.TileEngineRF;
import buildcraftreborn.energy.tile.TileEngineStone_BC8;
import buildcraftreborn.energy.tile.TileSpringOil;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.lib.engine.BlockEngineBase_BC8;

/** Energy blocks: the stirling ({@code engine_stone}), combustion ({@code engine_iron}) and RF ({@code engine_rf})
 * engines, the MJ dynamo, and their menus. The RF engine and MJ dynamo are always registered; their config switches
 * (off by default, as in 1.12) decide whether they can be crafted. */
public final class BCEnergyBlocks {
    public static final DeferredBlock<BlockEngineBase_BC8> ENGINE_STONE = BCRegistries.BLOCKS.register("engine_stone",
        () -> new BlockEngineBase_BC8(BlockBCBase_Neptune.defaultProperties(), EnumEngineType.STONE,
            BCEnergyBlocks.ENGINE_STONE_TILE));
    public static final DeferredBlock<BlockEngineBase_BC8> ENGINE_IRON = BCRegistries.BLOCKS.register("engine_iron",
        () -> new BlockEngineBase_BC8(BlockBCBase_Neptune.defaultProperties(), EnumEngineType.IRON,
            BCEnergyBlocks.ENGINE_IRON_TILE));

    public static final DeferredBlock<BlockEngineBase_BC8> ENGINE_RF = BCRegistries.BLOCKS.register("engine_rf",
        () -> new BlockEngineBase_BC8(BlockBCBase_Neptune.defaultProperties(), EnumEngineType.RF,
            BCEnergyBlocks.ENGINE_RF_TILE));
    public static final DeferredBlock<BlockDynamoMJ> MJ_DYNAMO = BCRegistries.BLOCKS.register("mj_dynamo",
        () -> new BlockDynamoMJ(BlockBCBase_Neptune.defaultProperties()));

    /** 1.12's {@code buildcraftcore:spring} with {@code type=oil}: sits under the shaft of large oil wells. */
    public static final DeferredBlock<BlockSpringOil> OIL_SPRING = BCRegistries.BLOCKS.register("oil_spring",
        () -> new BlockSpringOil(BlockSpring.springProperties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEngineStone_BC8>> ENGINE_STONE_TILE =
        BCRegistries.registerTile("engine_stone", TileEngineStone_BC8::new, ENGINE_STONE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEngineIron_BC8>> ENGINE_IRON_TILE =
        BCRegistries.registerTile("engine_iron", TileEngineIron_BC8::new, ENGINE_IRON);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEngineRF>> ENGINE_RF_TILE =
        BCRegistries.registerTile("engine_rf", TileEngineRF::new, ENGINE_RF);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileDynamoMJ>> MJ_DYNAMO_TILE =
        BCRegistries.registerTile("mj_dynamo", TileDynamoMJ::new, MJ_DYNAMO);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileSpringOil>> OIL_SPRING_TILE =
        BCRegistries.BLOCK_ENTITY_TYPES.register("oil_spring",
            () -> BlockEntityType.Builder.of(TileSpringOil::new, OIL_SPRING.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerEngineStone_BC8>> ENGINE_STONE_MENU =
        BCRegistries.MENU_TYPES.register("engine_stone",
            () -> IMenuTypeExtension.create(ContainerEngineStone_BC8::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerEngineIron_BC8>> ENGINE_IRON_MENU =
        BCRegistries.MENU_TYPES.register("engine_iron",
            () -> IMenuTypeExtension.create(ContainerEngineIron_BC8::fromNetwork));

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerEngineRF>> ENGINE_RF_MENU =
        BCRegistries.MENU_TYPES.register("engine_rf", () -> IMenuTypeExtension.create(ContainerEngineRF::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerDynamoMJ>> MJ_DYNAMO_MENU =
        BCRegistries.MENU_TYPES.register("mj_dynamo", () -> IMenuTypeExtension.create(ContainerDynamoMJ::fromNetwork));

    public static final DeferredHolder<Feature<?>, OilWellFeature> OIL_WELLS =
        BCRegistries.FEATURES.register("oil_wells", OilWellFeature::new);

    static {
        BCRegistries.registerBlockItem(ENGINE_STONE);
        BCRegistries.registerBlockItem(ENGINE_IRON);
        BCRegistries.registerBlockItem(ENGINE_RF);
        BCRegistries.registerBlockItem(MJ_DYNAMO);
        BCRegistries.registerBlockItem(OIL_SPRING);
    }

    private BCEnergyBlocks() {}

    static void init() {}
}
