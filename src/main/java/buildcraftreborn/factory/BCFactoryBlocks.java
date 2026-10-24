/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

import buildcraftreborn.factory.block.BlockAutoWorkbenchItems;
import buildcraftreborn.factory.block.BlockChute;
import buildcraftreborn.factory.block.BlockDistiller;
import buildcraftreborn.factory.block.BlockFloodGate;
import buildcraftreborn.factory.block.BlockHeatExchange;
import buildcraftreborn.factory.block.BlockMiningWell;
import buildcraftreborn.factory.block.BlockPump;
import buildcraftreborn.factory.block.BlockTank;
import buildcraftreborn.factory.block.BlockTube;
import buildcraftreborn.factory.block.BlockWaterGel;
import buildcraftreborn.factory.container.ContainerAutoCraftItems;
import buildcraftreborn.factory.container.ContainerChute;
import buildcraftreborn.factory.container.ContainerDistiller;
import buildcraftreborn.factory.container.ContainerTank;
import buildcraftreborn.factory.tile.TileAutoWorkbenchItems;
import buildcraftreborn.factory.tile.TileChute;
import buildcraftreborn.factory.tile.TileDistiller_BC8;
import buildcraftreborn.factory.tile.TileFloodGate;
import buildcraftreborn.factory.tile.TileHeatExchange;
import buildcraftreborn.factory.tile.TileMiningWell;
import buildcraftreborn.factory.tile.TilePump;
import buildcraftreborn.factory.tile.TileTank;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;

/** Factory blocks (1.12's {@code buildcraftfactory} mod), with their tile types and menus. Registry paths are 1.12's,
 * in the {@code buildcraftreborn} namespace. */
public final class BCFactoryBlocks {
    public static final DeferredBlock<BlockChute> CHUTE = BCRegistries.BLOCKS.register("chute",
        () -> new BlockChute(BlockBCBase_Neptune.defaultProperties().noOcclusion()));
    public static final DeferredBlock<BlockTank> TANK = BCRegistries.BLOCKS.register("tank",
        () -> new BlockTank(BlockBCBase_Neptune.defaultProperties().noOcclusion()));

    public static final DeferredBlock<BlockAutoWorkbenchItems> AUTO_WORKBENCH_ITEMS = BCRegistries.BLOCKS.register(
        "autoworkbench_item", () -> new BlockAutoWorkbenchItems(BlockBCBase_Neptune.defaultProperties()
            .sound(SoundType.STONE)));
    public static final DeferredBlock<BlockMiningWell> MINING_WELL = BCRegistries.BLOCKS.register("mining_well",
        () -> new BlockMiningWell(BlockBCBase_Neptune.defaultProperties()));
    public static final DeferredBlock<BlockPump> PUMP = BCRegistries.BLOCKS.register("pump",
        () -> new BlockPump(BlockBCBase_Neptune.defaultProperties()));
    /** The column below a mining well or pump. Unbreakable, and has no item. */
    public static final DeferredBlock<BlockTube> TUBE = BCRegistries.BLOCKS.register("tube",
        () -> new BlockTube(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(-1.0F, 3600000.0F)
            .sound(SoundType.METAL).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK)));
    public static final DeferredBlock<BlockFloodGate> FLOOD_GATE = BCRegistries.BLOCKS.register("flood_gate",
        () -> new BlockFloodGate(BlockBCBase_Neptune.defaultProperties()));

    public static final DeferredBlock<BlockDistiller> DISTILLER = BCRegistries.BLOCKS.register("distiller",
        () -> new BlockDistiller(BlockBCBase_Neptune.defaultProperties().noOcclusion()));
    public static final DeferredBlock<BlockHeatExchange> HEAT_EXCHANGE = BCRegistries.BLOCKS.register(
        "heat_exchange", () -> new BlockHeatExchange(BlockBCBase_Neptune.defaultProperties().noOcclusion()));

    /** Has no item: it only appears when water gel is thrown into water. */
    public static final DeferredBlock<BlockWaterGel> WATER_GEL = BCRegistries.BLOCKS.register("water_gel",
        () -> new BlockWaterGel(BlockBehaviour.Properties.of().mapColor(MapColor.CLAY).strength(0.6f)
            .sound(SoundType.SLIME_BLOCK).pushReaction(PushReaction.NORMAL)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileTank>> TANK_TILE =
        BCRegistries.registerTile("tank", TileTank::new, TANK);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileAutoWorkbenchItems>>
        AUTO_WORKBENCH_ITEMS_TILE = BCRegistries.registerTile("autoworkbench_item", TileAutoWorkbenchItems::new,
            AUTO_WORKBENCH_ITEMS);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileChute>> CHUTE_TILE =
        BCRegistries.registerTile("chute", TileChute::new, CHUTE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileMiningWell>> MINING_WELL_TILE =
        BCRegistries.registerTile("mining_well", TileMiningWell::new, MINING_WELL);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TilePump>> PUMP_TILE =
        BCRegistries.registerTile("pump", TilePump::new, PUMP);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileFloodGate>> FLOOD_GATE_TILE =
        BCRegistries.registerTile("flood_gate", TileFloodGate::new, FLOOD_GATE);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileDistiller_BC8>> DISTILLER_TILE =
        BCRegistries.registerTile("distiller", TileDistiller_BC8::new, DISTILLER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileHeatExchange>> HEAT_EXCHANGE_TILE =
        BCRegistries.registerTile("heat_exchange", TileHeatExchange::new, HEAT_EXCHANGE);

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerTank>> TANK_MENU =
        BCRegistries.MENU_TYPES.register("tank", () -> IMenuTypeExtension.create(ContainerTank::fromNetwork));

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerChute>> CHUTE_MENU =
        BCRegistries.MENU_TYPES.register("chute", () -> IMenuTypeExtension.create(ContainerChute::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerAutoCraftItems>> AUTO_WORKBENCH_ITEMS_MENU =
        BCRegistries.MENU_TYPES.register("autoworkbench_item",
            () -> IMenuTypeExtension.create(ContainerAutoCraftItems::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerDistiller>> DISTILLER_MENU =
        BCRegistries.MENU_TYPES.register("distiller", () -> IMenuTypeExtension.create(ContainerDistiller::fromNetwork));

    static {
        BCRegistries.registerBlockItem(AUTO_WORKBENCH_ITEMS);
        BCRegistries.registerBlockItem(MINING_WELL);
        BCRegistries.registerBlockItem(PUMP);
        BCRegistries.registerBlockItem(FLOOD_GATE);
        BCRegistries.registerBlockItem(TANK);
        BCRegistries.registerBlockItem(CHUTE);
        BCRegistries.registerBlockItem(DISTILLER);
        BCRegistries.registerBlockItem(HEAT_EXCHANGE);
    }

    private BCFactoryBlocks() {}

    static void init() {}
}
