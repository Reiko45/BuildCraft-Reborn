/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.tile;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.core.SafeTimeTracker;
import buildcraftreborn.api.mj.IMjReceiver;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.core.tile.ITileOilSpring;
import buildcraftreborn.energy.BCEnergyFluids;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.lib.BCLibConfig;
import buildcraftreborn.lib.fluid.Tank;
import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.misc.BlockUtil;
import buildcraftreborn.lib.misc.FluidUtilBC;
import buildcraftreborn.lib.mj.MjRedstoneBatteryReceiver;
import buildcraftreborn.lib.net.PacketBufferBC;

/** Lowers a tube to the first fluid below it and pumps out the connected fluid blocks, furthest first, into its tank
 * and then into neighbouring tanks. Water that is an infinite source isn't removed (unless {@code pumpsConsumeWater}
 * is on). Pumping oil above a well's oil spring tells the spring, which counts what each player has taken. */
public class TilePump extends TileMiner {
    public static final boolean DEBUG_PUMP = Boolean.getBoolean("buildcraftreborn.debug.pump");

    private static final Direction[] SEARCH_NORMAL = new Direction[] {
        Direction.UP, Direction.NORTH, Direction.SOUTH,
        Direction.WEST, Direction.EAST
    };

    private static final Direction[] SEARCH_GASEOUS = new Direction[] {
        Direction.DOWN, Direction.NORTH, Direction.SOUTH,
        Direction.WEST, Direction.EAST
    };

    static final class FluidPath {
        public final BlockPos thisPos;

        @Nullable
        public final FluidPath parent;

        public FluidPath(BlockPos thisPos, @Nullable FluidPath parent) {
            this.thisPos = thisPos;
            this.parent = parent;
        }

        public FluidPath and(BlockPos pos) {
            return new FluidPath(pos, this);
        }
    }

    private static final ResourceLocation ADVANCEMENT_DRAIN_ANY = BuildCraftReborn.id("draining_the_world");

    private static final ResourceLocation ADVANCEMENT_DRAIN_OIL = BuildCraftReborn.id("oil_platform");

    private final Tank tank = new Tank("tank", 16 * 1000, this);
    private boolean queueBuilt = false;
    private final Map<BlockPos, FluidPath> paths = new HashMap<>();
    private BlockPos fluidConnection;
    private final Deque<BlockPos> queue = new ArrayDeque<>();
    private boolean isInfiniteWaterSource;
    private final SafeTimeTracker rebuildDelay = new SafeTimeTracker(30);

    /** The position just below the bottom of the pump tube. */
    private BlockPos targetPos;

    @Nullable
    private BlockPos oilSpringPos;

    public TilePump(BlockPos pos, BlockState state) {
        super(BCFactoryBlocks.PUMP_TILE.get(), pos, state);
        tank.setCanFill(false);
        tankManager.add(tank);
        caps.addCapabilityInstance(Capabilities.FluidHandler.BLOCK, tank, EnumPipePart.VALUES);
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjRedstoneBatteryReceiver(battery);
    }

    private void buildQueue() {
        queue.clear();
        paths.clear();
        Fluid queueFluid = null;
        isInfiniteWaterSource = false;
        Set<BlockPos> checked = new HashSet<>();
        List<BlockPos> nextPosesToCheck = new ArrayList<>();
        for (targetPos = worldPosition.below(); !level.isOutsideBuildHeight(targetPos); targetPos = targetPos.below()) {
            if (worldPosition.getY() - targetPos.getY() > BCLibConfig.miningMaxDepth()) {
                break;
            }
            if (BlockUtil.getFluidWithFlowing(level, targetPos) != null) {
                queueFluid = BlockUtil.getFluidWithFlowing(level, targetPos);
                nextPosesToCheck.add(targetPos);
                paths.put(targetPos, new FluidPath(targetPos, null));
                checked.add(targetPos);
                if (BlockUtil.getFluid(level, targetPos) != null) {
                    queue.add(targetPos);
                }
                fluidConnection = targetPos;
                break;
            }
            if (!level.isEmptyBlock(targetPos) && level.getBlockState(targetPos).getBlock() != BCFactoryBlocks.TUBE.get()) {
                break;
            }
        }
        if (nextPosesToCheck.isEmpty() || queueFluid == null) {
            return;
        }

        long start = System.nanoTime();
        buildQueue0(queueFluid, nextPosesToCheck, checked);
        if (DEBUG_PUMP) {
            BuildCraftReborn.LOGGER.info("[factory.pump] Pump @ {} built a queue of {} in {}us", worldPosition,
                queue.size(), (System.nanoTime() - start) / 1000);
        }
    }

