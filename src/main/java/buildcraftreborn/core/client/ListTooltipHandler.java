/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import buildcraftreborn.api.items.IList;
import buildcraftreborn.core.list.ContainerList;

/** While the list GUI is open, item tooltips say whether the held list matches them. */
public final class ListTooltipHandler {
    private ListTooltipHandler() {}

    public static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && player != null && player.containerMenu instanceof ContainerList) {
            ItemStack list = player.getMainHandItem();
            if (list.getItem() instanceof IList ilist && ilist.matches(list, stack)) {
                event.getToolTip().add(Component.translatable("tip.list.matches").withStyle(ChatFormatting.GREEN));
            }
        }
    }
}
