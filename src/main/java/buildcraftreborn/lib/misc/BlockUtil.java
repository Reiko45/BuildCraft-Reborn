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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.lib.BCLibConfig;
import buildcraftreborn.lib.inventory.TransactorEntityItem;
import buildcraftreborn.lib.inventory.filter.StackFilter;

/** Breaking blocks as a machine's owner, and reading fluid blocks.
 * <p>
 * 1.21 notes: a "fluid block" here is a {@link LiquidBlock} (vanilla water and lava, and every BuildCraft fluid), as
 * 1.12's were {@code BlockLiquid} and Forge fluid blocks; waterlogged blocks don't count. Fluid blocks have no fluid
 * capability in NeoForge, so {@link #drainBlock} picks them up directly. Only the methods used so far are ported. */
public final class BlockUtil {

    private BlockUtil() {}

    public static boolean harvestBlock(ServerLevel world, BlockPos pos, ItemStack tool, GameProfile owner) {
        FakePlayer fakePlayer = getFakePlayerWithTool(world, tool, owner);
        BlockState state = world.getBlockState(pos);
        BlockEvent.BreakEvent breakEvent =
            CommonHooks.fireBlockBreak(world, GameType.SURVIVAL, fakePlayer, pos, state);

        if (breakEvent.isCanceled()) {
            return false;
        }

        if (!state.canHarvestBlock(world, pos, fakePlayer)) {
            return false;
        }

        BlockEntity tile = world.getBlockEntity(pos);
        BlockState destroyed = state.getBlock().playerWillDestroy(world, pos, state, fakePlayer);
        state.getBlock().playerDestroy(world, fakePlayer, pos, destroyed, tile, tool);
        // Don't drop items as we do that ourselves
        removeBlock(world, pos, state, false);

        return true;
    }

    public static boolean destroyBlock(ServerLevel world, BlockPos pos, ItemStack tool, GameProfile owner) {
        FakePlayer fakePlayer = getFakePlayerWithTool(world, tool, owner);
        BlockEvent.BreakEvent breakEvent =
            CommonHooks.fireBlockBreak(world, GameType.SURVIVAL, fakePlayer, pos, world.getBlockState(pos));

        if (breakEvent.isCanceled()) {
            return false;
        }

        removeBlock(world, pos, world.getBlockState(pos), true);

        return true;
    }