    private void buildQueue0(Fluid queueFluid, List<BlockPos> nextPosesToCheck, Set<BlockPos> checked) {
        Direction[] directions = queueFluid.getFluidType().isLighterThanAir() ? SEARCH_GASEOUS : SEARCH_NORMAL;
        boolean isWater = !BCLibConfig.pumpsConsumeWater() && FluidUtilBC.areFluidsEqual(queueFluid, Fluids.WATER);
        final int maxDistance = BCLibConfig.pumpMaxDistance();
        final int maxLengthSquared = maxDistance * maxDistance;
        outer: while (!nextPosesToCheck.isEmpty()) {
            List<BlockPos> nextPosesToCheckCopy = new ArrayList<>(nextPosesToCheck);
            nextPosesToCheck.clear();
            for (BlockPos posToCheck : nextPosesToCheckCopy) {
                int count = 0;
                for (Direction side : directions) {
                    BlockPos offsetPos = posToCheck.relative(side);
                    if (offsetPos.distSqr(targetPos) > maxLengthSquared) {
                        continue;
                    }
                    boolean isNew = checked.add(offsetPos);
                    if (isNew) {
                        Fluid fluidAt = BlockUtil.getFluidWithFlowing(level, offsetPos);
                        boolean eq = FluidUtilBC.areFluidsEqual(fluidAt, queueFluid);
                        if (eq) {
                            FluidPath oldPath = paths.get(posToCheck);
                            FluidPath path = new FluidPath(offsetPos, oldPath);
                            paths.put(offsetPos, path);
                            if (BlockUtil.getFluid(level, offsetPos) != null) {
                                queue.add(offsetPos);
                            }
                            nextPosesToCheck.add(offsetPos);
                            count++;
                        }
                    } else {
                        // We've already tested this block: it *must* be a valid water source
                        count++;
                    }
                }
                if (isWater) {
                    if (count >= 2) {
                        BlockState below = level.getBlockState(posToCheck.below());
                        // Same check as in FlowingFluid.getNewLiquid: if that method changes how it checks for
                        // adjacent water sources then this also needs updating
                        Fluid fluidBelow = BlockUtil.getFluidWithoutFlowing(below);
                        if (FluidUtilBC.areFluidsEqual(fluidBelow, Fluids.WATER) || below.isSolid()) {
                            isInfiniteWaterSource = true;
                            break outer;
                        }
                    }
                }
            }
        }
        if (isOil(queueFluid)) {
            List<BlockPos> springPositions = new ArrayList<>();
            int bottom = level.getMinBuildHeight();
            BlockPos center = new BlockPos(worldPosition.getX(), bottom, worldPosition.getZ());
            for (BlockPos spring : BlockPos.betweenClosed(center.offset(-10, 0, -10), center.offset(10, 0, 10))) {
                BlockEntity tile = level.getBlockEntity(spring);
                if (tile instanceof ITileOilSpring) {
                    springPositions.add(spring.immutable());
                }
            }
            switch (springPositions.size()) {
                case 0:
                    break;
                case 1:
                    oilSpringPos = springPositions.get(0);
                    break;
                default:
                    springPositions.sort(Comparator.comparingDouble(worldPosition::distSqr));
                    oilSpringPos = springPositions.get(0);
            }
        }
    }

    private static boolean isOil(@Nullable Fluid queueFluid) {
        return FluidUtilBC.areFluidsEqual(queueFluid, BCEnergyFluids.crudeOil[0].getSource());
    }

    private boolean canDrain(BlockPos blockPos) {
        Fluid fluid = BlockUtil.getFluid(level, blockPos);
        return tank.isEmpty() ? fluid != null : FluidUtilBC.areFluidsEqual(fluid, tank.getFluid().getFluid());
    }

    private void nextPos() {
        while (!queue.isEmpty()) {
            currentPos = queue.removeLast();
            if (canDrain(currentPos)) {
                updateLength();
                return;
            }
        }
        currentPos = null;
        updateLength();
    }

    @Override
    @Nullable
    protected BlockPos getTargetPos() {
        if (queue.isEmpty()) {
            return null;
        }
        return targetPos;
    }

