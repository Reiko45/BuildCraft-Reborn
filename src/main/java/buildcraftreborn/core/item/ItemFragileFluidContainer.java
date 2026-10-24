/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.item;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import buildcraftreborn.api.items.IItemFluidShard;
import buildcraftreborn.lib.fluid.BCFluidType;
import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.lib.misc.LocaleUtil;

/** The fragile fluid shard: what a broken tank (or, later, a fluid pipe) leaves behind. It holds up to half a bucket,
 * can be emptied but not filled, and is used up when empty. Not in the creative tab (there would be one per fluid). */
public class ItemFragileFluidContainer extends Item implements IItemFluidShard {

    // Half of a bucket
    public static final int MAX_FLUID_HELD = 500;

    public ItemFragileFluidContainer(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        FluidStack fluid = getFluid(stack);
        if (fluid.isEmpty()) {
            return Component.translatable(getDescriptionId(stack), "ERROR! NULL FLUID!");
        }
        if (fluid.getFluidType() instanceof BCFluidType bcType && (bcType.fluid.heatable || bcType.fluid.heat > 0)) {
            // Add the heatable bit to the end of the name
            Component bare = Component.translatable(bcType.getDescriptionId());
            Component whole = Component.translatable(getDescriptionId(stack), bare);
            return Component.translatable("buildcraft.fluid.heat_" + bcType.fluid.heat, whole);
        }
        return Component.translatable(getDescriptionId(stack), fluid.getHoverName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        FluidStack fluid = getFluid(stack);
        if (!fluid.isEmpty()) {
            tooltip.add(LocaleUtil.localizeFluidStaticAmount(fluid.getAmount(), MAX_FLUID_HELD));
        }
    }

    @Override
    public void addFluidDrops(NonNullList<ItemStack> toDrop, FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) {
            return;
        }
        int amount = fluid.getAmount();
        while (amount >= MAX_FLUID_HELD) {
            ItemStack stack = new ItemStack(this);
            setFluid(stack, fluid.copyWithAmount(MAX_FLUID_HELD));
            amount -= MAX_FLUID_HELD;
            toDrop.add(stack);
        }
        if (amount > 0) {
            ItemStack stack = new ItemStack(this);
            setFluid(stack, fluid.copyWithAmount(amount));
            toDrop.add(stack);
        }
    }

    static void setFluid(ItemStack container, FluidStack fluid) {
        container.set(BCItemComponents.FLUID.get(), SimpleFluidContent.copyOf(fluid));
    }

    static FluidStack getFluid(ItemStack container) {
        if (container.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return container.getOrDefault(BCItemComponents.FLUID.get(), SimpleFluidContent.EMPTY).copy();
    }

    /** The shard's fluid handler: drain only, and the shard is used up once it's empty. */
    public static class FragileFluidHandler implements IFluidHandlerItem {
        private ItemStack container;

        public FragileFluidHandler(ItemStack container) {
            this.container = container;
        }

        @Override
        public ItemStack getContainer() {
            return container;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return getFluid(container);
        }

        @Override
        public int getTankCapacity(int tank) {
            return MAX_FLUID_HELD;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack fluid = getFluid(container);
            if (fluid.isEmpty() || resource.isEmpty() || !FluidStack.isSameFluidSameComponents(fluid, resource)) {
                return FluidStack.EMPTY;
            }
            return drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack fluid = getFluid(container);
            if (fluid.isEmpty() || maxDrain <= 0) {
                return FluidStack.EMPTY;
            }
            int toDrain = Math.min(maxDrain, fluid.getAmount());
            FluidStack drained = fluid.copyWithAmount(toDrain);
            if (action.execute()) {
                fluid.shrink(toDrain);
                if (fluid.isEmpty()) {
                    container = ItemStack.EMPTY;
                } else {
                    setFluid(container, fluid);
                }
            }
            return drained;
        }
    }
}
