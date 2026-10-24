/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

import buildcraftreborn.api.enums.EnumLaserTableType;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.silicon.block.BlockLaser;
import buildcraftreborn.silicon.block.BlockLaserTable;
import buildcraftreborn.silicon.container.ContainerAdvancedCraftingTable;
import buildcraftreborn.silicon.container.ContainerAssemblyTable;
import buildcraftreborn.silicon.container.ContainerGate;
import buildcraftreborn.silicon.container.ContainerIntegrationTable;
import buildcraftreborn.silicon.tile.TileAdvancedCraftingTable;
import buildcraftreborn.silicon.tile.TileAssemblyTable;
import buildcraftreborn.silicon.tile.TileIntegrationTable;
import buildcraftreborn.silicon.tile.TileLaser;

/** The laser and the laser tables, their tile types, and the silicon menus. (The charging and programming tables are
 * registered by robotics, in {@code BCRoboticsBlocks}.) */
public final class BCSiliconBlocks {
    public static final DeferredBlock<BlockLaser> LASER = BCRegistries.BLOCKS.register("laser",
        () -> new BlockLaser(properties()));
    public static final DeferredBlock<BlockLaserTable> ASSEMBLY_TABLE = BCRegistries.BLOCKS.register("assembly_table",
        () -> new BlockLaserTable(EnumLaserTableType.ASSEMBLY_TABLE, properties()));
    public static final DeferredBlock<BlockLaserTable> ADVANCED_CRAFTING_TABLE = BCRegistries.BLOCKS.register(
        "advanced_crafting_table",
        () -> new BlockLaserTable(EnumLaserTableType.ADVANCED_CRAFTING_TABLE, properties()));
    public static final DeferredBlock<BlockLaserTable> INTEGRATION_TABLE = BCRegistries.BLOCKS.register(
        "integration_table", () -> new BlockLaserTable(EnumLaserTableType.INTEGRATION_TABLE, properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileLaser>> LASER_TILE =
        BCRegistries.registerTile("laser", TileLaser::new, LASER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileAssemblyTable>> ASSEMBLY_TABLE_TILE =
        BCRegistries.registerTile("assembly_table", TileAssemblyTable::new, ASSEMBLY_TABLE);
    public static final DeferredHolder<BlockEntityType<?>,
        BlockEntityType<TileAdvancedCraftingTable>> ADVANCED_CRAFTING_TABLE_TILE = BCRegistries.registerTile(
            "advanced_crafting_table", TileAdvancedCraftingTable::new, ADVANCED_CRAFTING_TABLE);
    public static final DeferredHolder<BlockEntityType<?>,
        BlockEntityType<TileIntegrationTable>> INTEGRATION_TABLE_TILE = BCRegistries.registerTile(
            "integration_table", TileIntegrationTable::new, INTEGRATION_TABLE);

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerAssemblyTable>> ASSEMBLY_TABLE_MENU =
        BCRegistries.MENU_TYPES.register("assembly_table",
            () -> IMenuTypeExtension.create(ContainerAssemblyTable::fromNetwork));
    public static final DeferredHolder<MenuType<?>,
        MenuType<ContainerAdvancedCraftingTable>> ADVANCED_CRAFTING_TABLE_MENU = BCRegistries.MENU_TYPES.register(
            "advanced_crafting_table", () -> IMenuTypeExtension.create(ContainerAdvancedCraftingTable::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerIntegrationTable>> INTEGRATION_TABLE_MENU =
        BCRegistries.MENU_TYPES.register("integration_table",
            () -> IMenuTypeExtension.create(ContainerIntegrationTable::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerGate>> GATE_MENU = BCRegistries.MENU_TYPES
        .register("gate", () -> IMenuTypeExtension.create(ContainerGate::fromNetwork));

    static {
        BCRegistries.registerBlockItem(LASER);
        BCRegistries.registerBlockItem(ASSEMBLY_TABLE);
        BCRegistries.registerBlockItem(ADVANCED_CRAFTING_TABLE);
        BCRegistries.registerBlockItem(INTEGRATION_TABLE);
    }

    private BCSiliconBlocks() {}

    /** 1.12: {@code Material.IRON} with the default BuildCraft hardness. The models aren't full cubes. */
    public static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL)
            .requiresCorrectToolForDrops().noOcclusion();
    }

    static void init() {}
}
