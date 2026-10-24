/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.block;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Tells registered subscribers about block changes within their range.
 * <p>
 * 1.21: 1.12 listened to every block update through a world event listener; NeoForge has none, so this listens to
 * {@link BlockEvent.NeighborNotifyEvent}, which fires whenever a block change notifies its neighbours (placing,
 * breaking and most state changes). */
public final class LocalBlockUpdateNotifier {
    private static final Map<LevelAccessor, LocalBlockUpdateNotifier> INSTANCES = new WeakHashMap<>();
    private static boolean registered = false;

    private final Set<ILocalBlockUpdateSubscriber> subscribers =
        java.util.Collections.newSetFromMap(new WeakHashMap<>());

    private LocalBlockUpdateNotifier() {}

    public static LocalBlockUpdateNotifier instance(Level level) {
        if (!registered) {
            registered = true;
            NeoForge.EVENT_BUS.addListener(LocalBlockUpdateNotifier::onNeighbourNotify);
        }
        return INSTANCES.computeIfAbsent(level, l -> new LocalBlockUpdateNotifier());
    }

    public void registerSubscriberForUpdateNotifications(ILocalBlockUpdateSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void removeSubscriberFromUpdateNotifications(ILocalBlockUpdateSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    private static void onNeighbourNotify(BlockEvent.NeighborNotifyEvent event) {
        LocalBlockUpdateNotifier notifier = INSTANCES.get(event.getLevel());
        if (notifier != null && event.getLevel() instanceof Level level) {
            notifier.notifySubscribersInRange(level, event.getPos());
        }
    }

    private void notifySubscribersInRange(Level level, BlockPos eventPos) {
        for (ILocalBlockUpdateSubscriber subscriber : Set.copyOf(subscribers)) {
            BlockPos keyPos = subscriber.getSubscriberPos();
            int range = subscriber.getUpdateRange();
            if (Math.abs(keyPos.getX() - eventPos.getX()) <= range && Math.abs(keyPos.getY() - eventPos.getY()) <= range
                && Math.abs(keyPos.getZ() - eventPos.getZ()) <= range) {
                subscriber.setWorldUpdated(level, eventPos, level.getBlockState(eventPos));
            }
        }
    }
}
