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

import buildcraftreborn.api.transport.IStripesActivator;
import buildcraftreborn.api.transport.IStripesHandlerItem;
import buildcraftreborn.api.transport.pipe.IItemPipe;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pipe.PipeDefinition;

/** An item pipe sent into a stripes pipe extends the stripes pipe by one block (at the end of the tick). */
public class StripesHandlerPipes implements IStripesHandlerItem {

    @Override
    public boolean handle(Level level, BlockPos pos, Direction direction, ItemStack stack, Player player,
        IStripesActivator activator) {
        if (!(stack.getItem() instanceof IItemPipe itemPipe)) {
            return false;
        }
        PipeDefinition pipeDefinition = itemPipe.getDefinition();
        if (pipeDefinition.flowType == PipeApi.flowItems) {
            if (PipeApi.extensionManager.requestPipeExtension(level, pos, direction, activator, stack.copy())) {
                // No items are sent back now: the extension manager handles that later
                player.getInventory().clearContent();
                return true;
            }
        }
        return false;
    }
}
