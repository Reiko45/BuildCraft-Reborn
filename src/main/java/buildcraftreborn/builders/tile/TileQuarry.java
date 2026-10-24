/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.tile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.core.IAreaProvider;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.mj.MjBattery;
import buildcraftreborn.api.mj.MjCapabilityHelper;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.BCBuildersConfig;
import buildcraftreborn.builders.client.render.AdvDebuggerQuarry;
import buildcraftreborn.core.marker.VolumeCache;
import buildcraftreborn.core.marker.VolumeConnection;
import buildcraftreborn.core.marker.VolumeSubCache;
import buildcraftreborn.core.tile.TileMarkerVolume;
import buildcraftreborn.lib.BCLibConfig;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.lib.chunkload.ChunkLoaderManager;
import buildcraftreborn.lib.chunkload.IChunkLoadingTile;
import buildcraftreborn.lib.client.render.DetachedRenderer.IDetachedRenderer;
import buildcraftreborn.lib.inventory.AutomaticProvidingTransactor;
import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.misc.BlockUtil;
import buildcraftreborn.lib.misc.CapUtil;
import buildcraftreborn.lib.misc.InventoryUtil;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.MathUtil;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.lib.misc.VecUtil;
import buildcraftreborn.lib.misc.data.AxisOrder;
import buildcraftreborn.lib.misc.data.Box;
import buildcraftreborn.lib.misc.data.BoxIterator;
import buildcraftreborn.lib.misc.data.EnumAxisOrder;
import buildcraftreborn.lib.mj.MjBatteryReceiver;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.world.WorldBlockChangeListeners;

/** The quarry: builds a frame around its area, then mines it out layer by layer with a drill hanging from the frame.
 * <p>
 * 1.21 differences: block changes are seen through {@link WorldBlockChangeListeners} (1.12 used a world event
 * listener); the drill's arms no longer have collision boxes (1.21 has no event to add them); the frame is removed by
 * {@link #onRemove()} rather than by the block; a task in progress is saved properly (1.12 wrote it under one name and
 * looked for another, so it was always dropped); and the mining area stops at the bottom of the world, wherever that
 * is. */
public class TileQuarry extends TileBC_Neptune implements ITickable, IDebuggable, IChunkLoadingTile {
    private static final long MAX_POWER_PER_TICK = 512 * MjAPI.MJ;
    private static final ResourceLocation ADVANCEMENT_COMPLETE = BuildCraftReborn.id("diggy_diggy_hole");

