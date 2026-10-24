/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import buildcraftreborn.silicon.BCSiliconItems;

/** Copies a gate's statements: click a gate to copy it (if empty) or paste onto it (if full). Sneak and use to
 * clear it.
 * <p>
 * 1.21: the copied gate is the {@code gate_data} data component (1.12 kept it in the item's NBT and showed it as
 * metadata 1). */
public class ItemGateCopier extends Item {
    public ItemGateCopier(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (getCopiedGateData(stack) != null) {
            tooltip.add(Component.translatable("buildcraft.item.nonclean.usage").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !player.isShiftKeyDown() || getCopiedGateData(stack) == null) {
            return InteractionResultHolder.pass(stack);
        }
        stack.remove(BCSiliconItems.GATE_DATA.get());
        player.displayClientMessage(Component.translatable("chat.gateCopier.clear"), true);
        return InteractionResultHolder.success(stack);
    }

    @Nullable
    public static CompoundTag getCopiedGateData(ItemStack stack) {
        CompoundTag nbt = stack.get(BCSiliconItems.GATE_DATA.get());
        return nbt == null ? null : nbt.copy();
    }

    public static void setCopiedGateData(ItemStack stack, CompoundTag nbt) {
        stack.set(BCSiliconItems.GATE_DATA.get(), nbt.copy());
    }
}
