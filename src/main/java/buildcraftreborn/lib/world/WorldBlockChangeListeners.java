/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.world;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Per-level listeners told about every block change.
 * <p>
 * 1.21: replaces 1.12's {@code IWorldEventListener.notifyBlockUpdate} (registered through
 * {@code World.addEventListener}), which NeoForge doesn't have. Changes are picked up from
 * {@link BlockEvent.NeighborNotifyEvent}, which fires whenever a block change updates its neighbours (placing,
 * breaking and most state changes), and from {@link BlockEvent.BreakEvent} and {@link BlockEvent.EntityPlaceEvent}. */
public final class WorldBlockChangeListeners {
    @FunctionalInterface
    public interface IBlockChangeListener {
        void onBlockChanged(Level level, BlockPos pos, BlockState newState);
    }

    private static final Map<LevelAccessor, Set<IBlockChangeListener>> LISTENERS = new WeakHashMap<>();
    private static boolean registered = false;

    private WorldBlockChangeListeners() {}

    public static synchronized void add(Level level, IBlockChangeListener listener) {
        if (!registered) {
            registered = true;
            NeoForge.EVENT_BUS.addListener(WorldBlockChangeListeners::onNeighbourNotify);
        }
        LISTENERS.computeIfAbsent(level, l -> Collections.newSetFromMap(new WeakHashMap<>())).add(listener);
    }

    public static synchronized void remove(Level level, IBlockChangeListener listener) {
        Set<IBlockChangeListener> set = LISTENERS.get(level);
        if (set != null) {
            set.remove(listener);
        }
    }

    private static void onNeighbourNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof Level level)) {
            return;
        }
        List<IBlockChangeListener> listeners;
        synchronized (WorldBlockChangeListeners.class) {
            Set<IBlockChangeListener> set = LISTENERS.get(level);
            if (set == null || set.isEmpty()) {
                return;
            }
            listeners = List.copyOf(set);
        }
        for (IBlockChangeListener listener : listeners) {
            listener.onBlockChanged(level, event.getPos(), event.getState());
        }
    }
}
