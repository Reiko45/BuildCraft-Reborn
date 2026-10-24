/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.core;

import java.util.List;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import buildcraftreborn.core.item.ItemFragileFluidContainer;
import buildcraftreborn.core.item.ItemList_BC8;
import buildcraftreborn.core.item.ItemMapLocation;
import buildcraftreborn.core.item.ItemMarkerConnector;
import buildcraftreborn.core.item.ItemPaintbrush_BC8;
import buildcraftreborn.core.item.ItemVolumeBox;
import buildcraftreborn.core.item.ItemWrench_Neptune;
import buildcraftreborn.core.list.ContainerList;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.item.ItemDebugger;
import buildcraftreborn.lib.item.ItemGuide;

/**
 * Core items. 1.12 registered these in {@code BCCoreItems.preInit()} as {@code ItemBC_Neptune("item.gear.wood")}
 * with registry name {@code buildcraftcore:gear_wood}; the registry paths are unchanged, only the namespace moves to
 * {@code buildcraftreborn}.
 */
public final class BCCoreItems {
    public static final DeferredItem<ItemWrench_Neptune> WRENCH =
        BCRegistries.ITEMS.registerItem("wrench", ItemWrench_Neptune::new);

    public static final DeferredItem<Item> GEAR_WOOD = BCRegistries.ITEMS.registerSimpleItem("gear_wood");
    public static final DeferredItem<Item> GEAR_STONE = BCRegistries.ITEMS.registerSimpleItem("gear_stone");
    public static final DeferredItem<Item> GEAR_IRON = BCRegistries.ITEMS.registerSimpleItem("gear_iron");
    public static final DeferredItem<Item> GEAR_GOLD = BCRegistries.ITEMS.registerSimpleItem("gear_gold");
    public static final DeferredItem<Item> GEAR_DIAMOND = BCRegistries.ITEMS.registerSimpleItem("gear_diamond");

    public static final DeferredItem<ItemPaintbrush_BC8> PAINTBRUSH =
        BCRegistries.ITEMS.registerItem("paintbrush", ItemPaintbrush_BC8::new);

    public static final DeferredItem<ItemMarkerConnector> MARKER_CONNECTOR =
        BCRegistries.ITEMS.registerItem("marker_connector", ItemMarkerConnector::new);

    /** Makes volume boxes. Creative only, as in 1.12 (no recipe). */
    public static final DeferredItem<ItemVolumeBox> VOLUME_BOX =
        BCRegistries.ITEMS.registerItem("volume_box", ItemVolumeBox::new);

    /** Filters for pipes and gates (two lines of nine items, with "same type" and "same material" options). */
    public static final DeferredItem<ItemList_BC8> LIST = BCRegistries.ITEMS.registerItem("list", ItemList_BC8::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerList>> LIST_MENU =
        BCRegistries.MENU_TYPES.register("list", () -> IMenuTypeExtension.create(ContainerList::fromNetwork));

    /** Remembers a spot, area, path or zone. Creative only, as in 8.0 (no recipe). */
    public static final DeferredItem<ItemMapLocation> MAP_LOCATION =
        BCRegistries.ITEMS.registerItem("map_location", ItemMapLocation::new);

    /** Shows BuildCraft debug info on the F3 screen, and picks the advanced debug target. */
    public static final DeferredItem<ItemDebugger> DEBUGGER =
        BCRegistries.ITEMS.registerItem("debugger", ItemDebugger::new);

    /** The guide book (1.12's {@code buildcraftlib:guide}): one per book. */
    public static final DeferredItem<ItemGuide> GUIDE = BCRegistries.ITEMS.registerItem("guide", ItemGuide::new);
    /** A guide book note (1.12's {@code buildcraftlib:guide_note}). 1.12 never finished notes, so it does nothing. */
    public static final DeferredItem<Item> GUIDE_NOTE = BCRegistries.ITEMS.registerSimpleItem("guide_note");

    /** What broken tanks leave their fluid in. Not in the creative tab. */
    public static final DeferredItem<ItemFragileFluidContainer> FRAGILE_FLUID_SHARD =
        BCRegistries.ITEMS.registerItem("fragile_fluid_shard", ItemFragileFluidContainer::new);

    /** All gears, cheapest first. Each tier is crafted around the one before it. */
    public static final List<DeferredItem<Item>> GEARS = List.of(GEAR_WOOD, GEAR_STONE, GEAR_IRON, GEAR_GOLD, GEAR_DIAMOND);

    private BCCoreItems() {}

    /** Forces class loading so the holders above exist before the registers attach to the mod bus. */
    static void init() {}
}
