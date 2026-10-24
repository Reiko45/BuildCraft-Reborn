/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.stripes;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.common.IShearable;

import buildcraftreborn.api.transport.IStripesActivator;
import buildcraftreborn.api.transport.IStripesHandlerItem;

/** Shears a shearable block (leaves, vines, cobwebs...) in front of the pipe, sending the drops back down it. */
public enum StripesHandlerShears implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(Level level, BlockPos pos, Direction direction, ItemStack stack, Player player,
        IStripesActivator activator) {
        if (!(stack.getItem() instanceof ShearsItem)) {
            return false;
        }
        pos = pos.relative(direction);
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof IShearable shearable && shearable.isShearable(player, stack, level, pos)) {
            List<ItemStack> drops = shearable.onSheared(player, stack, level, pos);
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                Block.UPDATE_ALL_IMMEDIATE);
            for (ItemStack drop : drops) {
                activator.sendItem(drop, direction);
            }
            return true;
        }
        return false;
    }
}
