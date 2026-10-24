/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import buildcraftreborn.api.core.IFluidFilter;
import buildcraftreborn.api.core.IFluidHandlerAdv;

/** Fluid helpers. Only the parts the ported code uses so far are here; the rest of 1.12's {@code FluidUtilBC}
 * comes with the modules that need it. */
public final class FluidUtilBC {
    private FluidUtilBC() {}

    /** Pushes as much of the tank's fluid as possible into the neighbouring block entities' fluid handlers (on the
     * side facing this block). As in 1.12 only block entities are filled, not other blocks with a fluid
     * capability. */
    public static void pushFluidAround(Level world, BlockPos pos, IFluidHandler tank) {
        FluidStack potential = tank.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
        int drained = 0;
        if (potential.isEmpty()) {
            return;
        }
        FluidStack working = potential.copy();
        for (Direction side : Direction.values()) {
            if (potential.getAmount() <= 0) {
                break;
            }
            BlockPos target = pos.relative(side);
            if (world.getBlockEntity(target) == null) {
                continue;
            }
            IFluidHandler handler = world.getCapability(Capabilities.FluidHandler.BLOCK, target, side.getOpposite());
            if (handler != null) {
                int used = handler.fill(potential.copy(), FluidAction.EXECUTE);

                if (used > 0) {
                    drained += used;
                    potential.shrink(used);
                }
            }
        }
        if (drained > 0) {
            FluidStack actuallyDrained = tank.drain(drained, FluidAction.EXECUTE);
            if (actuallyDrained.getAmount() != drained) {
                throw new IllegalStateException("Bad tank! Could drain " + working + " but only drained "
                    + actuallyDrained + "( tank " + tank.getClass() + ")");
            }
        }
    }

    /** Merges every stack of the same fluid (and components) into one. */
    public static List<FluidStack> mergeSameFluids(List<FluidStack> fluids) {
        List<FluidStack> stacks = new ArrayList<>();
        fluids.forEach(toAdd -> {
            boolean found = false;
            for (FluidStack stack : stacks) {
                if (FluidStack.isSameFluidSameComponents(stack, toAdd)) {
                    stack.grow(toAdd.getAmount());
                    found = true;
                }
            }
            if (!found) {
                stacks.add(toAdd.copy());
            }
        });
        return stacks;
    }

    /** @return True if both fluids are the same (or both null). */
    public static boolean areFluidsEqual(@Nullable Fluid a, @Nullable Fluid b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.isSame(b);
    }

    /** @return The fluidstack that was moved, or an empty stack if no fluid was moved. */
    public static FluidStack move(@Nullable IFluidHandler from, @Nullable IFluidHandler to) {
        return move(from, to, Integer.MAX_VALUE);
    }

    /** @param max The maximum amount of fluid to move.
     * @return The fluidstack that was moved, or an empty stack if no fluid was moved. */
    public static FluidStack move(@Nullable IFluidHandler from, @Nullable IFluidHandler to, int max) {
        if (from == null || to == null) {
            return FluidStack.EMPTY;
        }
        FluidStack toDrainPotential;
        if (from instanceof IFluidHandlerAdv adv) {
            IFluidFilter filter = f -> to.fill(f.copy(), FluidAction.SIMULATE) > 0;
            toDrainPotential = adv.drain(filter, max, FluidAction.SIMULATE);
        } else {
            toDrainPotential = from.drain(max, FluidAction.SIMULATE);
        }
        if (toDrainPotential.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int accepted = to.fill(toDrainPotential.copy(), FluidAction.SIMULATE);
        if (accepted <= 0) {
            return FluidStack.EMPTY;
        }
        FluidStack toDrain = toDrainPotential.copyWithAmount(accepted);
        if (accepted < toDrainPotential.getAmount()) {
            toDrainPotential = from.drain(toDrain.copy(), FluidAction.SIMULATE);
            if (toDrainPotential.getAmount() < accepted) {
                return FluidStack.EMPTY;
            }
        }
        FluidStack drained = from.drain(toDrain.copy(), FluidAction.EXECUTE);
        if (drained.getAmount() != toDrain.getAmount() || !FluidStack.isSameFluidSameComponents(toDrain, drained)) {
            throw new IllegalStateException("Drained fluid did not equal expected fluid!\n(To Drain = " + toDrain
                + ",\npotential drain = " + toDrainPotential + ",\nactually drained = " + drained
                + ",\nIFluidHandler (from) = " + from.getClass() + "(" + from + ")"
                + ",\nIFluidHandler (to) = " + to.getClass() + "(" + to + "))");
        }
        int actuallyAccepted = to.fill(drained.copy(), FluidAction.EXECUTE);
        if (actuallyAccepted != accepted) {
            throw new IllegalStateException("Mismatched IFluidHandler implementations!\n(actually accepted = "
                + actuallyAccepted + ", accepted = " + accepted + ")");
        }
        return drained.copyWithAmount(accepted);
    }

    /** Handles a right-click on a block that holds fluid, with the item the player is holding. Buckets and other fluid
     * containers are emptied into the handler first; if nothing could be emptied, they are filled from it instead.
     * Creative players keep their held item unchanged.
     *
     * @return true if the held item is a fluid container. This is true on both sides and even when no fluid moved, so
     *         right-clicking with a bucket never falls through to opening the block's GUI (as in 1.12). */
    public static boolean onTankActivated(Player player, BlockPos pos, InteractionHand hand,
        IFluidHandler fluidHandler) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return false;
        }
        boolean replace = !player.getAbilities().instabuild;
        boolean single = held.getCount() == 1;
        // Creative players and stacked containers work on a single copy, so the held stack is never changed in place.
        ItemStack toUse = replace && single ? held : held.copyWithCount(1);
        IFluidHandlerItem flItem = FluidUtil.getFluidHandler(toUse).orElse(null);
        if (flItem == null) {
            return false;
        }
        Level level = player.level();
        if (level.isClientSide) {
            return true;
        }
        boolean changed = true;
        FluidStack moved;
        if (!(moved = move(flItem, fluidHandler)).isEmpty()) {
            playSound(level, pos, moved, SoundActions.BUCKET_EMPTY);
        } else if (!(moved = move(fluidHandler, flItem)).isEmpty()) {
            playSound(level, pos, moved, SoundActions.BUCKET_FILL);
        } else {
            changed = false;
        }

        if (changed && replace) {
            if (single) {
                // if it was the single item, replace with changed one
                player.setItemInHand(hand, flItem.getContainer());
            } else {
                // if it was part of stack, shrink stack and give / drop the new one
                held.shrink(1);
                ItemHandlerHelper.giveItemToPlayer(player, flItem.getContainer());
            }
            player.inventoryMenu.broadcastChanges();
        }
        return true;
    }

    private static void playSound(Level level, BlockPos pos, FluidStack fluid, SoundAction action) {
        SoundEvent sound = fluid.getFluidType().getSound(fluid, action);
        if (sound != null) {
            level.playSound(null, pos, sound, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }
}
