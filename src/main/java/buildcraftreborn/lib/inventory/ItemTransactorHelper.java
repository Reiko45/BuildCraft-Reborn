/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.inventory;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.inventory.IItemTransactor;
import buildcraftreborn.api.transport.IInjectable;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.inventory.IItemTransactor.IItemInsertable;
import buildcraftreborn.lib.misc.CapUtil;
import buildcraftreborn.lib.misc.InventoryUtil;
import buildcraftreborn.lib.misc.StackUtil;

/** Finds and moves items between {@link IItemTransactor}s.
 * <p>
 * 1.21: blocks are looked up through the level's capability cache ({@code level.getCapability}) rather than asking a
 * tile entity; vanilla containers already expose NeoForge item handlers, so 1.12's {@code IInventory} wrappers aren't
 * needed. Pipes accept items through the {@code PipeApi.CAP_INJECTABLE} capability. */
public class ItemTransactorHelper {
    /** @param face The side of the block being accessed (from the block's point of view), or null. */
    public static IItemTransactor getTransactor(Level world, BlockPos pos, @Nullable Direction face) {
        IItemTransactor trans = world.getCapability(CapUtil.CAP_ITEM_TRANSACTOR, pos, face);
        if (trans != null) {
            return trans;
        }

        IItemHandler handler = world.getCapability(Capabilities.ItemHandler.BLOCK, pos, face);
        if (handler == null) {
            return NoSpaceTransactor.INSTANCE;
        }
        if (handler instanceof IItemTransactor transactor) {
            return transactor;
        }
        return new ItemHandlerWrapper(handler);
    }

    /** @param face The side of the tile being accessed (from the tile's point of view), or null.
     * @return The transactor for the tile's position, or {@link NoSpaceTransactor#INSTANCE} for a null tile. */
    public static IItemTransactor getTransactor(@Nullable BlockEntity tile, @Nullable Direction face) {
        if (tile == null || tile.getLevel() == null) {
            return NoSpaceTransactor.INSTANCE;
        }
        return getTransactor(tile.getLevel(), tile.getBlockPos(), face);
    }

    /** @return Something that accepts items pushed by a pipe (another pipe's flow, for example), or
     *         {@link NoSpaceInjectable#INSTANCE}. */
    public static IInjectable getInjectable(@Nullable BlockEntity tile, @Nullable Direction face) {
        if (tile == null || tile.getLevel() == null) {
            return NoSpaceInjectable.INSTANCE;
        }
        IInjectable injectable = tile.getLevel().getCapability(PipeApi.CAP_INJECTABLE, tile.getBlockPos(), face);
        return injectable == null ? NoSpaceInjectable.INSTANCE : injectable;
    }

    public static IItemTransactor wrapInjectable(IInjectable injectable, @Nullable Direction facing) {
        return new InjectableWrapper(injectable, facing);
    }

