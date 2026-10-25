/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import buildcraftreborn.robotics.BCRoboticsItems;

/** Robot goggles, which BuildCraft started after 7.1 (in its unfinished 1.8 robotics) and never finished: worn, they
 * make robots glow through walls, tinted by how charged they are ({@code EntityRobot.isCurrentlyGlowing}), and show
 * laser beams even when the client config hides them (what 1.12's development-only goggles did). Like that unfinished
 * item they are a helmet that gives no protection and is never damaged. */
public class ItemRobotGoggles extends ArmorItem {
    public ItemRobotGoggles(Properties properties) {
        super(BCRoboticsItems.GOGGLES_MATERIAL, Type.HELMET, properties);
    }

    /** @return Whether the entity has robot goggles on its head. */
    public static boolean isWornBy(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ItemRobotGoggles;
    }

    // No armour points (an armour item would show "+0 Armor" otherwise)

    @Override
    @SuppressWarnings("deprecation")
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return ItemAttributeModifiers.EMPTY;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return ItemAttributeModifiers.EMPTY;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        String key = getDescriptionId() + ".tip";
        tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        if (flag.hasShiftDown() && Language.getInstance().has(key + ".shift")) {
            for (String line : Language.getInstance().getOrDefault(key + ".shift").split("\n")) {
                tooltip.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