    private final MjBattery battery = new MjBattery(24000 * MjAPI.MJ);
    public final Box frameBox = new Box();
    public final Box miningBox = new Box();
    private BoxIterator boxIterator;
    public final List<BlockPos> framePoses = new ArrayList<>();
    private int frameBoxPosesCount = 0;
    private final LinkedList<BlockPos> toCheck = new LinkedList<>();
    private final Set<BlockPos> firstCheckedPoses = new HashSet<>();
    private boolean firstChecked = false;
    private final Set<BlockPos> frameBreakBlockPoses = new TreeSet<>(
        BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(p -> getBlockPos().distSqr(p))));
    private final Set<BlockPos> framePlaceFramePoses = new HashSet<>();
    public Task currentTask = null;
    public Vec3 drillPos;
    public Vec3 clientDrillPos;
    public Vec3 prevClientDrillPos;
    private long debugPowerRate = 0;
    private double blockPercentSoFar;
    private double moveDistanceSoFar;
    private boolean chunkUnloading = false;

    private final WorldBlockChangeListeners.IBlockChangeListener blockChangeListener = this::onBlockChanged;

    public TileQuarry(BlockPos pos, BlockState state) {
        super(BCBuildersBlocks.QUARRY_TILE.get(), pos, state);
        caps.addMj(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
        caps.addCapabilityInstance(CapUtil.CAP_ITEM_TRANSACTOR, AutomaticProvidingTransactor.INSTANCE,
            EnumPipePart.VALUES);
    }

    private void onBlockChanged(Level changedLevel, BlockPos updatePos, BlockState newState) {
        if (changedLevel != level || isRemoved()) {
            return;
        }
        if (frameBox.isInitialized() && miningBox.isInitialized()) {
            if (frameBox.contains(updatePos)) {
                check(updatePos);
            } else if (miningBox.contains(updatePos) && boxIterator != null) {
                if (boxIterator.hasVisited(updatePos)) {
                    if (!canMoveThrough(updatePos) && canMoveDownTo(updatePos)) {
                        boxIterator.moveTo(updatePos);
                    }
                }
            }
        }
    }

    private BoxIterator createBoxIterator() {
        long x = worldPosition.getX();
        long y = worldPosition.getY();
        long z = worldPosition.getZ();
        // (As in 1.12: the z part is masked oddly, but it only picks the order that the quarry mines in.)
        long seed = ((x & 0xFFFF) << 0) | ((y & 0xFFFF) << 16) | ((z & 0xFFFF << 32));

        Random rand = new Random(seed);
        EnumAxisOrder axisOrder = rand.nextBoolean() ? EnumAxisOrder.XZY : EnumAxisOrder.ZXY;
        AxisOrder.Inversion inv = AxisOrder.Inversion.getFor(rand.nextBoolean(), rand.nextBoolean(), false);
        return new BoxIterator(miningBox, AxisOrder.getFor(axisOrder, inv), true);
    }

    /** Gets the current positions where frame blocks should be placed, in placement order (spreading out from the
     * quarry).
     *
     * @throws IllegalStateException if something went wrong during iteration, or the current {@link #frameBox} was
     *             incorrect compared to {@link #getBlockPos()} */
    private List<BlockPos> getFramePositions() {
        Set<BlockPos> visitedSet = new HashSet<>();
        List<BlockPos> framePositions = new ArrayList<>();

        List<BlockPos> openSet = new ArrayList<>();
        List<BlockPos> nextOpenSet = new ArrayList<>();
        openSet.add(worldPosition);

        Direction[] order = Direction.values();
        List<Direction> orderAsList = Arrays.asList(order);

        int maxIterationCount = frameBox.getBlocksOnEdgeCount();
        int iterationCount = 0;
        do {
            for (BlockPos p : openSet) {
                Collections.shuffle(orderAsList);
                for (Direction face : order) {
                    BlockPos next = p.relative(face);
                    if (frameBox.isOnEdge(next) && visitedSet.add(next)) {
                        nextOpenSet.add(next);
                        framePositions.add(next);
                    }
                }
            }
            openSet.clear();
            List<BlockPos> t = openSet;
            openSet = nextOpenSet;
            nextOpenSet = t;

            Collections.shuffle(openSet);

            if (openSet.size() > 8 * 3) {
                throw new IllegalStateException(framePositionsError("OpenSet got too big!", iterationCount, openSet));
            }
            iterationCount++;
            if (iterationCount >= maxIterationCount) {
                throw new IllegalStateException(framePositionsError(
                    "Failed to generate a correct list of frame positions! Was the frame box wrong?", iterationCount,
                    openSet));
            }
        } while (!openSet.isEmpty());

        if (framePositions.isEmpty()) {
            throw new IllegalStateException(framePositionsError(
                "Failed to generate a correct list of frame positions! Was the frame box wrong?", iterationCount,
                openSet));
        }
        return framePositions;
    }

    private String framePositionsError(String msg, int iterationCount, List<BlockPos> openSet) {
        return msg + "\n  Position = " + worldPosition + "\n  Frame Box = " + frameBox + "\n  Iteration Count = "
            + iterationCount + "\n  OpenSet = "
            + openSet.stream().map(Object::toString).collect(Collectors.joining("\n  ", "[", "]"));
    }

    private boolean shouldBeFrame(BlockPos p) {
        return frameBox.isOnEdge(p);
    }

    @Override
    public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (level.isClientSide) {
            return;
        }
        Direction facing = level.getBlockState(worldPosition).getValue(BlockBCBase_Neptune.PROP_FACING);
        BlockPos areaPos = worldPosition.relative(facing.getOpposite());
        BlockEntity tile = level.getBlockEntity(areaPos);
        BlockPos min = null, max = null;
        if (tile instanceof IAreaProvider provider) {
            min = provider.min();
            max = provider.max();
            int dx = max.getX() - min.getX();
            int dz = max.getZ() - min.getZ();
            if (dx < 3 || dz < 3) {
                min = null;
                max = null;
            } else {
                provider.removeFromWorld();
            }
        }
        if (min == null || max == null) {
            min = null;
            max = null;
            VolumeSubCache cache = VolumeCache.INSTANCE.getSubCache(level);
            for (BlockPos markerPos : cache.getAllMarkers()) {
                if (!(cache.getMarker(markerPos) instanceof TileMarkerVolume marker)) {
                    continue;
                }
                VolumeConnection connection = marker.getCurrentConnection();
                if (connection == null) {
                    continue;
                }
                Box volBox = connection.getBox();
                Box box2 = new Box();
                box2.initialize(volBox);
                if (!box2.isInitialized()) {
                    continue;
                }
                if (worldPosition.getY() != box2.min().getY()) {
                    continue;
                }
                if (box2.contains(worldPosition)) {
                    continue;
                }
                if (!box2.contains(areaPos)) {
                    continue;
                }
                if (box2.size().getX() < 3 || box2.size().getZ() < 3) {
                    continue;
                }
                box2.expand(1);
                box2.setMin(box2.min().above());
                if (box2.isOnEdge(worldPosition)) {
                    min = volBox.min();
                    max = volBox.max();
                    marker.removeFromWorld();
                    break;
                }
            }
        }
        if (min == null || max == null) {
            switch (facing.getOpposite()) {
                case WEST -> { // -X
                    min = worldPosition.offset(-11, 0, -5);
                    max = worldPosition.offset(-1, 4, 5);
                }
                case SOUTH -> { // +Z
                    min = worldPosition.offset(-5, 0, 1);
                    max = worldPosition.offset(5, 4, 11);
                }
                case NORTH -> { // -Z
                    min = worldPosition.offset(-5, 0, -11);
                    max = worldPosition.offset(5, 4, -1);
                }
                default -> { // +X
                    min = worldPosition.offset(1, 0, -5);
                    max = worldPosition.offset(11, 4, 5);
                }
            }
        }
        if (max.getY() - min.getY() < BCBuildersConfig.quarryFrameMinHeight) {
            max = new BlockPos(max.getX(), min.getY() + BCBuildersConfig.quarryFrameMinHeight, max.getZ());
        }
        if (level.isOutsideBuildHeight(max)) {
            int dist = max.getY() - min.getY();
            min = min.below(dist);
            max = max.below(dist);
        }
        frameBox.reset();
        frameBox.setMin(min);
        frameBox.setMax(max);
        miningBox.reset();
        int minY = max.getY() - 1 - BCLibConfig.miningMaxDepth();
        if (minY < level.getMinBuildHeight()) {
            minY = level.getMinBuildHeight();
        }
        miningBox.setMin(new BlockPos(min.getX() + 1, minY, min.getZ() + 1));
        miningBox.setMax(new BlockPos(max.getX() - 1, max.getY() - 1, max.getZ() - 1));
        updatePoses();
        setChanged();
    }

    private boolean canMine(BlockPos blockPos) {
        if (level.getBlockState(blockPos).getDestroySpeed(level, blockPos) < 0) {
            return false;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(level, blockPos);
        return fluid == null || fluid.getFluidType().getViscosity() <= 1000;
    }

    private boolean canMoveThrough(BlockPos blockPos) {
        if (level.isEmptyBlock(blockPos)) {
            return true;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(level, blockPos);
        return fluid != null && fluid.getFluidType().getViscosity() <= 1000;
    }

    private boolean canMoveDownTo(BlockPos blockPos) {
        for (int y = miningBox.max().getY(); y > blockPos.getY(); y--) {
            if (!canMoveThrough(VecUtil.replaceValue(blockPos, Axis.Y, y))) {
                return false;
            }
        }
        return true;
    }

    private boolean canIgnoreInFrameBox(BlockPos blockPos) {
        return !level.isEmptyBlock(blockPos) && BlockUtil.getFluidWithFlowing(level, blockPos) == null;
    }

    private void check(BlockPos blockPos) {
        frameBreakBlockPoses.remove(blockPos);
        framePlaceFramePoses.remove(blockPos);
        if (shouldBeFrame(blockPos)) {
            if (level.getBlockState(blockPos).getBlock() != BCBuildersBlocks.FRAME.get()) {
                if (canIgnoreInFrameBox(blockPos)) {
                    frameBreakBlockPoses.add(blockPos);
                } else {
                    framePlaceFramePoses.add(blockPos);
                }
            }
        } else {
            if (canIgnoreInFrameBox(blockPos)) {
                frameBreakBlockPoses.add(blockPos);
            }
        }
        if (!firstChecked) {
            firstCheckedPoses.add(blockPos);
            if (firstCheckedPoses.size() >= frameBoxPosesCount) {
                firstChecked = true;
            }
        }
    }

    // Loading and unloading

    @Override
    public void onLoad() {
        super.onLoad();
        chunkUnloading = false;
        if (!level.isClientSide) {
            WorldBlockChangeListeners.add(level, blockChangeListener);
            updatePoses();
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        // The chunk tickets must survive the chunk (or the whole server) unloading.
        chunkUnloading = true;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            WorldBlockChangeListeners.remove(level, blockChangeListener);
            if (!chunkUnloading) {
                ChunkLoaderManager.releaseChunksFor(this);
            }
            if (currentTask instanceof TaskBreakBlock breakTask) {
                level.destroyBlockProgress(breakTask.breakPos.hashCode(), breakTask.breakPos, -1);
            }
        }
    }

    /** Called when the quarry is broken: takes its frame down with it. */
    @Override
    public void onRemove() {
        super.onRemove();
        for (BlockPos blockPos : framePoses) {
            if (level.getBlockState(blockPos).getBlock() == BCBuildersBlocks.FRAME.get()) {
                level.removeBlock(blockPos, false);
            }
        }
    }

    @Nullable
    @Override
    public LoadType getLoadType() {
        return LoadType.HARD;
    }

    @Nullable
    @Override
    public Set<ChunkPos> getChunksToLoad() {
        if (!miningBox.isInitialized()) {
            return null;
        }
        Set<ChunkPos> chunkPoses = new HashSet<>();
        ChunkPos minChunkPos = new ChunkPos(frameBox.min());
        ChunkPos maxChunkPos = new ChunkPos(frameBox.max());
        for (int x = minChunkPos.x; x <= maxChunkPos.x; x++) {
            for (int z = minChunkPos.z; z <= maxChunkPos.z; z++) {
                chunkPoses.add(new ChunkPos(x, z));
            }
        }
        return chunkPoses;
    }

    private void updatePoses() {
        framePoses.clear();
        frameBoxPosesCount = 0;
        toCheck.clear();
        firstCheckedPoses.clear();
        firstChecked = false;
        frameBreakBlockPoses.clear();
        framePlaceFramePoses.clear();
        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() == BCBuildersBlocks.QUARRY.get() && frameBox.isInitialized()) {
            List<BlockPos> blocksInArea = new ArrayList<>(frameBox.getBlocksInArea());
            blocksInArea.sort(BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(worldPosition::distSqr)));
            frameBoxPosesCount = blocksInArea.size();
            toCheck.addAll(blocksInArea);
            framePoses.addAll(getFramePositions());
            ChunkLoaderManager.loadChunksForTile(this);
        }
    }

    // Ticking

    @Override
    public void update() {
        if (level.isClientSide) {
            prevClientDrillPos = clientDrillPos;
            clientDrillPos = drillPos;
            if (currentTask != null) {
                currentTask.clientTick();
            }
            return;
        }

        if (!frameBox.isInitialized() || !miningBox.isInitialized()) {
            return;
        }

        if (!toCheck.isEmpty()) {
            for (int i = 0; i < (firstChecked ? 10 : 500); i++) {
                BlockPos blockPos = toCheck.pollFirst();
                check(blockPos);
                toCheck.addLast(blockPos);
            }
        }

        if (!firstChecked) {
            return;
        }

        long max;
        if (battery.getStored() > battery.getCapacity() / 2) {
            max = MAX_POWER_PER_TICK;
        } else {
            long roundedUp = battery.getStored() + MjAPI.MJ / 2;
            if (roundedUp > Long.MAX_VALUE / MAX_POWER_PER_TICK) {
                // The multiplication would overflow, so we'll have to use BigInteger for this bit
                max = BigInteger.valueOf(roundedUp).multiply(BigInteger.valueOf(MAX_POWER_PER_TICK))
                    .divide(BigInteger.valueOf(battery.getCapacity() / 2)).longValue();
            } else {
                max = MAX_POWER_PER_TICK * roundedUp / (battery.getCapacity() / 2);
            }
            max = MathUtil.clamp(max, 0, MAX_POWER_PER_TICK);
        }
        debugPowerRate = max;
        blockPercentSoFar = 0;
        moveDistanceSoFar = 0;

        int maxTasks = Math.max(1, (int) (max * BCBuildersConfig.quarryMaxTasksPerTick / MAX_POWER_PER_TICK));
        boolean sendUpdate = false;
        power_loop: for (int i = 0; i < maxTasks; i++) {

            if (currentTask != null) {
                long needed = currentTask.getRequiredPowerThisTick();
                long added;
                final int mult = BCBuildersConfig.quarryTaskPowerDivisor;
                if (mult > 0) {
                    long nNeeded = needed * (mult + i) / mult;
                    long leftover = (needed * (mult + i)) % mult;
                    long power = battery.extractPower(0, Math.min(max, nNeeded));
                    max -= power;
                    added = power * mult / (mult + i);
                    if (leftover > 0) {
                        added++;
                    }
                } else {
                    added = battery.extractPower(0, Math.min(max, needed));
                    max -= added;
                }
                if (currentTask.addPower(added)) {
                    currentTask = null;
                } else {
                    sendUpdate = true;
                    break;
                }
            }

            if (!frameBreakBlockPoses.isEmpty()) {
                BlockPos blockPos = frameBreakBlockPoses.iterator().next();
                if (canMine(blockPos)) {
                    drillPos = null;
                    currentTask = new TaskBreakBlock(blockPos);
                    sendUpdate = true;
                }
                check(blockPos);
                continue power_loop;
            }

            if (!framePlaceFramePoses.isEmpty()) {
                for (BlockPos blockPos : framePoses) {
                    if (!framePlaceFramePoses.contains(blockPos)) {
                        continue;
                    }
                    check(blockPos);
                    if (!framePlaceFramePoses.contains(blockPos)) {
                        continue;
                    }
                    drillPos = null;
                    currentTask = new TaskAddFrame(blockPos);
                    sendUpdate = true;
                    continue power_loop;
                }
            }

            if (boxIterator == null || drillPos == null) {
                boxIterator = createBoxIterator();
                while (canMoveThrough(boxIterator.getCurrent()) || !canMine(boxIterator.getCurrent())
                    || !canMoveDownTo(boxIterator.getCurrent())) {
                    if (boxIterator.advance() == null) {
                        break;
                    }
                }
                drillPos = Vec3.atLowerCornerOf(miningBox.closestInsideTo(worldPosition));
            }

            if (boxIterator != null && boxIterator.hasNext()) {
                while (canMoveThrough(boxIterator.getCurrent()) || !canMine(boxIterator.getCurrent())
                    || !canMoveDownTo(boxIterator.getCurrent())) {
                    if (boxIterator.advance() == null) {
                        break;
                    }
                }

                if (boxIterator.hasNext()) {
                    boolean found = false;
                    Vec3 current = Vec3.atLowerCornerOf(boxIterator.getCurrent());
                    if (drillPos.distanceToSqr(current) >= 1) {
                        currentTask = new TaskMoveDrill(drillPos, current);
                        found = true;
                    } else if (canMine(boxIterator.getCurrent())) {
                        currentTask = new TaskBreakBlock(boxIterator.getCurrent());
                        found = true;
                    }

                    if (found) {
                        sendUpdate = true;
                    } else {
                        AABB box = miningBox.getBoundingBox();
                        if (box.maxX - box.minX == 63 && box.maxZ - box.minZ == 63) {
                            AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_COMPLETE);
                        }
                    }
                }
            }
        }
        debugPowerRate -= max;
        if (sendUpdate) {
            sendNetworkUpdate(NET_RENDER_DATA);
            setChanged();
        }
    }

    // Saving

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.put("box", miningBox.writeToNBT());
        nbt.put("frame", frameBox.writeToNBT());
        if (boxIterator != null) {
            nbt.put("boxIterator", boxIterator.writeToNbt());
        }
        nbt.put("battery", battery.serializeNBT());
        if (currentTask != null) {
            nbt.putByte("currentTaskId", (byte) EnumTaskType.of(currentTask).ordinal());
            nbt.put("currentTaskData", currentTask.serializeNBT());
        }
        if (drillPos != null) {
            nbt.put("drillPos", NBTUtilBC.writeVec3d(drillPos));
        }
        nbt.putBoolean("firstChecked", firstChecked);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        miningBox.initialize(nbt.getCompound("box"));
        frameBox.initialize(nbt.getCompound("frame"));
        boxIterator = nbt.contains("boxIterator") ? BoxIterator.readFromNbt(nbt.getCompound("boxIterator")) : null;
        battery.deserializeNBT(nbt.getCompound("battery"));
        currentTask = null;
        if (nbt.contains("currentTaskId")) {
            int taskId = nbt.getByte("currentTaskId");
            if (taskId >= 0 && taskId < EnumTaskType.values().length) {
                currentTask = EnumTaskType.values()[taskId].supplier.apply(this);
                // (This may set currentTask back to null if the data is bad.)
                currentTask.readFromNBT(nbt.getCompound("currentTaskData"));
            }
        }
        drillPos = NBTUtilBC.readVec3d(nbt.get("drillPos"));
        firstChecked = nbt.getBoolean("firstChecked");
        if (drillPos != null && drillPos.distanceToSqr(Vec3.atLowerCornerOf(worldPosition)) > 1024 * 1024) {
            drillPos = null;
        }

        // Validation
        boolean isValid = false;
        if (frameBox.isInitialized() && miningBox.isInitialized()) {
            Direction validFace = null;
            for (Direction face : Direction.Plane.HORIZONTAL) {
                // We can't read the blockstate yet so instead we'll have to try all possible faces
                if (frameBox.isOnEdge(worldPosition.relative(face))) {
                    validFace = face;
                    break;
                }
            }
            if (validFace != null) {
                BlockPos fMin = frameBox.min();
                BlockPos fMax = frameBox.max();
                BlockPos mMin = miningBox.min();
                BlockPos mMax = miningBox.max();
                isValid = fMin.getX() + 1 == mMin.getX() && fMax.getX() - 1 == mMax.getX()
                    && fMin.getZ() + 1 == mMin.getZ() && fMax.getZ() - 1 == mMax.getZ()
                    && fMin.getY() >= mMin.getY() && fMax.getY() - 1 == mMax.getY();
            }
        }
        if (!isValid) {
            frameBox.reset();
            miningBox.reset();
            drillPos = null;
        }
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                frameBox.writeData(buffer);
                miningBox.writeData(buffer);
                buffer.writeBoolean(drillPos != null);
                if (drillPos != null) {
                    buffer.writeVec3(drillPos);
                }
                buffer.writeBoolean(currentTask != null);
                if (currentTask != null) {
                    buffer.writeByte(EnumTaskType.of(currentTask).ordinal());
                    // Twice: the client reads it once to compare with its current task.
                    for (int i = 0; i < 2; i++) {
                        currentTask.toBytes(buffer);
                    }
                }
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                frameBox.readData(buffer);
                miningBox.readData(buffer);
                if (buffer.readBoolean()) {
                    drillPos = buffer.readVec3();
                } else {
                    drillPos = null;
                }
                if (buffer.readBoolean()) {
                    int taskId = buffer.readByte();
                    Task task = EnumTaskType.values()[taskId].supplier.apply(this);
                    task.fromBytes(buffer);
                    if (currentTask == null || !currentTask.equals(task)) {
                        currentTask = task;
                        Task tempTask = EnumTaskType.values()[taskId].supplier.apply(this);
                        tempTask.fromBytes(buffer);
                    } else {
                        currentTask.fromBytes(buffer);
                    }
                } else {
                    currentTask = null;
                }
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("battery = " + battery.getDebugString());
        left.add("rate = " + LocaleUtil.localizeMjFlow(debugPowerRate).getString());
        left.add("frameBox");
        left.add(" - min = " + frameBox.min());
        left.add(" - max = " + frameBox.max());
        left.add("miningBox:");
        left.add(" - min = " + miningBox.min());
        left.add(" - max = " + miningBox.max());

        left.add("firstCheckedPoses = " + firstCheckedPoses.size());
        left.add("frameBoxPosesCount = " + frameBoxPosesCount);
        left.add("firstChecked = " + firstChecked);

        BoxIterator iter = boxIterator;
        left.add("current = " + (iter == null ? "null" : iter.getCurrent()));

        Task task = currentTask;
        if (task != null) {
            left.add("task:");
            left.add(" - class = " + task.getClass().getSimpleName());
            left.add(" - power = " + LocaleUtil.localizeMj(task.power).getString());
            left.add(" - target = " + LocaleUtil.localizeMj(task.getTarget()).getString());
        } else {
            left.add("task = null");
        }
        left.add("drill = " + drillPos);
    }

    /** For the renderer and GameTests: how much of the battery is full (0 to 1). */
    public double getBatteryFill() {
        return battery.getStored() / (double) battery.getCapacity();
    }

    public MjBattery getBattery() {
        return battery;
    }

    /** For GameTests: true once every position of the frame box has been looked at. */
    public boolean hasCheckedFrame() {
        return firstChecked;
    }

    // Tasks

    private enum EnumTaskType {
        BREAK_BLOCK(TaskBreakBlock.class, quarry -> quarry.new TaskBreakBlock()),
        ADD_FRAME(TaskAddFrame.class, quarry -> quarry.new TaskAddFrame()),
        MOVE_DRILL(TaskMoveDrill.class, quarry -> quarry.new TaskMoveDrill());

        public final Class<? extends Task> clazz;
        public final Function<TileQuarry, Task> supplier;

        EnumTaskType(Class<? extends Task> clazz, Function<TileQuarry, Task> supplier) {
            this.clazz = clazz;
            this.supplier = supplier;
        }

        static EnumTaskType of(Task task) {
            for (EnumTaskType type : values()) {
                if (type.clazz == task.getClass()) {
                    return type;
                }
            }
            throw new IllegalStateException("Unknown task " + task.getClass());
        }
    }

    public abstract class Task {
        public long power;
        public long clientPower;
        public long prevClientPower;

        CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.putLong("power", power);
            return nbt;
        }

        void readFromNBT(CompoundTag nbt) {
            power = nbt.getLong("power");
            if (power < 0) {
                power = 0;
            }
        }

        void toBytes(PacketBufferBC buffer) {
            buffer.writeLong(power);
        }

        void fromBytes(PacketBufferBC buffer) {
            power = buffer.readLong();
        }

        void clientTick() {
            prevClientPower = clientPower;
            clientPower = power;
        }

        public abstract long getTarget();

        public long getRequiredPowerThisTick() {
            return Math.max(0, getTarget() - power);
        }

        /** @return {@code true} if this task has been completed, or cancelled. */
        protected abstract boolean onReceivePower(long added, long target);

        protected abstract boolean finish(long added, long target);

        /** @return {@code true} if this task has been completed, or cancelled. */
        final boolean addPower(long microJoules) {
            power += microJoules;
            long target = getTarget();
            if (power >= target) {
                if (!finish(microJoules, target)) {
                    battery.addPower(Math.min(power, battery.getCapacity() - battery.getStored()), false);
                }
                return true;
            } else {
                return onReceivePower(microJoules, target);
            }
        }
    }

    public class TaskBreakBlock extends Task {
        public BlockPos breakPos = BlockPos.ZERO;

        TaskBreakBlock() {}

        TaskBreakBlock(BlockPos pos) {
            this.breakPos = pos;
        }

        @Override
        CompoundTag serializeNBT() {
            CompoundTag nbt = super.serializeNBT();
            nbt.put("breakPos", NBTUtilBC.writeBlockPos(breakPos));
            return nbt;
        }

        @Override
        void readFromNBT(CompoundTag nbt) {
            super.readFromNBT(nbt);
            breakPos = NBTUtilBC.readBlockPos(nbt.get("breakPos"));
            if (breakPos == null) {
                // We failed to read, abort
                currentTask = null;
            }
        }

        @Override
        void toBytes(PacketBufferBC buffer) {
            super.toBytes(buffer);
            buffer.writeBlockPos(breakPos);
        }

        @Override
        void fromBytes(PacketBufferBC buffer) {
            super.fromBytes(buffer);
            breakPos = buffer.readBlockPos();
        }

        @Override
        public long getTarget() {
            return BlockUtil.computeBlockBreakPower(level, breakPos);
        }

        @Override
        public long getRequiredPowerThisTick() {
            long target = getTarget();
            long req = Math.max(0, target - power);
            double rate = BCBuildersConfig.quarryMaxBlockMineRate;
            if (rate < 0.1) {
                return req;
            }
            rate /= 20; // seconds -> ticks
            rate -= blockPercentSoFar;
            if (rate <= 0) {
                return 0;
            }
            return Math.min(req, (long) (target * rate));
        }

        @Override
        protected boolean onReceivePower(long added, long target) {
            blockPercentSoFar += added / (double) target;
            if (!level.isEmptyBlock(breakPos)) {
                level.destroyBlockProgress(breakPos.hashCode(), breakPos, (int) (power * 9 / getTarget()));
                return false;
            } else {
                return true;
            }
        }

        @Override
        protected boolean finish(long added, long target) {
            blockPercentSoFar += added / (double) target;
            if (!canMine(breakPos)) {
                return true;
            }
            level.destroyBlockProgress(breakPos.hashCode(), breakPos, -1);
            Optional<List<ItemStack>> stacks = BlockUtil.breakBlockAndGetDrops((ServerLevel) level, breakPos,
                new ItemStack(Items.DIAMOND_PICKAXE), getOwner(), true);
            if (stacks.isPresent()) {
                // The drill pos will be null if we are making the frame: this is when we want to destroy the block,
                // not drop its contents
                if (drillPos != null) {
                    stacks.get().forEach(stack -> InventoryUtil.addToBestAcceptor(level, worldPosition, null, stack));
                }
            }
            check(breakPos);
            return stacks.isPresent();
        }

        @Override
        public boolean equals(Object o) {
            return o == this || o != null && getClass() == o.getClass() && breakPos.equals(((TaskBreakBlock) o).breakPos);
        }

        @Override
        public int hashCode() {
            return breakPos.hashCode();
        }
    }

    public class TaskAddFrame extends Task {
        public BlockPos framePos = BlockPos.ZERO;

        TaskAddFrame() {}

        TaskAddFrame(BlockPos framePos) {
            this.framePos = framePos;
        }

        @Override
        CompoundTag serializeNBT() {
            CompoundTag nbt = super.serializeNBT();
            nbt.put("framePos", NBTUtilBC.writeBlockPos(framePos));
            return nbt;
        }

        @Override
        void readFromNBT(CompoundTag nbt) {
            super.readFromNBT(nbt);
            framePos = NBTUtilBC.readBlockPos(nbt.get("framePos"));
            if (framePos == null) {
                // We failed to read, abort
                currentTask = null;
            }
        }

        @Override
        void toBytes(PacketBufferBC buffer) {
            super.toBytes(buffer);
            buffer.writeBlockPos(framePos);
        }

        @Override
        void fromBytes(PacketBufferBC buffer) {
            super.fromBytes(buffer);
            framePos = buffer.readBlockPos();
        }

        @Override
        public long getTarget() {
            return 24 * MjAPI.MJ;
        }

        @Override
        protected boolean onReceivePower(long added, long target) {
            return canIgnoreInFrameBox(framePos);
        }

        @Override
        protected boolean finish(long added, long target) {
            if (canIgnoreInFrameBox(framePos)) {
                return false;
            }
            BlockState frame = BCBuildersBlocks.FRAME.get().defaultBlockState();
            level.setBlockAndUpdate(framePos, BCBuildersBlocks.FRAME.get().withConnections(frame, level, framePos));
            return true;
        }

        @Override
        public boolean equals(Object o) {
            return o == this || o != null && getClass() == o.getClass() && framePos.equals(((TaskAddFrame) o).framePos);
        }

        @Override
        public int hashCode() {
            return framePos.hashCode();
        }
    }

    public class TaskMoveDrill extends Task {
        public Vec3 from = Vec3.ZERO;
        public Vec3 to = Vec3.ZERO;

        TaskMoveDrill() {}

        TaskMoveDrill(Vec3 from, Vec3 to) {
            this.from = from;
            this.to = to;
        }

        @Override
        CompoundTag serializeNBT() {
            CompoundTag nbt = super.serializeNBT();
            nbt.put("from", NBTUtilBC.writeVec3d(from));
            nbt.put("to", NBTUtilBC.writeVec3d(to));
            return nbt;
        }

        @Override
        void readFromNBT(CompoundTag nbt) {
            super.readFromNBT(nbt);
            from = NBTUtilBC.readVec3d(nbt.get("from"));
            to = NBTUtilBC.readVec3d(nbt.get("to"));
            if (from == null || to == null) {
                // We failed to read. Abort.
                currentTask = null;
            }
        }

        @Override
        void toBytes(PacketBufferBC buffer) {
            super.toBytes(buffer);
            buffer.writeVec3(from);
            buffer.writeVec3(to);
        }

        @Override
        void fromBytes(PacketBufferBC buffer) {
            super.fromBytes(buffer);
            from = buffer.readVec3();
            to = buffer.readVec3();
        }

        @Override
        public long getTarget() {
            return (long) (from.distanceTo(to) * 20 * MjAPI.MJ);
        }

        @Override
        public long getRequiredPowerThisTick() {
            long req = Math.max(0, getTarget() - power);

            double max = BCBuildersConfig.quarryMaxFrameMoveSpeed;
            if (max < 0.1) {
                return req;
            }
            max /= 20;
            max -= moveDistanceSoFar;
            if (max <= 0) {
                return 0;
            }
            return Math.min(req, (long) (max * 20 * MjAPI.MJ));
        }

        @Override
        protected boolean onReceivePower(long added, long target) {
            moveDistanceSoFar += added / (double) MjAPI.MJ;
            drillPos = from.scale(1 - power / (double) target).add(to.scale(power / (double) target));
            return false;
        }

        @Override
        protected boolean finish(long added, long target) {
            moveDistanceSoFar += added / (double) MjAPI.MJ;
            drillPos = to;
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TaskMoveDrill other = (TaskMoveDrill) o;
            return from.equals(other.from) && to.equals(other.to);
        }

        @Override
        public int hashCode() {
            return from.hashCode() * 31 + to.hashCode();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IDetachedRenderer getDebugRenderer() {
        return new AdvDebuggerQuarry(this);
    }
}