    @Override
    public void update() {
        if (!queueBuilt && !level.isClientSide) {
            buildQueue();
            queueBuilt = true;
        }

        super.update();

        if (!level.isClientSide) {
            FluidUtilBC.pushFluidAround(level, worldPosition, tank);
        }
    }

    @Override
    public void mine() {
        if (tank.getFluidAmount() > tank.getCapacity() / 2) {
            return;
        }
        long target = 10 * MjAPI.MJ;
        if (currentPos != null && paths.containsKey(currentPos)) {
            progress += battery.extractPower(0, target - progress);
            if (progress < target) {
                return;
            }

            FluidStack drain = BlockUtil.drainBlock(level, currentPos, false);

            drain_attempt: {

                if (drain.isEmpty()) {
                    if (DEBUG_PUMP) {
                        BuildCraftReborn.LOGGER.info("Pump @ {} tried to drain {} but couldn't because no fluid was "
                            + "drained!", worldPosition, currentPos);
                    }
                    break drain_attempt;
                }

                BlockPos invalid = getFirstInvalidPointOnPath(currentPos);
                if (invalid != null) {
                    if (DEBUG_PUMP) {
                        BuildCraftReborn.LOGGER.info("Pump @ {} tried to drain {} but couldn't because the path "
                            + "stopped at {}!", worldPosition, currentPos, invalid);
                    }
                    break drain_attempt;
                } else if (!canDrain(currentPos)) {
                    if (DEBUG_PUMP) {
                        BuildCraftReborn.LOGGER.info("Pump @ {} tried to drain {} but couldn't because it couldn't "
                            + "be drained!", worldPosition, currentPos);
                    }
                    break drain_attempt;
                }
                tank.fillInternal(drain, FluidAction.EXECUTE);
                progress = 0;
                isInfiniteWaterSource &= !BCLibConfig.pumpsConsumeWater();
                if (isInfiniteWaterSource) {
                    isInfiniteWaterSource = FluidUtilBC.areFluidsEqual(drain.getFluid(), Fluids.WATER);
                }
                AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_DRAIN_ANY);
                if (!isInfiniteWaterSource) {
                    BlockUtil.drainBlock(level, currentPos, true);
                    if (isOil(drain.getFluid())) {
                        AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_DRAIN_OIL);
                        if (oilSpringPos != null) {
                            BlockEntity tile = level.getBlockEntity(oilSpringPos);
                            if (tile instanceof ITileOilSpring spring) {
                                spring.onPumpOil(getOwner(), currentPos);
                            }
                        }
                    }
                    paths.remove(currentPos);
                    nextPos();
                }
                return;
            }
            if (!rebuildDelay.markTimeIfDelay(level)) {
                return;
            }
        } else {
            if (currentPos == null && !rebuildDelay.markTimeIfDelay(level)) {
                return;
            }
            if (DEBUG_PUMP) {
                if (currentPos == null) {
                    BuildCraftReborn.LOGGER.info("Pump @ {} is rebuilding it's queue...", worldPosition);
                } else {
                    BuildCraftReborn.LOGGER.info("Pump @ {} is rebuilding it's queue because we don't have a path "
                        + "for {}", worldPosition, currentPos);
                }
            }
        }
        buildQueue();
        nextPos();
    }

    @Nullable
    private BlockPos getFirstInvalidPointOnPath(BlockPos from) {
        FluidPath path = paths.get(from);
        if (path == null) {
            return from;
        }
        do {
            if (BlockUtil.getFluidWithFlowing(level, path.thisPos) == null) {
                return path.thisPos;
            }
        } while ((path = path.parent) != null);
        return null;
    }

    // NBT

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        oilSpringPos = NbtUtils.readBlockPos(nbt, "oilSpringPos").orElse(null);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        if (oilSpringPos != null) {
            nbt.put("oilSpringPos", NbtUtils.writeBlockPos(oilSpringPos));
        }
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_LED_STATUS, buffer, side);
            } else if (id == NET_LED_STATUS) {
                tank.writeToBuffer(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_LED_STATUS, buffer, side, ctx);
            } else if (id == NET_LED_STATUS) {
                tank.readFromBuffer(buffer);
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("fluid = " + tank.getDebugString());
        left.add("queue size = " + queue.size());
        left.add("infinite = " + isInfiniteWaterSource);
    }

    @Override
    protected long getBatteryCapacity() {
        return 50 * MjAPI.MJ;
    }
}
