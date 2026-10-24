/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.stripes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import buildcraftreborn.api.crops.CropManager;
import buildcraftreborn.api.transport.IStripesActivator;
import buildcraftreborn.api.transport.IStripesHandlerItem;

/** Plants seeds, saplings and other plants in front of the pipe, on the block below the front (or on the front
 * block), through BuildCraft's crop API, as 1.12 did. (Before the robots brought the crop API back, this port planted
 * plant blocks itself.) */
public enum StripesHandlerPlant implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(Level level, BlockPos pos, Direction direction, ItemStack stack, Player player,
        IStripesActivator activator) {
        if (CropManager.getDefaultHandler() == null) {
            return false;
        }
        BlockPos front = pos.relative(direction);
        return CropManager.plantCrop(level, player, stack, front.below())
            || CropManager.plantCrop(level, player, stack, front);
    }
}
