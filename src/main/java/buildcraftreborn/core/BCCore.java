/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.core;

import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import buildcraftreborn.api.items.FluidItemDrops;
import buildcraftreborn.core.marker.PathCache;
import buildcraftreborn.core.marker.VolumeCache;
import buildcraftreborn.core.marker.volume.VolumeBoxEvents;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.debug.BCAdvDebugging;
import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.lib.item.IItemSubtypes;
import buildcraftreborn.lib.list.VanillaListHandlers;
import buildcraftreborn.lib.marker.MarkerCache;

/**
 * Core module. In 1.12 this was the {@code buildcraftcore} mod; here it is a package, initialised from
 * {@code BuildCraftReborn}'s constructor.
 */
public final class BCCore {
    public static final String TAB_MAIN_KEY = "itemGroup.buildcraftreborn.main";

    /** The main creative tab ("buildcraft.main" in 1.12), with the wrench as its icon. */
    public static final Supplier<CreativeModeTab> TAB_MAIN = BCRegistries.CREATIVE_TABS.register("main",
        () -> CreativeModeTab.builder()
            .title(Component.translatable(TAB_MAIN_KEY))
            .icon(() -> new ItemStack(BCCoreItems.WRENCH.get()))
            .displayItems((params, output) -> BCRegistries.ITEMS.getEntries().stream()
                .filter(item -> item != BCCoreItems.FRAGILE_FLUID_SHARD)
                .filter(item -> !BCRegistries.NOT_IN_MAIN_TAB.contains(item.getId()))
                .filter(item -> BCRegistries.isShownInCreative(item.getId()))
                .forEach(item -> IItemSubtypes.accept(output, item.get())))
            .build());

    private BCCore() {}

    public static void init() {
        BCItemComponents.init();
        BCCoreItems.init();
        BCCoreBlocks.init();
        BCCoreStatements.init();
        VanillaListHandlers.init();
        MarkerCache.registerCache(VolumeCache.INSTANCE);
        MarkerCache.registerCache(PathCache.INSTANCE);
        VolumeBoxEvents.register();
        WrenchInterop.register();
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> BCAdvDebugging.INSTANCE.onServerPostTick());
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> BCAdvDebugging.INSTANCE.onServerStopped());
    }

    /** Called from common setup, once every item exists. */
    public static void commonSetup() {
        FluidItemDrops.item = BCCoreItems.FRAGILE_FLUID_SHARD.get();
    }
}
