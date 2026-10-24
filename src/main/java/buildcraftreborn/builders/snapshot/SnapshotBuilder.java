/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.lib.misc.BlockUtil;
import buildcraftreborn.lib.misc.VecUtil;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.world.WorldBlockChangeListeners;

/** Breaks and places the blocks of a snapshot, using the power and items of the tile it belongs to (a builder or a
 * filler). */
public abstract class SnapshotBuilder<T extends ITileForSnapshotBuilder> {
    private static final int MAX_QUEUE_SIZE = 16;
    protected static final byte CHECK_RESULT_UNKNOWN = 0;
    protected static final byte CHECK_RESULT_CORRECT = 1;
    protected static final byte CHECK_RESULT_TO_BREAK = 2;
    protected static final byte CHECK_RESULT_TO_PLACE = 3;
    private static final byte REQUIRED_UNKNOWN = 0;
    private static final byte REQUIRED_TRUE = 1;
    private static final byte REQUIRED_FALSE = 2;
    private static final int CHECKS_PER_TICK = 10;
    private static final long MAX_POWER_PER_TICK = 256 * MjAPI.MJ;

    protected final T tile;
    /** 1.12's world event listener: rechecks a position in the box whenever its block changes. */
    private final WorldBlockChangeListeners.IBlockChangeListener worldEventListener = this::onBlockChanged;
    private final Queue<BreakTask> breakTasks = new ArrayDeque<>();
    public final Queue<BreakTask> clientBreakTasks = new ArrayDeque<>();
    public final Queue<BreakTask> prevClientBreakTasks = new ArrayDeque<>();
    private final Queue<PlaceTask> placeTasks = new ArrayDeque<>();
    public final Queue<PlaceTask> clientPlaceTasks = new ArrayDeque<>();
    public final Queue<PlaceTask> prevClientPlaceTasks = new ArrayDeque<>();
    protected byte[] checkResults;
    private byte[] requiredCache;
    private int[] breakOrder;
    private int[] placeOrder;
    private int[] checkOrder;
    private int currentCheckIndex;
    public Vec3 robotPos = null;
    public Vec3 prevRobotPos = null;
    public int leftToBreak = 0;
    public int leftToPlace = 0;

    protected SnapshotBuilder(T tile) {
        this.tile = tile;
    }

    protected abstract Snapshot.BuildingInfo getBuildingInfo();

    private void onBlockChanged(Level level, BlockPos pos, net.minecraft.world.level.block.state.BlockState newState) {
        if (tile.getBuilder() == this && getBuildingInfo() != null && checkResults != null
            && getBuildingInfo().box.contains(pos)) {
            if (check(pos)) {
                afterChecks();
            }
        }
    }

    public void validate() {
        if (!tile.getWorldBC().isClientSide) {
            WorldBlockChangeListeners.add(tile.getWorldBC(), worldEventListener);
        }
    }

    public void invalidate() {
        if (!tile.getWorldBC().isClientSide) {
            WorldBlockChangeListeners.remove(tile.getWorldBC(), worldEventListener);
        }
    }

    protected abstract boolean isAir(BlockPos blockPos);

    protected abstract boolean canPlace(BlockPos blockPos);

    protected abstract boolean isReadyToPlace(BlockPos blockPos);

    protected abstract boolean hasEnoughToPlaceItems(BlockPos blockPos);

    protected abstract List<ItemStack> getToPlaceItems(BlockPos blockPos);

    /** @return true if task done successfully, false otherwise */
    protected abstract boolean doPlaceTask(PlaceTask placeTask);

    /** Executed if break task failed */
    private void cancelBreakTask(BreakTask breakTask) {
        tile.getBattery().addPower(
            Math.min(breakTask.power, tile.getBattery().getCapacity() - tile.getBattery().getStored()), false);
    }

    /** Executed if {@link #doPlaceTask} failed */
    protected void cancelPlaceTask(PlaceTask placeTask) {
        tile.getBattery().addPower(
            Math.min(placeTask.power, tile.getBattery().getCapacity() - tile.getBattery().getStored()), false);
    }

    /** @return true if block in wold is correct (is not to break) according to snapshot, false otherwise */
    protected abstract boolean isBlockCorrect(BlockPos blockPos);

    private ProfilerFiller profiler() {
        return tile.getWorldBC().getProfiler();
    }

