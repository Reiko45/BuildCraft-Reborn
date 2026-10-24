/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.stripes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.api.transport.IStripesActivator;
import buildcraftreborn.api.transport.IStripesHandlerItem;

/** Uses one of {@link #ITEMS} on the block in front of the pipe. Empty by default (as in 1.12), for addons. */
public enum StripesHandlerUse implements IStripesHandlerItem {
    INSTANCE;

    public static final List<Item> ITEMS = new ArrayList<>();

    @Override
    public boolean handle(Level level, BlockPos pos, Direction direction, ItemStack stack, Player player,
        IStripesActivator activator) {
        if (!ITEMS.contains(stack.getItem())) {
            return false;
        }
        BlockPos target = pos.relative(direction);
        Direction face = direction.getOpposite();
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(target), face, target, false);
        UseOnContext ctx = new UseOnContext(level, player, InteractionHand.MAIN_HAND, stack, hit);
        if (stack.onItemUseFirst(ctx).consumesAction()) {
            return true;
        }
        return stack.useOn(ctx).consumesAction();
    }
}
