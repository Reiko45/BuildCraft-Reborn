/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.robotics;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

import buildcraftreborn.api.enums.EnumLaserTableType;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.robotics.block.BlockRequester;
import buildcraftreborn.robotics.block.BlockZonePlanner;
import buildcraftreborn.robotics.container.ContainerChargingTable;
import buildcraftreborn.robotics.container.ContainerProgrammingTable;
import buildcraftreborn.robotics.container.ContainerRequester;
import buildcraftreborn.robotics.container.ContainerZonePlanner;
import buildcraftreborn.robotics.tile.TileChargingTable;
import buildcraftreborn.robotics.tile.TileProgrammingTable;
import buildcraftreborn.robotics.tile.TileRequester;
import buildcraftreborn.robotics.tile.TileZonePlanner;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.block.BlockLaserTable;

/** Robotics blocks (1.12's {@code buildcraftrobotics} mod): 8.0's zone planner, and from 7.1 the requester and the
 * charging and programming tables (laser tables, like silicon's). */
public final class BCRoboticsBlocks {
    public static final DeferredBlock<BlockZonePlanner> ZONE_PLANNER = BCRegistries.BLOCKS.register("zone_planner",
        () -> new BlockZonePlanner(BlockBCBase_Neptune.defaultProperties()));
    public static final DeferredBlock<BlockRequester> REQUESTER = BCRegistries.BLOCKS.register("requester",
        () -> new BlockRequester(BlockBCBase_Neptune.defaultProperties()));
    public static final DeferredBlock<BlockLaserTable> CHARGING_TABLE = BCRegistries.BLOCKS.register("charging_table",
        () -> new BlockLaserTable(EnumLaserTableType.CHARGING_TABLE, BCSiliconBlocks.properties(),
            TileChargingTable::new));
    public static final DeferredBlock<BlockLaserTable> PROGRAMMING_TABLE = BCRegistries.BLOCKS.register(
        "programming_table", () -> new BlockLaserTable(EnumLaserTableType.PROGRAMMING_TABLE,
            BCSiliconBlocks.properties(), TileProgrammingTable::new));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileZonePlanner>> ZONE_PLANNER_TILE =
        BCRegistries.registerTile("zone_planner", TileZonePlanner::new, ZONE_PLANNER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileRequester>> REQUESTER_TILE =
        BCRegistries.registerTile("requester", TileRequester::new, REQUESTER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileChargingTable>> CHARGING_TABLE_TILE =
        BCRegistries.registerTile("charging_table", TileChargingTable::new, CHARGING_TABLE);
    public static final DeferredHolder<BlockEntityType<?>,
        BlockEntityType<TileProgrammingTable>> PROGRAMMING_TABLE_TILE = BCRegistries.registerTile(
            "programming_table", TileProgrammingTable::new, PROGRAMMING_TABLE);

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerZonePlanner>> ZONE_PLANNER_MENU =
        BCRegistries.MENU_TYPES.register("zone_planner",
            () -> IMenuTypeExtension.create(ContainerZonePlanner::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerRequester>> REQUESTER_MENU =
        BCRegistries.MENU_TYPES.register("requester", () -> IMenuTypeExtension.create(ContainerRequester::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerChargingTable>> CHARGING_TABLE_MENU =
        BCRegistries.MENU_TYPES.register("charging_table",
            () -> IMenuTypeExtension.create(ContainerChargingTable::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerProgrammingTable>> PROGRAMMING_TABLE_MENU =
        BCRegistries.MENU_TYPES.register("programming_table",
            () -> IMenuTypeExtension.create(ContainerProgrammingTable::fromNetwork));

    static {
        BCRegistries.registerBlockItem(ZONE_PLANNER);
        BCRegistries.registerBlockItem(REQUESTER);
        BCRegistries.registerBlockItem(CHARGING_TABLE);
        BCRegistries.registerBlockItem(PROGRAMMING_TABLE);
    }

    private BCRoboticsBlocks() {}

    static void init() {}
}