    /** @return Pos where flying item should be rendered */
    public Vec3 getPlaceTaskItemPos(PlaceTask placeTask) {
        Vec3 height = Vec3.atLowerCornerOf(placeTask.pos.subtract(tile.getBuilderPos()));
        double progress = placeTask.power * 1D / placeTask.getTarget();
        return Vec3.atLowerCornerOf(tile.getBuilderPos())
            .add(height.scale(progress))
            .add(new Vec3(0, Math.sin(progress * Math.PI) * (Math.abs(height.y) + 1), 0))
            .add(new Vec3(0.5, 1, 0.5));
    }

    public void updateSnapshot() {
        profiler().push("init");
        BlockPos size = getBuildingInfo().box.size();
        checkResults = new byte[size.getX() * size.getY() * size.getZ()];
        Arrays.fill(checkResults, CHECK_RESULT_UNKNOWN);
        requiredCache = new byte[size.getX() * size.getY() * size.getZ()];
        Arrays.fill(requiredCache, REQUIRED_UNKNOWN);
        BlockPos center = getBuildingInfo().box.center();
        BlockPos builderPos = tile.getBuilderPos();
        List<BlockPos> blocks = getBuildingInfo().box.getBlocksInArea();
        breakOrder = blocks.stream()
            .sorted(uniqueBlockPosComparator(Comparator.comparingDouble(blockPos ->
                Math.pow(blockPos.getX() - center.getX(), 2) + Math.pow(blockPos.getZ() - center.getZ(), 2)
                    + 100_000 - Math.abs(blockPos.getY() - builderPos.getY()) * 100_000)))
            .mapToInt(this::posToIndex)
            .toArray();
        placeOrder = blocks.stream()
            .sorted(uniqueBlockPosComparator(Comparator.comparingDouble(blockPos ->
                100_000 - (Math.pow(blockPos.getX() - builderPos.getX(), 2)
                    + Math.pow(blockPos.getZ() - builderPos.getZ(), 2))
                    + Math.abs(blockPos.getY() - builderPos.getY()) * 100_000)))
            .mapToInt(this::posToIndex)
            .toArray();
        checkOrder = blocks.stream()
            .sorted(uniqueBlockPosComparator(Comparator.comparingDouble(blockPos ->
                Math.pow(blockPos.getX() - center.getX(), 2) + Math.pow(blockPos.getY() - center.getY(), 2)
                    + Math.pow(blockPos.getZ() - center.getZ(), 2))))
            .mapToInt(this::posToIndex)
            .toArray();
        profiler().pop();
    }