    /** {@link Level#destroyBlock} leaves a block's fluid behind (for waterlogged blocks), which would leave a fluid
     * block in place forever. As in 1.12, a fluid block is replaced with air instead. */
    private static void removeBlock(ServerLevel world, BlockPos pos, BlockState state, boolean drop) {
        if (state.getBlock() instanceof LiquidBlock) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        } else {
            world.destroyBlock(pos, drop);
        }
    }

    public static FakePlayer getFakePlayerWithTool(ServerLevel world, ItemStack tool, GameProfile owner) {
        FakePlayer player = FakePlayerProvider.INSTANCE.getFakePlayer(world, owner);
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        return player;
    }

    public static Optional<List<ItemStack>> breakBlockAndGetDrops(ServerLevel world, BlockPos pos, ItemStack tool,
        GameProfile owner) {
        return breakBlockAndGetDrops(world, pos, tool, owner, false);
    }

    /** @param grabAll If true then this will pickup every item in range of the position, false to only get the items
     *            that the dropped while breaking the block. */
    public static Optional<List<ItemStack>> breakBlockAndGetDrops(ServerLevel world, BlockPos pos, ItemStack tool,
        GameProfile owner, boolean grabAll) {
        AABB aabb = new AABB(pos).inflate(1);
        Set<ItemEntity> entities;
        if (grabAll) {
            entities = Collections.emptySet();
        } else {
            entities = Sets.newIdentityHashSet();
            entities.addAll(world.getEntitiesOfClass(ItemEntity.class, aabb));
        }
        if (!harvestBlock(world, pos, tool, owner)) {
            if (!destroyBlock(world, pos, tool, owner)) {
                return Optional.empty();
            }
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemEntity entity : world.getEntitiesOfClass(ItemEntity.class, aabb)) {
            if (entities.contains(entity)) {
                continue;
            }
            TransactorEntityItem transactor = new TransactorEntityItem(entity);
            ItemStack stack;
            while (!(stack = transactor.extract(StackFilter.ALL, 0, Integer.MAX_VALUE, false)).isEmpty()) {
                stacks.add(stack);
            }
        }
        return Optional.of(stacks);
    }

    public static boolean canChangeBlock(Level world, BlockPos pos, GameProfile owner) {
        return canChangeBlock(world.getBlockState(pos), world, pos, owner);
    }

    public static boolean canChangeBlock(@Nullable BlockState state, Level world, BlockPos pos, GameProfile owner) {
        if (state == null) return true;

        if (state.isAir()) {
            return true;
        }

        if (isUnbreakableBlock(world, pos, state, owner)) {
            return false;
        }

        Block block = state.getBlock();
        if (block == Blocks.LAVA) {
            return false;
        } else if (block instanceof LiquidBlock) {
            FluidState fluid = state.getFluidState();
            if (!fluid.isEmpty() && fluid.getFluidType().getDensity(fluid, world, pos) >= 3000) {
                return false;
            }
        }

        return true;
    }

    public static float getBlockHardnessMining(Level world, BlockPos pos, BlockState state, GameProfile owner) {
        if (world instanceof ServerLevel server) {
            FakePlayer fakePlayer = FakePlayerProvider.INSTANCE.getFakePlayer(server, owner);
            float relativeHardness = state.getDestroyProgress(fakePlayer, world, pos);
            if (relativeHardness <= 0.0F) {
                return -1.0F;
            }
        }
        return state.getDestroySpeed(world, pos);
    }

    public static boolean isUnbreakableBlock(Level world, BlockPos pos, BlockState state, GameProfile owner) {
        return getBlockHardnessMining(world, pos, state, owner) < 0;
    }

    public static boolean isUnbreakableBlock(Level world, BlockPos pos, GameProfile owner) {
        return isUnbreakableBlock(world, pos, world.getBlockState(pos), owner);
    }

    /** Returns true if a block cannot be harvested without a tool. */
    public static boolean isToughBlock(Level world, BlockPos pos) {
        return world.getBlockState(pos).requiresCorrectToolForDrops();
    }

    public static boolean isFullFluidBlock(Level world, BlockPos pos) {
        return isFullFluidBlock(world.getBlockState(pos), world, pos);
    }

    public static boolean isFullFluidBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return state.getBlock() instanceof LiquidBlock && state.getFluidState().isSource();
    }

    /** @return The fluid that {@link #drainBlock} could take from this position, or null. */
    @Nullable
    public static Fluid getFluid(Level world, BlockPos pos) {
        FluidStack fluid = drainBlock(world, pos, false);
        return fluid.isEmpty() ? null : fluid.getFluid();
    }

    /** @return The (source) fluid of the fluid block at this position, whether it is a source or flowing, or null. */
    @Nullable
    public static Fluid getFluidWithFlowing(Level world, BlockPos pos) {
        return getFluidWithFlowing(world.getBlockState(pos));
    }

    @Nullable
    public static Fluid getFluidWithFlowing(BlockState state) {
        if (!(state.getBlock() instanceof LiquidBlock)) {
            return null;
        }
        FluidState fluid = state.getFluidState();
        if (fluid.isEmpty()) {
            return null;
        }
        return asSource(fluid.getType());
    }

    /** @return The fluid if this is a fluid source block, or null otherwise. */
    @Nullable
    public static Fluid getFluidWithoutFlowing(BlockState state) {
        if (!(state.getBlock() instanceof LiquidBlock)) {
            return null;
        }
        FluidState fluid = state.getFluidState();
        if (fluid.isEmpty() || !fluid.isSource()) {
            return null;
        }
        return asSource(fluid.getType());
    }

    private static Fluid asSource(Fluid fluid) {
        return fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid;
    }

    /** Drains a bucket of fluid from the block at the given position: from its fluid capability if it has one, or
     * by picking up a fluid source block.
     *
     * @return The fluid drained (or that would be drained, if simulating), or {@link FluidStack#EMPTY}. */
    public static FluidStack drainBlock(Level world, BlockPos pos, boolean doDrain) {
        IFluidHandler handler = world.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
        if (handler != null) {
            return handler.drain(1000, doDrain ? FluidAction.EXECUTE : FluidAction.SIMULATE);
        }
        BlockState state = world.getBlockState(pos);
        Fluid source = getFluidWithoutFlowing(state);
        if (source == null || !(state.getBlock() instanceof LiquidBlock liquid)) {
            return FluidStack.EMPTY;
        }
        if (doDrain) {
            liquid.pickupBlock(null, world, pos, state);
        }
        return new FluidStack(source, 1000);
    }

    public static long computeBlockBreakPower(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        float hardness = state.getDestroySpeed(world, pos);
        return (long) Math.floor(16 * MjAPI.MJ * ((hardness + 1) * 2) * BCLibConfig.miningMultiplier());
    }

    // Block state helpers used by the builders' schematics

    public static <T extends Comparable<T>> BlockState copyProperty(net.minecraft.world.level.block.state.properties.Property<T> property,
        BlockState dst, BlockState src) {
        return dst.hasProperty(property) && src.hasProperty(property) ? dst.setValue(property, src.getValue(property)) : dst;
    }

    public static <T extends Comparable<T>> String getPropertyStringValue(BlockState blockState,
        net.minecraft.world.level.block.state.properties.Property<T> property) {
        return property.getName(blockState.getValue(property));
    }

    public static boolean blockStatesWithoutBlockEqual(BlockState a, BlockState b,
        java.util.Collection<net.minecraft.world.level.block.state.properties.Property<?>> ignoredProperties) {
        for (net.minecraft.world.level.block.state.properties.Property<?> property : a.getProperties()) {
            if (b.hasProperty(property) && !ignoredProperties.contains(property)
                && !java.util.Objects.equals(a.getValue(property), b.getValue(property))) {
                return false;
            }
        }
        return true;
    }

    public static boolean blockStatesWithoutBlockEqual(BlockState a, BlockState b) {
        return blockStatesWithoutBlockEqual(a, b, java.util.List.of());
    }

    public static boolean blockStatesEqual(BlockState a, BlockState b,
        java.util.Collection<net.minecraft.world.level.block.state.properties.Property<?>> ignoredProperties) {
        return a.getBlock() == b.getBlock() && blockStatesWithoutBlockEqual(a, b, ignoredProperties);
    }

    public static boolean blockStatesEqual(BlockState a, BlockState b) {
        return blockStatesEqual(a, b, java.util.List.of());
    }

    /** @return A comparator that falls back to comparing the coordinates (x, then y, then z) when the parent says two
     *         positions are equal, so that a sorted set never drops a position. */
    public static Comparator<BlockPos> uniqueBlockPosComparator(Comparator<BlockPos> parent) {
        return (a, b) -> {
            int parentValue = parent.compare(a, b);
            if (parentValue != 0) {
                return parentValue;
            } else if (a.getX() != b.getX()) {
                return Integer.compare(a.getX(), b.getX());
            } else if (a.getY() != b.getY()) {
                return Integer.compare(a.getY(), b.getY());
            } else {
                return Integer.compare(a.getZ(), b.getZ());
            }
        };
    }
}
