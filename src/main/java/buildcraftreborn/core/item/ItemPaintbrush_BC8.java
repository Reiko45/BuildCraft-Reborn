/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.core.item;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import buildcraftreborn.api.blocks.CustomPaintHelper;
import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.lib.item.IItemSubtypes;
import buildcraftreborn.lib.misc.ColourUtil;
import buildcraftreborn.lib.misc.SoundUtil;

/** The paintbrush: paints pipes (and stained glass, wool, terracotta...) with its colour, 64 times before it's clean
 * again. A clean brush takes the paint off.
 * <p>
 * 1.21: the colour is the {@code colour} component (1.12: the metadata) and the uses so far are {@code paint_used}
 * (1.12: the {@code damage} NBT byte). */
public class ItemPaintbrush_BC8 extends Item implements IItemSubtypes {
    public static final int MAX_USES = 64;

    public ItemPaintbrush_BC8(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void addSubtypes(Consumer<ItemStack> output) {
        output.accept(new ItemStack(this));
        for (DyeColor colour : DyeColor.values()) {
            output.accept(new Brush(colour).save(ItemStack.EMPTY));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Brush brush = new Brush(stack);
        Level level = context.getLevel();
        if (brush.useOnBlock(level, context, player)) {
            ItemStack newStack = brush.save(stack);
            if (!newStack.isEmpty() && player != null) {
                player.setItemInHand(context.getHand(), newStack);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public Component getName(ItemStack stack) {
        Brush brush = new Brush(stack);
        if (brush.colour == null) {
            return super.getName(stack);
        }
        return ColourUtil.getColouredName(brush.colour, Component.empty(), super.getName(stack));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        Brush brush = new Brush(stack);
        return brush.colour != null && brush.usesLeft < MAX_USES;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * new Brush(stack).usesLeft / MAX_USES);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, new Brush(stack).usesLeft / (float) MAX_USES);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    /** The brush in a stack. */
    public class Brush {
        @Nullable
        public DyeColor colour;
        public int usesLeft;

        public Brush(@Nullable DyeColor colour) {
            this.colour = colour;
            usesLeft = colour == null ? 0 : MAX_USES;
        }

        public Brush(ItemStack stack) {
            colour = stack.get(BCItemComponents.COLOUR.get());
            if (colour != null) {
                usesLeft = MAX_USES - stack.getOrDefault(BCItemComponents.PAINT_USED.get(), 0);
                if (usesLeft <= 0) {
                    colour = null;
                    usesLeft = 0;
                }
            } else {
                usesLeft = 0;
            }
        }

        /** @return A new stack if the old one couldn't be changed (or was empty), otherwise
         *         {@link ItemStack#EMPTY}. */
        public ItemStack save(ItemStack existing) {
            ItemStack stack = existing.isEmpty() ? new ItemStack(ItemPaintbrush_BC8.this) : existing;
            if (colour == null || usesLeft <= 0) {
                stack.remove(BCItemComponents.COLOUR.get());
                stack.remove(BCItemComponents.PAINT_USED.get());
            } else {
                stack.set(BCItemComponents.COLOUR.get(), colour);
                if (usesLeft == MAX_USES) {
                    stack.remove(BCItemComponents.PAINT_USED.get());
                } else {
                    stack.set(BCItemComponents.PAINT_USED.get(), MAX_USES - usesLeft);
                }
            }
            return stack == existing ? ItemStack.EMPTY : stack;
        }

        public boolean useOnBlock(Level level, UseOnContext context, @Nullable Player player) {
            if (colour != null && usesLeft <= 0) {
                return false;
            }
            InteractionResult result = CustomPaintHelper.INSTANCE.attemptPaintBlock(level, context.getClickedPos(),
                level.getBlockState(context.getClickedPos()), context.getClickLocation(), context.getClickedFace(),
                colour);
            if (result == InteractionResult.SUCCESS) {
                if (!level.isClientSide) {
                    SoundUtil.playChangeColour(level, context.getClickedPos(), colour);
                }
                if (player == null || !player.getAbilities().instabuild) {
                    usesLeft--;
                }
                if (usesLeft <= 0) {
                    colour = null;
                    usesLeft = 0;
                }
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "[" + usesLeft + " of " + (colour == null ? "nothing" : colour.getName()) + "]";
        }
    }
}
