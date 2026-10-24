/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import buildcraftreborn.api.inventory.IItemTransactor;
import buildcraftreborn.lib.inventory.ItemTransactorHelper;

public class InventoryUtil {
    // Drops

    public static void dropAll(Level world, Vec3 vec, IItemHandlerModifiable handler) {
        dropAll(world, vec.x, vec.y, vec.z, handler);
    }

    public static void dropAll(Level world, BlockPos pos, IItemHandlerModifiable handler) {
        dropAll(world, pos.getX(), pos.getY(), pos.getZ(), handler);
    }

    public static void dropAll(Level world, double x, double y, double z, IItemHandlerModifiable handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            drop(world, x, y, z, handler.extractItem(i, Integer.MAX_VALUE, false));
        }
    }

    public static void dropAll(Level world, BlockPos pos, NonNullList<ItemStack> toDrop) {
        for (ItemStack stack : toDrop) {
            drop(world, pos, stack);
        }
    }

    public static void drop(Level world, BlockPos pos, ItemStack stack) {
        Block.popResource(world, pos, stack);
    }

    public static void drop(Level world, Vec3 vec, ItemStack stack) {
        drop(world, vec.x, vec.y, vec.z, stack);
    }

    public static void drop(Level world, double x, double y, double z, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemEntity entity = new ItemEntity(world, x, y, z, stack);
        world.addFreshEntity(entity);
    }

    // Sending items around

    /** @return The leftover stack */
    public static ItemStack addToRandomInventory(Level world, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        List<Direction> toTry = new ArrayList<>(6);
        Collections.addAll(toTry, Direction.values());
        Collections.shuffle(toTry);
        for (Direction face : toTry) {
            IItemTransactor transactor =
                ItemTransactorHelper.getTransactor(world, pos.relative(face), face.getOpposite());
            stack = transactor.insert(stack, false, false);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    /** Look around the tile given in parameter in all 6 position, tries to add the items to a random injectable tile
     * around (a pipe). Will make sure that the location from which the items are coming from (identified by the
     * {@code ignore} parameter) isn't used again so that entities doesn't go backwards.
     * <p>
     * Pipes arrive with the transport module, so for now nothing is injectable and the stack is returned as-is.
     *
     * @return The leftover stack. */
    public static ItemStack addToRandomInjectable(Level world, BlockPos pos, @Nullable Direction ignore,
        ItemStack stack) {
        return stack;
    }

    /** Attempts to add the given stack to the best acceptor, in this order: pipes, inventories (item handlers), and
     * finally dropping it down on the ground. */
    public static void addToBestAcceptor(Level world, BlockPos pos, @Nullable Direction ignore, ItemStack stack) {
        stack = addToRandomInjectable(world, pos, ignore, stack);
        stack = addToRandomInventory(world, pos, stack);
        drop(world, pos, stack);
    }

    /** Adds every stack from src to dst. Doesn't add empty stacks. */
    public static void addAll(IItemHandler src, NonNullList<ItemStack> dst) {
        for (int i = 0; i < src.getSlots(); i++) {
            ItemStack stack = src.getStackInSlot(i);
            if (!stack.isEmpty()) {
                dst.add(stack);
            }
        }
    }

    /** Adds the given {@link ItemStack} to the player's inventory, or drops it in front of them if their was not
     * enough room. */
    public static void addToPlayer(Player player, ItemStack stack) {
        if (player.getInventory().add(stack)) {
            player.inventoryMenu.broadcastChanges();
        } else {
            player.drop(stack, false, false);
        }
    }
}
