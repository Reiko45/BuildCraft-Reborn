/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.tile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.block.BlockFloodGate;
import buildcraftreborn.lib.fluid.Tank;
import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.misc.BlockUtil;
import buildcraftreborn.lib.misc.FakePlayerProvider;
import buildcraftreborn.lib.misc.FluidUtilBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** Places fluid from its tank into the world, one bucket at a time, spreading out through its open sides (all but the
 * top, until a wrench closes them).
 * <p>
 * 1.21: which sides are open is stored in the block state ({@code connected_<side>}) rather than in the tile and
 * worked out when drawing. 1.12 checked the wrong block when testing whether the fluid could still reach a target,
 * so it only filled spots that its fluid had already flowed into; the whole path is checked here. */
public class TileFloodGate extends TileBC_Neptune implements ITickable, IDebuggable {
    private static final Direction[] SEARCH_NORMAL = new Direction[] {
        Direction.DOWN, Direction.NORTH, Direction.SOUTH,
        Direction.WEST, Direction.EAST
    };

    private static final Direction[] SEARCH_GASEOUS = new Direction[] {
        Direction.UP, Direction.NORTH, Direction.SOUTH,
        Direction.WEST, Direction.EAST
    };

    private static final ResourceLocation ADVANCEMENT_FLOOD_SINGLE = BuildCraftReborn.id("flooding_the_world");

    private static final int[] REBUILD_DELAYS = { 16, 32, 64, 128, 256 };

    private final Tank tank = new Tank("tank", 2 * 1000, this);

    public final Deque<BlockPos> queue = new ArrayDeque<>();
    private final Map<BlockPos, List<BlockPos>> paths = new HashMap<>();

    private int delayIndex = 0;
    private int tick = 0;

    public TileFloodGate(BlockPos pos, BlockState state) {
        super(BCFactoryBlocks.FLOOD_GATE_TILE.get(), pos, state);
        caps.addCapabilityInstance(Capabilities.FluidHandler.BLOCK, tank, EnumPipePart.VALUES);
        tankManager.add(tank);
    }

    /** @return The sides fluid can leave through (never {@link Direction#UP}). */
    public Set<Direction> getOpenSides() {
        Set<Direction> open = EnumSet.noneOf(Direction.class);
        BlockState state = getBlockState();
        for (Map.Entry<Direction, net.minecraft.world.level.block.state.properties.BooleanProperty> entry
            : BlockFloodGate.CONNECTED_MAP.entrySet()) {
            if (state.hasProperty(entry.getValue()) && state.getValue(entry.getValue())) {
                open.add(entry.getKey());
            }
        }
        return open;
    }

    private int getCurrentDelay() {
        return REBUILD_DELAYS[delayIndex];
    }

    private void buildQueue() {
        queue.clear();
        paths.clear();
        FluidStack fluid = tank.getFluid();
        if (fluid.isEmpty()) {
            return;
        }
        Set<BlockPos> checked = new HashSet<>();
        checked.add(worldPosition);
        List<BlockPos> nextPosesToCheck = new ArrayList<>();
        for (Direction face : getOpenSides()) {
            BlockPos offset = worldPosition.relative(face);
            nextPosesToCheck.add(offset);
            paths.put(offset, ImmutableList.of(offset));
        }
        Direction[] directions = fluid.getFluid().getFluidType().isLighterThanAir() ? SEARCH_GASEOUS : SEARCH_NORMAL;
        outer: while (!nextPosesToCheck.isEmpty()) {
            List<BlockPos> nextPosesToCheckCopy = new ArrayList<>(nextPosesToCheck);
            nextPosesToCheck.clear();
            for (BlockPos toCheck : nextPosesToCheckCopy) {
                if (toCheck.distSqr(worldPosition) > 64 * 64) {
                    continue;
                }
                if (checked.add(toCheck)) {
                    if (canSearch(toCheck)) {
                        if (canFill(toCheck)) {
                            queue.push(toCheck);
                            if (queue.size() >= 4096) {
                                break outer;
                            }
                        }
                        List<BlockPos> checkPath = paths.get(toCheck);
                        for (Direction side : directions) {
                            BlockPos next = toCheck.relative(side);
                            if (checked.contains(next)) {
                                continue;
                            }
                            ImmutableList.Builder<BlockPos> pathBuilder = ImmutableList.builder();
                            pathBuilder.addAll(checkPath);
                            pathBuilder.add(next);
                            paths.put(next, pathBuilder.build());
                            nextPosesToCheck.add(next);
                        }
                    }
                }
            }
        }
    }

