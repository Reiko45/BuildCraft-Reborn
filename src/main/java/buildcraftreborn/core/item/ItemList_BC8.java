/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.items.IList;
import buildcraftreborn.core.list.ContainerList;
import buildcraftreborn.lib.list.ListHandler;
import buildcraftreborn.lib.misc.AdvancementUtil;

/** The list: two lines of item filters (with "same type" and "same material" options) that diamond pipes, emzuli
 * pipes and gate triggers accept in place of a single item. Right click to edit it. */
public class ItemList_BC8 extends Item implements IList {
    private static final ResourceLocation ADVANCEMENT = BuildCraftReborn.id("list");

    public ItemList_BC8(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
            serverPlayer.openMenu(new SimpleMenuProvider((windowId, inventory, p) -> new ContainerList(windowId, p),
                Component.translatable(getDescriptionId())));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), world.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String name = getLabel(stack);
        if (!name.isEmpty()) {
            tooltip.add(Component.literal(name).withStyle(ChatFormatting.ITALIC));
        }
    }

    // IList

    @Override
    public String getLabel(ItemStack stack) {
        return ListHandler.getData(stack).label();
    }

    @Override
    public boolean setLabel(ItemStack stack, String name) {
        ListHandler.setData(stack, ListHandler.getData(stack).withLabel(name));
        return true;
    }

    @Override
    public boolean matches(ItemStack stackList, ItemStack item) {
        return ListHandler.matches(stackList, item);
    }
}
