/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport;

import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.item.IItemSubtypes;
import buildcraftreborn.lib.registry.PluggableRegistry;
import buildcraftreborn.transport.net.PipeItemMessageQueue;
import buildcraftreborn.transport.wire.WorldSavedDataWireSystems;

/** Transport module (1.12's {@code buildcrafttransport} mod): pipes, pipe plugs, pipe wires and the filtered buffer.
 * Initialised from {@code BuildCraftReborn}'s constructor. */
public final class BCTransport {
    public static final String TAB_PIPES_KEY = "itemGroup.buildcraftreborn.pipes";
    public static final String TAB_PLUGS_KEY = "itemGroup.buildcraftreborn.plugs";

    /** Every pipe ("buildcraft.pipes" in 1.12), with the diamond item pipe as its icon. */
    public static final Supplier<CreativeModeTab> TAB_PIPES = BCRegistries.CREATIVE_TABS.register("pipes",
        () -> CreativeModeTab.builder()
            .title(Component.translatable(TAB_PIPES_KEY))
            .icon(() -> new ItemStack(BCTransportItems.PIPES.get(BCTransportPipes.diamondItem).get()))
            .displayItems((params, output) -> BCTransportItems.PIPES.values().forEach(i -> output.accept(i.get())))
            .build());

    /** Pipe plugs, wires and (with silicon) gates and lenses ("buildcraft.plugs" in 1.12). */
    public static final Supplier<CreativeModeTab> TAB_PLUGS = BCRegistries.CREATIVE_TABS.register("plugs",
        () -> CreativeModeTab.builder()
            .title(Component.translatable(TAB_PLUGS_KEY))
            .icon(() -> new ItemStack(BCTransportItems.PLUG_BLOCKER.get()))
            .displayItems((params, output) -> BCTransportItems.getPlugTabItems()
                .forEach(i -> IItemSubtypes.accept(output, i.get())))
            .build());

    private BCTransport() {}

    public static void init(IEventBus modBus, ModContainer container) {
        PipeApi.pluggableRegistry = PluggableRegistry.INSTANCE;
        BCTransportRegistries.init();
        BCTransportPipes.init();
        BCTransportPlugs.init();
        BCTransportBlocks.init();
        BCTransportItems.init();
        BCTransportStatements.init();
        BCTransportRecipes.init();
        // Transfer rates need the pipe definitions; they're applied again once the config loads.
        BCTransportConfig.reloadConfig();

        container.registerConfig(ModConfig.Type.COMMON, BCTransportConfig.SPEC,
            BuildCraftReborn.MODID + "-transport.toml");
        modBus.addListener(BCTransportConfig::onConfigLoad);

        NeoForge.EVENT_BUS.addListener(BCTransport::onLevelTick);
        NeoForge.EVENT_BUS.addListener(BCTransport::onServerTick);
        NeoForge.EVENT_BUS.addListener(BCTransport::onChunkWatch);
    }

    /** Called in common setup, once every block and item exists. */
    public static void commonSetup() {
        BCTransportRegistries.commonSetup();
    }

    private static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            WorldSavedDataWireSystems.get(level).tick();
        }
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        PipeItemMessageQueue.serverTick();
    }

    private static void onChunkWatch(ChunkWatchEvent.Watch event) {
        ServerPlayer player = event.getPlayer();
        WorldSavedDataWireSystems.get(event.getLevel()).changedPlayers.add(player);
    }
}
