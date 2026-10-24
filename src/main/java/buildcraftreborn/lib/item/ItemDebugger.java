/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraftreborn.lib.debug.BCAdvDebugging;
import buildcraftreborn.lib.debug.IAdvDebugTarget;

/** The debugger: while held (or in creative), the F3 screen shows the BuildCraft debug info of the block looked at,
 * from both sides. Using it on a BuildCraft block makes that block the "advanced debug" target, which draws extra
 * details in the world (the quarry's loaded chunks, the blocks a laser can see). */
public class ItemDebugger extends Item {
    public ItemDebugger(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().isClientSide) {
            return InteractionResult.PASS;
        }
        BlockEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
        if (tile instanceof IAdvDebugTarget target) {
            BCAdvDebugging.setCurrentDebugTarget(target);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.debugger.warning").withStyle(ChatFormatting.RED));
    }

    public static boolean isShowDebugInfo(Player player) {
        return player.isCreative()
            || player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemDebugger
            || player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof ItemDebugger;
    }
}
