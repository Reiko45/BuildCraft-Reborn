/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.pipe;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraftreborn.api.core.EnumHandlerPriority;
import buildcraftreborn.api.transport.IStripesActivator;
import buildcraftreborn.api.transport.IStripesHandlerBlock;
import buildcraftreborn.api.transport.IStripesHandlerItem;
import buildcraftreborn.api.transport.IStripesRegistry;

public enum StripesRegistry implements IStripesRegistry {
    INSTANCE;

    private final EnumMap<EnumHandlerPriority, List<IStripesHandlerItem>> itemHandlers = new EnumMap<>(EnumHandlerPriority.class);
    private final EnumMap<EnumHandlerPriority, List<IStripesHandlerBlock>> blockHandlers = new EnumMap<>(EnumHandlerPriority.class);

    StripesRegistry() {
        for (EnumHandlerPriority priority : EnumHandlerPriority.VALUES) {
            itemHandlers.put(priority, new ArrayList<>());
            blockHandlers.put(priority, new ArrayList<>());
        }
    }

    @Override
    public void addHandler(IStripesHandlerItem handler, EnumHandlerPriority priority) {
        itemHandlers.get(priority).add(handler);
    }

    @Override
    public void addHandler(IStripesHandlerBlock handler, EnumHandlerPriority priority) {
        blockHandlers.get(priority).add(handler);
    }

    /** @return True if a handler handled the itemstack, false otherwise (and so nothing has been done) */
    @Override
    public boolean handleItem(Level level,
                              BlockPos pos,
                              Direction direction,
                              ItemStack stack,
                              Player player,
                              IStripesActivator activator) {
        for (EnumHandlerPriority priority : EnumHandlerPriority.VALUES) {
            for (IStripesHandlerItem handler : itemHandlers.get(priority)) {
                if (handler.handle(level, pos, direction, stack, player, activator)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** @return True if a handler broke a block, false otherwise (and so nothing has been done) */
    @Override
    public boolean handleBlock(Level level,
                               BlockPos pos,
                               Direction direction,
                               Player player,
                               IStripesActivator activator) {
        for (EnumHandlerPriority priority : EnumHandlerPriority.VALUES) {
            for (IStripesHandlerBlock handler : blockHandlers.get(priority)) {
                if (handler.handle(level, pos, direction, player, activator)) {
                    return true;
                }
            }
        }
        return false;
    }
}