    @Nullable
    private Fluid getTankFluid() {
        FluidStack fluid = tank.getFluid();
        return fluid.isEmpty() ? null : fluid.getFluid();
    }

    private boolean canFill(BlockPos offsetPos) {
        if (level.isEmptyBlock(offsetPos)) {
            return true;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(level, offsetPos);
        return fluid != null && FluidUtilBC.areFluidsEqual(fluid, getTankFluid())
            && BlockUtil.getFluidWithoutFlowing(getLocalState(offsetPos)) == null;
    }

    private boolean canSearch(BlockPos offsetPos) {
        if (canFill(offsetPos)) {
            return true;
        }
        Fluid fluid = BlockUtil.getFluid(level, offsetPos);
        return FluidUtilBC.areFluidsEqual(fluid, getTankFluid());
    }

    private boolean canFillThrough(BlockPos pos) {
        if (level.isEmptyBlock(pos)) {
            return false;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(level, pos);
        return FluidUtilBC.areFluidsEqual(fluid, getTankFluid());
    }

    // ITickable

    @Override
    public void update() {
        if (level.isClientSide) {
            return;
        }

        if (tank.getFluidAmount() < 1000) {
            return;
        }

        tick++;
        if (tick % 16 == 0) {
            if (!tank.isEmpty() && !queue.isEmpty()) {
                FluidStack fluid = tank.drain(1000, FluidAction.SIMULATE);
                if (fluid.getAmount() >= 1000) {
                    BlockPos currentPos = queue.removeLast();
                    List<BlockPos> path = paths.get(currentPos);
                    boolean canFill = true;
                    if (path != null) {
                        for (BlockPos p : path) {
                            if (p.equals(currentPos)) {
                                continue;
                            }
                            if (!canFillThrough(p)) {
                                canFill = false;
                                break;
                            }
                        }
                    }
                    if (canFill && canFill(currentPos)) {
                        FakePlayer fakePlayer =
                            FakePlayerProvider.INSTANCE.getFakePlayer((ServerLevel) level, getOwner(), currentPos);
                        if (FluidUtil.tryPlaceFluid(fakePlayer, level, InteractionHand.MAIN_HAND, currentPos, tank,
                            fluid)) {
                            AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_FLOOD_SINGLE);
                            for (Direction side : Direction.values()) {
                                level.updateNeighborsAt(currentPos.relative(side), BCFactoryBlocks.FLOOD_GATE.get());
                            }
                            delayIndex = 0;
                            tick = 0;
                        }
                    } else {
                        buildQueue();
                    }
                }
            }
        }

        if (queue.isEmpty() && tick >= getCurrentDelay()) {
            delayIndex = Math.min(delayIndex + 1, REBUILD_DELAYS.length - 1);
            tick = 0;
            buildQueue();
        }
    }

    /** Called when a side is opened or closed: the fill queue is rebuilt from scratch. */
    public void onSidesChanged() {
        queue.clear();
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("fluid = " + tank.getDebugString());
        left.add("open sides = " + getOpenSides().stream().map(Enum::name).collect(Collectors.joining(", ")));
        left.add("delay = " + getCurrentDelay());
        left.add("tick = " + tick);
        left.add("queue size = " + queue.size());
    }
}
