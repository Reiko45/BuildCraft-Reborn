/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.item;

import java.util.List;
import java.util.Map;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import buildcraftreborn.api.transport.pipe.IItemPipe;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pipe.PipeDefinition;
import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.lib.misc.ColourUtil;
import buildcraftreborn.lib.misc.LocaleUtil;

/** The item of a pipe type. Every pipe item places the same pipe holder block; the tile reads the pipe definition (and
 * the colour, from the {@code colour} data component) from the item.
 * <p>
 * 1.21: one item per pipe type, as in 1.12 (the colour moved from metadata to a data component). The holder block has
 * no item of its own, so these don't register themselves as "the" item of that block. */
public class ItemPipeHolder extends BlockItem implements IItemPipe {
    public final PipeDefinition definition;

    public ItemPipeHolder(Block holder, PipeDefinition definition, Properties properties) {
        super(holder, properties);
        this.definition = definition;
    }

    public ItemPipeHolder registerWithPipeApi() {
        PipeApi.pipeRegistry.setItemForPipe(definition, this);
        return this;
    }

    @Override
    public PipeDefinition getDefinition() {
        return definition;
    }

    @Override
    public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {
        // Many items place the pipe holder: don't make any of them its "block item".
    }

    @Override
    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }

    @Override
    public Component getName(ItemStack stack) {
        Component name = super.getName(stack);
        DyeColor colour = stack.get(BCItemComponents.COLOUR.get());
        if (colour != null) {
            return Component.literal(ColourUtil.getTextFullTooltipSpecial(colour) + " ").append(name);
        }
        return name;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        String key = getDescriptionId() + ".tip";
        if (Language.getInstance().has(key)) {
            tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        }
        // More detail while shift is held (1.12's "tip.shift." lines)
        String shiftKey = key + ".shift";
        if (flag.hasShiftDown() && Language.getInstance().has(shiftKey)) {
            for (String line : Language.getInstance().getOrDefault(shiftKey).split("\n")) {
                tooltip.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
            }
        }
        if (definition.flowType == PipeApi.flowFluids) {
            PipeApi.FluidTransferInfo fti = PipeApi.getFluidTransferInfo(definition);
            tooltip.add(LocaleUtil.localizeFluidFlow(fti.transferPerTick).copy().withStyle(ChatFormatting.GRAY));
        } else if (definition.flowType == PipeApi.flowPower) {
            PipeApi.PowerTransferInfo pti = PipeApi.getPowerTransferInfo(definition);
            tooltip.add(LocaleUtil.localizeMjFlow(pti.transferPerTick).copy().withStyle(ChatFormatting.GRAY));
        } else if (PipeApi.flowRf != null && definition.flowType == PipeApi.flowRf) {
            PipeApi.RedstoneFluxTransferInfo pti = PipeApi.getRfTransferInfo(definition);
            tooltip.add(Component.translatable("buildcraft.fe.flow", pti.transferPerTick)
                .withStyle(ChatFormatting.GRAY));
        }
    }
}
