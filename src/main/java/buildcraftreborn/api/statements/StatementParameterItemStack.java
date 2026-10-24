/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.statements;

import java.util.List;


import com.google.common.collect.ImmutableList;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;


import buildcraftreborn.api.core.render.ISprite;

public class StatementParameterItemStack implements IStatementParameter {
    private static final ItemStack EMPTY_STACK;

    /** Immutable parameter that has the {@link ItemStack#EMPTY} as it's {@link #stack}. */
    public static final StatementParameterItemStack EMPTY;

    static {
        ItemStack stack = ItemStack.EMPTY;
        if (stack == null) throw new Error("Somehow ItemStack.EMPTY was null!");
        EMPTY_STACK = stack;
        EMPTY = new StatementParameterItemStack();
    }

    protected final ItemStack stack;

    public StatementParameterItemStack() {
        stack = EMPTY_STACK;
    }

    public StatementParameterItemStack(ItemStack stack) {
        this.stack = stack;
    }

    public StatementParameterItemStack(CompoundTag nbt, HolderLookup.Provider registries) {
        ItemStack read = ItemStack.parseOptional(registries, nbt.getCompound("stack"));
        if (read.isEmpty()) {
            stack = EMPTY_STACK;
        } else {
            stack = read;
        }
    }

    @Override
    public void writeToNbt(CompoundTag compound, HolderLookup.Provider registries) {
        if (!stack.isEmpty()) {
            compound.put("stack", stack.save(registries));
        }
    }

    @Override
    public ISprite getSprite() {
        return null;
    }

    @Override
    public ItemStack getItemStack() {
        return stack;
    }

    @Override
    public StatementParameterItemStack onClick(
        IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse
    ) {
        if (stack.isEmpty()) {
            return EMPTY;
        } else {
            ItemStack newStack = stack.copy();
            newStack.setCount(1);
            return new StatementParameterItemStack(newStack);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof StatementParameterItemStack) {
            StatementParameterItemStack param = (StatementParameterItemStack) object;

            return ItemStack.matches(stack, param.stack);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return stack.isEmpty() ? 0 : ItemStack.hashItemAndComponents(stack) * 31 + stack.getCount();
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Don't call getDescription directly!");
    }

    @Override
    public List<Component> getTooltip() {
        if (stack.isEmpty()) {
            return ImmutableList.of();
        }
        List<Component> tooltip = new java.util.ArrayList<>(stack.getTooltipLines(Item.TooltipContext.EMPTY, null,
            TooltipFlag.Default.NORMAL));
        for (int i = 1; i < tooltip.size(); i++) {
            tooltip.set(i, tooltip.get(i).copy().withStyle(ChatFormatting.GRAY));
        }
        return tooltip;
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:stack";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        return null;
    }
}
