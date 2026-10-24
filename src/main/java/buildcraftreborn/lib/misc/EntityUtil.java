/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

import buildcraftreborn.api.tools.IToolWrench;

public final class EntityUtil {
    private EntityUtil() {}

    /** @return The hand the entity holds a wrench in (main hand first), or null if it holds none. */
    @Nullable
    public static InteractionHand getWrenchHand(LivingEntity entity) {
        ItemStack stack = entity.getMainHandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            return InteractionHand.MAIN_HAND;
        }
        stack = entity.getOffhandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    public static void activateWrench(Player player, HitResult trace) {
        InteractionHand hand = getWrenchHand(player);
        if (hand != null) {
            ItemStack stack = player.getItemInHand(hand);
            ((IToolWrench) stack.getItem()).wrenchUsed(player, hand, stack, trace);
        }
    }
}
