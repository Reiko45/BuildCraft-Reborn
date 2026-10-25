/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.silicon;

import java.util.function.Supplier;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import buildcraftreborn.api.enums.EnumLaserTableType;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.silicon.block.BlockLaserTable;
import buildcraftreborn.silicon.block.BlockPackager;
import buildcraftreborn.silicon.container.ContainerPackager;
import buildcraftreborn.silicon.container.ContainerStampingTable;
import buildcraftreborn.silicon.entity.EntityPackage;
import buildcraftreborn.silicon.item.ItemPackage;
import buildcraftreborn.silicon.tile.TilePackager;
import buildcraftreborn.silicon.tile.TileStampingTable;

/** BuildCraft 7.1's crafting by package, which 8.0 never ported: the recipe packager packs a crafting grid's
 * ingredients into a package, pipes and robots carry packages around, and the stamping table (a laser table) crafts
 * them. A package can also be thrown, by hand, by a dispenser or by a stripes pipe, and bursts open where it lands. */
public final class BCSiliconPackaging {
    /** The items in a package, by crafting grid slot (7.1: the {@code item0} to {@code item8} NBT tags). */
    public static final Supplier<DataComponentType<ItemContainerContents>> PACKAGE_CONTENTS = BCRegistries
        .DATA_COMPONENTS.registerComponentType("package_contents", b -> b.persistent(ItemContainerContents.CODEC)
            .networkSynchronized(ItemContainerContents.STREAM_CODEC).cacheEncoding());

    public static final DeferredItem<ItemPackage> PACKAGE = BCRegistries.ITEMS.register("package",
        () -> new ItemPackage(new Item.Properties()));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityPackage>> PACKAGE_ENTITY =
        BCRegistries.ENTITY_TYPES.register("package", () -> EntityType.Builder.<EntityPackage> of(EntityPackage::new,
            MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).build("package"));

    public static final DeferredBlock<BlockPackager> PACKAGER = BCRegistries.BLOCKS.register("packager",
        () -> new BlockPackager(BlockBCBase_Neptune.defaultProperties().strength(10.0F, 10.0F)));
    public static final DeferredBlock<BlockLaserTable> STAMPING_TABLE = BCRegistries.BLOCKS.register(
        "stamping_table", () -> new BlockLaserTable(EnumLaserTableType.STAMPING_TABLE, BCSiliconBlocks.properties(),
            TileStampingTable::new));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TilePackager>> PACKAGER_TILE =
        BCRegistries.registerTile("packager", TilePackager::new, PACKAGER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileStampingTable>> STAMPING_TABLE_TILE =
        BCRegistries.registerTile("stamping_table", TileStampingTable::new, STAMPING_TABLE);

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerPackager>> PACKAGER_MENU =
        BCRegistries.MENU_TYPES.register("packager", () -> IMenuTypeExtension.create(ContainerPackager::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerStampingTable>> STAMPING_TABLE_MENU =
        BCRegistries.MENU_TYPES.register("stamping_table",
            () -> IMenuTypeExtension.create(ContainerStampingTable::fromNetwork));

    static {
        BCRegistries.registerBlockItem(PACKAGER);
        BCRegistries.registerBlockItem(STAMPING_TABLE);
        // An empty package does nothing, so (as in 7.1) it isn't in a creative tab
        BCRegistries.NOT_IN_MAIN_TAB.add(PACKAGE.getId());
    }

    private BCSiliconPackaging() {}

    static void init(IEventBus modBus) {
        modBus.addListener(BCSiliconPackaging::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        // Dispensers (and so stripes pipes, which use dispenser behaviours) throw packages
        event.enqueueWork(() -> DispenserBlock.registerProjectileBehavior(PACKAGE.get()));
    }
}
