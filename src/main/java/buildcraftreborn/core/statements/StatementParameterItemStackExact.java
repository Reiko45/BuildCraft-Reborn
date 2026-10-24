/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.statements;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.StatementMouseClick;

/** An item parameter that keeps the count too (how many of the item a robot station requests). Clicking with the
 * same item, or with nothing, changes the count by one (by 16 with shift; right click counts down); clicking with a
 * different item takes that stack.
 * <p>
 * 1.21: immutable (each click returns a new parameter), and {@link #getItemStack()} returns the stack (1.12's
 * returned nothing, so robots never saw the request). */
public class StatementParameterItemStackExact implements IStatementParameter {
    protected final ItemStack stack;

    public StatementParameterItemStackExact() {
        this(ItemStack.EMPTY);
    }

    public StatementParameterItemStackExact(ItemStack stack) {
        this.stack = stack;
    }

    public StatementParameterItemStackExact(CompoundTag nbt, HolderLookup.Provider registries) {
        this(ItemStack.parseOptional(registries, nbt.getCompound("stack")));
    }

    @Override
    public ItemStack getItemStack() {
        return stack;
    }

    @Override
    public StatementParameterItemStackExact onClick(IStatementContainer source, IStatement stmt, ItemStack held,
        StatementMouseClick mouse) {
        int delta = (mouse.isShift() ? 16 : 1) * (mouse.getButton() == 0 ? 1 : -1);
        if (!held.isEmpty() && !ItemStack.isSameItemSameComponents(stack, held)) {
            return new StatementParameterItemStackExact(held.copy());
        }
        if (stack.isEmpty()) {
            return this;
        }
        int count = stack.getCount() + delta;
        if (count <= 0) {
            return new StatementParameterItemStackExact();
        }
        return new StatementParameterItemStackExact(stack.copyWithCount(Math.min(count, 64)));
    }

    @Override
    public void writeToNbt(CompoundTag compound, HolderLookup.Provider registries) {
        if (!stack.isEmpty()) {
            compound.put("stack", stack.save(registries));
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof StatementParameterItemStackExact param) {
            return ItemStack.matches(stack, param.stack);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return stack.isEmpty() ? 0 : Objects.hash(ItemStack.hashItemAndComponents(stack), stack.getCount());
    }

    @Override
    public String getDescription() {
        return stack.isEmpty() ? "" : stack.getHoverName().getString();
    }

    @Override
    public List<Component> getTooltip() {
        if (stack.isEmpty()) {
            return ImmutableList.of();
        }
        List<Component> tooltip = new java.util.ArrayList<>(stack.getTooltipLines(Item.TooltipContext.EMPTY, null,
            TooltipFlag.Default.NORMAL));
        if (!tooltip.isEmpty()) {
            tooltip.set(0, Component.literal(stack.getCount() + " x ").append(tooltip.get(0)));
        }
        for (int i = 1; i < tooltip.size(); i++) {
            tooltip.set(i, tooltip.get(i).copy().withStyle(ChatFormatting.GRAY));
        }
        return tooltip;
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:stackExact";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public ISprite getSprite() {
        // What's rendered is not a sprite but the actual stack itself
        return null;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        return null;
    }
}