    /** @return The entity's automation item handler (chest minecarts and boats, for example), or
     *         {@link NoSpaceTransactor#INSTANCE} if it has none. */
    public static IItemTransactor getTransactor(Entity entity, @Nullable Direction face) {
        IItemHandler handler = entity.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, face);
        if (handler == null) {
            return NoSpaceTransactor.INSTANCE;
        }
        return handler instanceof IItemTransactor transactor ? transactor : new ItemHandlerWrapper(handler);
    }

    public static IItemTransactor getTransactorForEntity(Entity entity, @Nullable Direction face) {
        IItemTransactor transactor = getTransactor(entity, face);
        if (transactor != NoSpaceTransactor.INSTANCE) {
            return transactor;
        } else if (entity instanceof ItemEntity item) {
            return new TransactorEntityItem(item);
        } else {
            return NoSpaceTransactor.INSTANCE;
        }
    }

    /** Provides an implementation of {@link IItemTransactor#insert(NonNullList, boolean)} that relies on
     * {@link IItemTransactor#insert(ItemStack, boolean, boolean)}. This is the least efficient, default
     * implementation. */
    public static NonNullList<ItemStack> insertAllBypass(IItemTransactor transactor, NonNullList<ItemStack> stacks,
        boolean simulate) {
        NonNullList<ItemStack> leftOver = NonNullList.create();
        for (ItemStack stack : stacks) {
            ItemStack leftOverStack = transactor.insert(stack, false, simulate);
            if (!leftOverStack.isEmpty()) {
                leftOver.add(leftOverStack);
            }
        }
        return leftOver;
    }

    /** Attempts to move as many items as possible from the source {@link IItemTransactor} to the destination.
     *
     * @return The number of items moved. */
    public static int move(IItemTransactor src, IItemTransactor dst) {
        return move(src, dst, Integer.MAX_VALUE);
    }

    /** Attempts to move up to maxItems from the source {@link IItemTransactor} to the destination.
     *
     * @param maxItems The maximum number of items to move.
     * @return The number of items moved. */
    public static int move(IItemTransactor src, IItemTransactor dst, int maxItems) {
        return move(src, dst, null, maxItems);
    }

    /** Attempts to move up to maxItems from the source {@link IItemTransactor} to the destination.
     *
     * @return The number of items moved. */
    public static int move(IItemTransactor src, IItemTransactor dst, @Nullable IStackFilter filter) {
        return move(src, dst, filter, Integer.MAX_VALUE);
    }

    /** Attempts to move up to maxItems from the source {@link IItemTransactor} to the destination.
     *
     * @param filter The stack filter to use - only items that match this filter will be moved.
     * @param maxItems The maximum number of items to move.
     * @return The number of items moved. */
    public static int move(IItemTransactor src, IItemTransactor dst, @Nullable IStackFilter filter, int maxItems) {
        int moved = 0;
        IStackFilter rFilter = dst::canPartiallyAccept;
        if (filter != null) {
            rFilter = rFilter.and(filter);
        }
        while (true) {
            int m = moveSingle0(src, dst, rFilter, maxItems - moved, false, false);
            if (m == 0) {
                break;
            } else {
                moved += m;
            }
        }
        return moved;
    }

    public static int moveSingle(IItemTransactor src, IItemTransactor dst, @Nullable IStackFilter filter,
        boolean simulateSrc, boolean simulateDst) {
        return moveSingle(src, dst, filter, Integer.MAX_VALUE, simulateSrc, simulateDst);
    }

    /** Similar to {@link #move(IItemTransactor, IItemTransactor, IStackFilter, int)}, but will only attempt to extract
     * and insert once, which means that you can simulate the move safely. */
    public static int moveSingle(IItemTransactor src, IItemTransactor dst, @Nullable IStackFilter filter,
        int maxItems, boolean simulateSrc, boolean simulateDst) {
        IStackFilter rFilter = dst::canPartiallyAccept;
        if (filter != null) {
            rFilter = rFilter.and(filter);
        }
        return moveSingle0(src, dst, rFilter, maxItems, simulateSrc, simulateDst);
    }

    private static int moveSingle0(IItemTransactor src, IItemTransactor dst, IStackFilter filter, int maxItems,
        boolean simulateSrc, boolean simulateDst) {
        if (maxItems <= 0) return 0;
        ItemStack potential = src.extract(filter, 1, maxItems, true);
        if (potential.isEmpty()) return 0;
        ItemStack leftOver = dst.insert(potential, false, simulateDst);
        int toTake = potential.getCount() - leftOver.getCount();
        if (toTake <= 0) return 0;
        IStackFilter exactFilter = (stack) -> StackUtil.canMerge(stack, potential);
        ItemStack taken = src.extract(exactFilter, toTake, toTake, simulateSrc);
        if (taken.getCount() != toTake) {
            String msg = "One of the two transactors (either src = ";
            msg += src.getClass() + " or dst = " + dst.getClass() + ")";
            msg += " didn't respect the movement flags! ( potential = " + potential;
            msg += ", leftOver = " + leftOver + ", taken = " + taken;
            msg += ", count = " + toTake + " )";
            throw new IllegalStateException(msg);
        }
        return toTake;
    }

    public static IItemInsertable createDroppingTransactor(Level world, Vec3 vec) {
        return (stack, allorNone, simulate) -> {
            if (!simulate) {
                InventoryUtil.drop(world, vec, stack);
            }
            return ItemStack.EMPTY;
        };
    }
}
