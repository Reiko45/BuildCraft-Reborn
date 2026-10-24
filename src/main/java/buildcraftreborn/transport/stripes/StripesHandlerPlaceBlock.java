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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.api.transport.IStripesActivator;
import buildcraftreborn.api.transport.IStripesHandlerItem;

/** Places a block in the empty space in front of the pipe. */
public enum StripesHandlerPlaceBlock implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(Level level, BlockPos pos, Direction direction, ItemStack stack, Player player,
        IStripesActivator activator) {
        if (!(stack.getItem() instanceof BlockItem)) {
            return false;
        }
        BlockPos target = pos.relative(direction);
        if (!level.isEmptyBlock(target)) {
            return false;
        }
        // Click on the far face of the target space, as if the pipe were placing it against its own end.
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(target), direction, target, false);
        stack.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, stack, hit));
        return true;
    }
}