    /** 1.12's {@code BlockUtil.uniqueBlockPosComparator}: breaks ties by position so no two positions compare equal. */
    private static Comparator<BlockPos> uniqueBlockPosComparator(Comparator<BlockPos> parent) {
        return parent.thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getY)
            .thenComparingInt(BlockPos::getZ);
    }

    public void resourcesChanged() {
        if (requiredCache != null) {
            Arrays.fill(requiredCache, REQUIRED_UNKNOWN);
        }
    }

    public void cancel() {
        breakTasks.forEach(this::cancelBreakTask);
        placeTasks.forEach(this::cancelPlaceTask);
        breakTasks.clear();
        clientBreakTasks.clear();
        prevClientBreakTasks.clear();
        placeTasks.clear();
        clientPlaceTasks.clear();
        prevClientPlaceTasks.clear();
        checkResults = null;
        requiredCache = null;
        breakOrder = null;
        placeOrder = null;
        checkOrder = null;
        currentCheckIndex = 0;
        robotPos = null;
        prevRobotPos = null;
        leftToBreak = 0;
        leftToPlace = 0;
    }

    /** @return true is building is finished, false otherwise */
    public boolean tick() {
        Level level = tile.getWorldBC();
        if (level.isClientSide) {
            prevClientBreakTasks.clear();
            prevClientBreakTasks.addAll(clientBreakTasks);
            clientBreakTasks.clear();
            clientBreakTasks.addAll(breakTasks);
            prevClientPlaceTasks.clear();
            prevClientPlaceTasks.addAll(clientPlaceTasks);
            clientPlaceTasks.clear();
            clientPlaceTasks.addAll(placeTasks);
            prevRobotPos = robotPos;
            if (!breakTasks.isEmpty()) {
                Vec3 newRobotPos = breakTasks.stream()
                    .map(breakTask -> Vec3.atLowerCornerOf(breakTask.pos).add(VecUtil.VEC_HALF))
                    .reduce(Vec3.ZERO, Vec3::add)
                    .scale(1D / breakTasks.size());
                newRobotPos = new Vec3(newRobotPos.x,
                    breakTasks.stream().mapToDouble(breakTask -> breakTask.pos.getY()).max().orElse(newRobotPos.y),
                    newRobotPos.z);
                newRobotPos = newRobotPos.add(new Vec3(0, 3, 0));
                Vec3 oldRobotPos = robotPos;
                robotPos = newRobotPos;
                if (oldRobotPos != null) {
                    robotPos = oldRobotPos.add(newRobotPos.subtract(oldRobotPos).scale(1 / 4D));
                }
            } else {
                robotPos = null;
            }
            return false;
        }

        boolean checkResultsChanged = false;

        profiler().push("scan");
        for (int i = 0; i < CHECKS_PER_TICK; i++) {
            if (check(indexToPos(checkOrder[currentCheckIndex]))) {
                checkResultsChanged = true;
            }
            currentCheckIndex = (currentCheckIndex + 1) % checkOrder.length;
        }
        profiler().pop();

        profiler().push("remove tasks");
        for (Iterator<BreakTask> iterator = breakTasks.iterator(); iterator.hasNext();) {
            BreakTask breakTask = iterator.next();
            if (checkResults[posToIndex(breakTask.pos)] == CHECK_RESULT_CORRECT) {
                iterator.remove();
                cancelBreakTask(breakTask);
            }
        }
        for (Iterator<PlaceTask> iterator = placeTasks.iterator(); iterator.hasNext();) {
            PlaceTask placeTask = iterator.next();
            if (checkResults[posToIndex(placeTask.pos)] == CHECK_RESULT_CORRECT) {
                iterator.remove();
                cancelPlaceTask(placeTask);
            }
        }
        profiler().pop();

        boolean isDone = true;

        profiler().push("add tasks");
        if (tile.canExcavate()) {
            Set<Integer> breakTasksIndexes = breakTasks.stream()
                .map(breakTask -> posToIndex(breakTask.pos))
                .collect(Collectors.toSet());
            int[] blocks = Arrays.stream(breakOrder)
                .filter(i -> checkResults[i] == CHECK_RESULT_TO_BREAK && !breakTasksIndexes.contains(i))
                .toArray();
            leftToBreak = blocks.length;
            if (blocks.length != 0) {
                isDone = false;
            }
            Arrays.stream(blocks)
                .mapToObj(this::indexToPos)
                .filter(blockPos -> BlockUtil.getFluidWithFlowing(level, blockPos) == null)
                .map(blockPos -> new BreakTask(blockPos, 0))
                .limit(MAX_QUEUE_SIZE - breakTasks.size())
                .forEach(breakTasks::add);
        } else {
            leftToBreak = 0;
        }
        {
            Set<Integer> placeTasksIndexes = placeTasks.stream()
                .map(placeTask -> posToIndex(placeTask.pos))
                .collect(Collectors.toSet());
            int[] blocks = Arrays.stream(placeOrder)
                .filter(i -> checkResults[i] == CHECK_RESULT_TO_PLACE && !placeTasksIndexes.contains(i))
                .toArray();
            leftToPlace = blocks.length;
            if (!tile.canExcavate() || breakTasks.isEmpty()) {
                if (blocks.length != 0) {
                    isDone = false;
                }
                Arrays.stream(blocks)
                    .filter(i -> {
                        if (requiredCache[i] != REQUIRED_UNKNOWN) {
                            return requiredCache[i] == REQUIRED_TRUE;
                        }
                        boolean has = hasEnoughToPlaceItems(indexToPos(i));
                        requiredCache[i] = has ? REQUIRED_TRUE : REQUIRED_FALSE;
                        return has;
                    })
                    .mapToObj(this::indexToPos)
                    .filter(this::isReadyToPlace)
                    .limit(MAX_QUEUE_SIZE - placeTasks.size())
                    .filter(this::canPlace)
                    .map(blockPos -> new PlaceTask(blockPos, getToPlaceItems(blockPos), 0))
                    .filter(placeTask -> placeTask.items != null)
                    .forEach(placeTasks::add);
            }
        }
        profiler().pop();

        profiler().push("do tasks");
        long max = Math.min((long) (MAX_POWER_PER_TICK * (double) (tile.getBattery().getStored()
            + MAX_POWER_PER_TICK / 10) / (tile.getBattery().getCapacity() * 2)), MAX_POWER_PER_TICK);
        if (!breakTasks.isEmpty()) {
            for (Iterator<BreakTask> iterator = breakTasks.iterator(); iterator.hasNext();) {
                BreakTask breakTask = iterator.next();
                if (breakTask.isImpossible()) {
                    continue;
                }
                long target = breakTask.getTarget();
                breakTask.power += tile.getBattery().extractPower(0,
                    Math.min(target - breakTask.power, max / breakTasks.size()));
                if (breakTask.power >= target) {
                    level.destroyBlockProgress(breakTask.pos.hashCode(), breakTask.pos, -1);
                    Optional<List<ItemStack>> stacks = BlockUtil.breakBlockAndGetDrops((ServerLevel) level,
                        breakTask.pos, new ItemStack(Items.DIAMOND_PICKAXE), tile.getOwner());
                    if (stacks.isEmpty()) {
                        cancelBreakTask(breakTask);
                    }
                    if (check(breakTask.pos)) {
                        checkResultsChanged = true;
                    }
                    iterator.remove();
                } else {
                    level.destroyBlockProgress(breakTask.pos.hashCode(), breakTask.pos,
                        (int) ((breakTask.power * 9) / target));
                }
            }
        }
        if (!placeTasks.isEmpty()) {
            for (Iterator<PlaceTask> iterator = placeTasks.iterator(); iterator.hasNext();) {
                PlaceTask placeTask = iterator.next();
                long target = placeTask.getTarget();
                placeTask.power += tile.getBattery().extractPower(0,
                    Math.min(target - placeTask.power, max / placeTasks.size()));
                if (placeTask.power >= target) {
                    if (!doPlaceTask(placeTask)) {
                        cancelPlaceTask(placeTask);
                    }
                    if (check(placeTask.pos)) {
                        checkResultsChanged = true;
                    }
                    iterator.remove();
                }
            }
        }
        profiler().pop();

        if (checkResultsChanged) {
            afterChecks();
        }
        return isDone;
    }

    protected int posToIndex(BlockPos blockPos) {
        return getBuildingInfo().getSnapshot().posToIndex(getBuildingInfo().fromWorld(blockPos));
    }

    protected BlockPos indexToPos(int i) {
        return getBuildingInfo().toWorld(getBuildingInfo().getSnapshot().indexToPos(i));
    }

    /** @return true if changed, false otherwise */
    protected boolean check(BlockPos blockPos) {
        int i = posToIndex(blockPos);
        byte prev = checkResults[i];
        if (isAir(blockPos)) {
            if (tile.getWorldBC().isEmptyBlock(blockPos)) {
                checkResults[i] = CHECK_RESULT_CORRECT;
            } else {
                checkResults[i] = CHECK_RESULT_TO_BREAK;
            }
        } else {
            if (isBlockCorrect(blockPos)) {
                checkResults[i] = CHECK_RESULT_CORRECT;
            } else if (canPlace(blockPos)) {
                checkResults[i] = CHECK_RESULT_TO_PLACE;
            } else {
                checkResults[i] = CHECK_RESULT_TO_BREAK;
            }
        }
        return prev != checkResults[i];
    }

    protected void afterChecks() {}

    public void writeToByteBuf(PacketBufferBC buffer) {
        buffer.writeInt(breakTasks.size());
        breakTasks.forEach(breakTask -> breakTask.writePayload(buffer));
        buffer.writeInt(placeTasks.size());
        placeTasks.forEach(placeTask -> placeTask.writePayload(buffer));
        buffer.writeInt(leftToBreak);
        buffer.writeInt(leftToPlace);
    }

    public void readFromByteBuf(PacketBufferBC buffer) {
        breakTasks.clear();
        int breakCount = buffer.readInt();
        for (int i = 0; i < breakCount; i++) {
            breakTasks.add(new BreakTask(buffer));
        }
        placeTasks.clear();
        int placeCount = buffer.readInt();
        for (int i = 0; i < placeCount; i++) {
            placeTasks.add(new PlaceTask(buffer));
        }
        leftToBreak = buffer.readInt();
        leftToPlace = buffer.readInt();
    }

    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        nbt.putByteArray("checkResults", checkResults);
        ListTag breakList = new ListTag();
        breakTasks.forEach(task -> breakList.add(task.writeToNBT()));
        nbt.put("breakTasks", breakList);
        ListTag placeList = new ListTag();
        placeTasks.forEach(task -> placeList.add(task.writeToNBT(registries)));
        nbt.put("placeTasks", placeList);
        nbt.putInt("currentCheckIndex", currentCheckIndex);
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt, HolderLookup.Provider registries) {
        updateSnapshot();
        byte[] saved = nbt.getByteArray("checkResults");
        if (saved.length == checkResults.length) {
            checkResults = saved;
        }
        breakTasks.clear();
        ListTag breakList = nbt.getList("breakTasks", Tag.TAG_COMPOUND);
        for (int i = 0; i < breakList.size(); i++) {
            breakTasks.add(new BreakTask(breakList.getCompound(i)));
        }
        placeTasks.clear();
        ListTag placeList = nbt.getList("placeTasks", Tag.TAG_COMPOUND);
        for (int i = 0; i < placeList.size(); i++) {
            placeTasks.add(new PlaceTask(placeList.getCompound(i), registries));
        }
        currentCheckIndex = nbt.getInt("currentCheckIndex");
        if (checkOrder.length > 0) {
            currentCheckIndex = Math.floorMod(currentCheckIndex, checkOrder.length);
        }
    }

    public class BreakTask {
        public final BlockPos pos;
        public long power;

        public BreakTask(BlockPos pos, long power) {
            this.pos = pos;
            this.power = power;
        }

        public BreakTask(PacketBufferBC buffer) {
            pos = buffer.readBlockPos();
            power = buffer.readLong();
        }

        public BreakTask(CompoundTag nbt) {
            pos = NbtUtils.readBlockPos(nbt, "pos").orElse(BlockPos.ZERO);
            power = nbt.getLong("power");
        }

        public boolean isImpossible() {
            return BlockUtil.isUnbreakableBlock(tile.getWorldBC(), pos, tile.getOwner());
        }

        public long getTarget() {
            return BlockUtil.computeBlockBreakPower(tile.getWorldBC(), pos);
        }

        public void writePayload(PacketBufferBC buffer) {
            buffer.writeBlockPos(pos);
            buffer.writeLong(power);
        }

        public CompoundTag writeToNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.put("pos", NbtUtils.writeBlockPos(pos));
            nbt.putLong("power", power);
            return nbt;
        }
    }

    public class PlaceTask {
        public final BlockPos pos;
        public final List<ItemStack> items;
        public long power;

        public PlaceTask(BlockPos pos, List<ItemStack> items, long power) {
            this.pos = pos;
            this.items = items == null ? null : ImmutableList.copyOf(items);
            this.power = power;
        }

        public PlaceTask(PacketBufferBC buffer) {
            pos = buffer.readBlockPos();
            int count = buffer.readInt();
            List<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                list.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
            }
            items = list;
            power = buffer.readLong();
        }

        public PlaceTask(CompoundTag nbt, HolderLookup.Provider registries) {
            pos = NbtUtils.readBlockPos(nbt, "pos").orElse(BlockPos.ZERO);
            List<ItemStack> list = new ArrayList<>();
            ListTag itemList = nbt.getList("items", Tag.TAG_COMPOUND);
            for (int i = 0; i < itemList.size(); i++) {
                list.add(ItemStack.parseOptional(registries, itemList.getCompound(i)));
            }
            items = ImmutableList.copyOf(list);
            power = nbt.getLong("power");
        }

        public long getTarget() {
            return (long) (Math.sqrt(pos.distSqr(tile.getBuilderPos())) * 10 * MjAPI.MJ);
        }

        public void writePayload(PacketBufferBC buffer) {
            buffer.writeBlockPos(pos);
            buffer.writeInt(items.size());
            items.forEach(stack -> ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack));
            buffer.writeLong(power);
        }

        public CompoundTag writeToNBT(HolderLookup.Provider registries) {
            CompoundTag nbt = new CompoundTag();
            nbt.put("pos", NbtUtils.writeBlockPos(pos));
            ListTag itemList = new ListTag();
            items.forEach(stack -> itemList.add(stack.saveOptional(registries)));
            nbt.put("items", itemList);
            nbt.putLong("power", power);
            return nbt;
        }
    }
}
