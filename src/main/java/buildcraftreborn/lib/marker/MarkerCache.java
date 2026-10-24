/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import buildcraftreborn.BuildCraftReborn;

/** One kind of marker (volume or path). Holds a {@link MarkerSubCache} for every loaded level, separately for the
 * client and the server. Caches are registered in the order the modules load, and that index identifies them in
 * {@link buildcraftreborn.lib.net.MarkerPayload}. */
public abstract class MarkerCache<S extends MarkerSubCache<?>> {
    public static final boolean DEBUG = Boolean.getBoolean("buildcraftreborn.debug.markers");

    public static final List<MarkerCache<?>> CACHES = new ArrayList<>();

    public final String name;
    private final Map<ResourceKey<Level>, S> cacheClient = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, S> cacheServer = new ConcurrentHashMap<>();

    public MarkerCache(String name) {
        this.name = name;
    }

    public static void registerCache(MarkerCache<?> cache) {
        CACHES.add(cache);
        if (DEBUG) {
            BuildCraftReborn.LOGGER.info("[lib.markers] Registered a cache {} with an ID of {}", cache.name,
                CACHES.size() - 1);
        }
    }

    /** Registers the game events that keep the caches in step with levels and players. */
    public static void registerEvents() {
        NeoForge.EVENT_BUS.addListener((EntityJoinLevelEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                onPlayerJoinWorld(player);
            }
        });
        NeoForge.EVENT_BUS.addListener((LevelEvent.Unload event) -> {
            if (event.getLevel() instanceof Level level) {
                onWorldUnload(level);
            }
        });
    }

    public static void onPlayerJoinWorld(ServerPlayer player) {
        for (MarkerCache<?> cache : CACHES) {
            cache.getSubCache(player.level()).onPlayerJoinWorld(player);
        }
    }

    public static void onWorldUnload(Level world) {
        for (MarkerCache<?> cache : CACHES) {
            cache.onWorldUnloadImpl(world);
        }
    }

    private void onWorldUnloadImpl(Level world) {
        Map<ResourceKey<Level>, S> cache = world.isClientSide ? cacheClient : cacheServer;
        cache.remove(world.dimension());
    }

    protected abstract S createSubCache(Level world);

    public S getSubCache(Level world) {
        Map<ResourceKey<Level>, S> cache = world.isClientSide ? cacheClient : cacheServer;
        return cache.computeIfAbsent(world.dimension(), k -> createSubCache(world));
    }
}
