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
import net.neoforged.neoforge.common.Tags;

import buildcraftreborn.api.tools.IToolWrench;

public final class EntityUtil {
    private EntityUtil() {}

    /** @return Whether the stack is a wrench: BuildCraft's (any {@link IToolWrench}) or one in the common
     *         {@code c:tools/wrench} tag, such as other mods' wrenches (1.12 only knew {@link IToolWrench}). */
    public static boolean isWrench(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof IToolWrench || stack.is(Tags.Items.TOOLS_WRENCH));
    }

    /** @return The hand the entity holds a wrench in (main hand first), or null if it holds none. */
    @Nullable
    public static InteractionHand getWrenchHand(LivingEntity entity) {
        if (isWrench(entity.getMainHandItem())) {
            return InteractionHand.MAIN_HAND;
        }
        if (isWrench(entity.getOffhandItem())) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    public static void activateWrench(Player player, HitResult trace) {
        InteractionHand hand = getWrenchHand(player);
        if (hand != null) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof IToolWrench wrench) {
                wrench.wrenchUsed(player, hand, stack, trace);
            } else {
                player.swing(hand);
            }
        }
    }
}
