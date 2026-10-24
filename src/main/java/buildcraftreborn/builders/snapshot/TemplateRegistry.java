/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraftreborn.api.core.EnumHandlerPriority;
import buildcraftreborn.api.template.ITemplateHandler;
import buildcraftreborn.api.template.ITemplateRegistry;

public enum TemplateRegistry implements ITemplateRegistry {
    INSTANCE;

    private final EnumMap<EnumHandlerPriority, List<ITemplateHandler>> handlers = new EnumMap<>(EnumHandlerPriority.class);

    TemplateRegistry() {
        for (EnumHandlerPriority priority : EnumHandlerPriority.VALUES) {
            handlers.put(priority, new ArrayList<>());
        }
    }

    @Override
    public void addHandler(ITemplateHandler handler, EnumHandlerPriority priority) {
        handlers.get(priority).add(handler);
    }

    @Override
    public boolean handle(Level level, BlockPos pos, Player player, ItemStack stack) {
        for (EnumHandlerPriority priority : EnumHandlerPriority.VALUES) {
            for (ITemplateHandler handler : handlers.get(priority)) {
                if (handler.handle(level, pos, player, stack)) {
                    return true;
                }
            }
        }
        return false;
    }
}
