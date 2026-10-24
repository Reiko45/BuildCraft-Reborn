/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.item;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import buildcraftreborn.api.transport.IItemPluggable;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.lib.item.IItemSubtypes;
import buildcraftreborn.lib.misc.SoundUtil;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.BCSiliconPlugs;
import buildcraftreborn.silicon.gate.EnumGateLogic;
import buildcraftreborn.silicon.gate.EnumGateMaterial;
import buildcraftreborn.silicon.gate.EnumGateModifier;
import buildcraftreborn.silicon.gate.GateVariant;
import buildcraftreborn.silicon.plug.PluggableGate;

/** A gate, placed on a pipe side. Its variant (logic, material, modifier) is the {@code gate} data component; without
 * one it's a basic (clay brick) gate.
 * <p>
 * 1.21: the variant was in the item's NBT in 1.12. */
public class ItemPluggableGate extends Item implements IItemPluggable, IItemSubtypes {
    public static final GateVariant BASIC = new GateVariant(buildcraftreborn.silicon.gate.EnumGateLogic.AND,
        buildcraftreborn.silicon.gate.EnumGateMaterial.CLAY_BRICK,
        buildcraftreborn.silicon.gate.EnumGateModifier.NO_MODIFIER);

    public ItemPluggableGate(Properties properties) {
        super(properties);
    }

    public static GateVariant getVariant(ItemStack stack) {
        GateVariant variant = stack.get(BCSiliconItems.GATE_VARIANT.get());
        return variant == null ? BASIC : variant;
    }

    /** The basic gate has no {@code gate} component, so crafted ones stack with the creative tab's. */
    public ItemStack getStack(GateVariant variant) {
        ItemStack stack = new ItemStack(this);
        if (!BASIC.equals(variant)) {
            stack.set(BCSiliconItems.GATE_VARIANT.get(), variant);
        }
        return stack;
    }

    @Override
    public PipePluggable onPlace(ItemStack stack, IPipeHolder holder, Direction side, Player player,
        InteractionHand hand) {
        GateVariant variant = getVariant(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), variant.material.block.defaultBlockState());
        return new PluggableGate(BCSiliconPlugs.gate, holder, side, variant);
    }

    @Override
    public Component getName(ItemStack stack) {
        return getVariant(stack).getDisplayName();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        GateVariant variant = getVariant(stack);
        tooltip.add(Component.translatable("gate.slots", variant.numSlots).withStyle(ChatFormatting.GRAY));
        if (variant.numTriggerArgs == variant.numActionArgs) {
            if (variant.numTriggerArgs > 0) {
                tooltip.add(Component.translatable("gate.params", variant.numTriggerArgs)
                    .withStyle(ChatFormatting.GRAY));
            }
        } else {
            if (variant.numTriggerArgs > 0) {
                tooltip.add(Component.translatable("gate.params.trigger", variant.numTriggerArgs)
                    .withStyle(ChatFormatting.GRAY));
            }
            if (variant.numActionArgs > 0) {
                // (1.12 showed the trigger count here)
                tooltip.add(Component.translatable("gate.params.action", variant.numActionArgs)
                    .withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public void addSubtypes(Consumer<ItemStack> output) {
        output.accept(new ItemStack(this));
        for (EnumGateMaterial material : EnumGateMaterial.VALUES) {
            if (!material.canBeModified) {
                continue;
            }
            for (EnumGateLogic logic : EnumGateLogic.VALUES) {
                for (EnumGateModifier modifier : EnumGateModifier.VALUES) {
                    output.accept(getStack(new GateVariant(logic, material, modifier)));
                }
            }
        }
    }
}
