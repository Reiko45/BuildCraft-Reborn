/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;

/** Development-only content that exercises the phase 1 foundation: a test machine with a tank, an MJ battery, one item
 * slot and a GUI. Like 1.12's power tester, it is registered only in a development environment (never in a
 * released jar). */
public final class BCTestContent {
    /** True in a development environment ({@code runClient}, {@code runGameTestServer}, {@code runData}). */
    public static final boolean ENABLED = !FMLEnvironment.production;

    public static DeferredBlock<BlockTestMachine> TEST_MACHINE_BLOCK;
    public static DeferredItem<?> TEST_MACHINE_ITEM;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileTestMachine>> TEST_MACHINE_TILE;
    public static DeferredHolder<MenuType<?>, MenuType<ContainerTestMachine>> TEST_MACHINE_MENU;

    private BCTestContent() {}

    public static void init() {
        if (!ENABLED) {
            return;
        }
        // No loot table and no tags: datagen runs in development, so anything generated for this block would ship in the
        // released jar, where the block doesn't exist (a missing entry makes Minecraft drop the whole tag).
        TEST_MACHINE_BLOCK = BCRegistries.BLOCKS.register("test_machine",
            () -> new BlockTestMachine(BlockBCBase_Neptune.defaultProperties().noLootTable()));
        TEST_MACHINE_ITEM = BCRegistries.ITEMS.registerSimpleBlockItem(TEST_MACHINE_BLOCK);
        TEST_MACHINE_TILE = BCRegistries.registerTile("test_machine", TileTestMachine::new, TEST_MACHINE_BLOCK);
        TEST_MACHINE_MENU = BCRegistries.MENU_TYPES.register("test_machine",
            () -> IMenuTypeExtension.create(ContainerTestMachine::fromNetwork));
    }
}
