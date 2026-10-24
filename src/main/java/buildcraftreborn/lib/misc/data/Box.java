/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.api.core.IAreaProvider;
import buildcraftreborn.api.core.IBox;
import buildcraftreborn.lib.misc.PositionUtil;
import buildcraftreborn.lib.misc.VecUtil;

/** MUTABLE integer variant of AxisAlignedBB, with a few BC-specific methods. (1.12 also kept the client's laser render
 * cache in here; that lives in the laser renderer now.) */
public class Box implements IBox {

    @Nullable
    private BlockPos min, max;

    public Box() {
        reset();
    }

    public Box(BlockPos min, BlockPos max) {
        this();
        this.min = VecUtil.min(min, max);
        this.max = VecUtil.max(min, max);
    }

    public Box(BlockEntity e) {
        this(e.getBlockPos(), e.getBlockPos());
    }

    public void reset() {
        min = null;
        max = null;
    }

    public boolean isInitialized() {
        return min != null && max != null;
    }

    public void extendToEncompassBoth(BlockPos newMin, BlockPos newMax) {
        this.min = VecUtil.min(this.min, newMin, newMax);
        this.max = VecUtil.max(this.max, newMin, newMax);
    }

    public void setMin(@Nullable BlockPos min) {
        if (min == null) return;
        this.min = min;
        this.max = VecUtil.max(min, max);
    }

    public void setMax(@Nullable BlockPos max) {
        if (max == null) return;
        this.min = VecUtil.min(min, max);
        this.max = max;
    }

    public void initialize(IBox box) {
        reset();
        extendToEncompassBoth(box.min(), box.max());
    }

    public void initialize(IAreaProvider a) {
        reset();
        extendToEncompassBoth(a.min(), a.max());
    }

    public void initialize(CompoundTag nbt) {
        reset();
        if (nbt.contains("xMin")) {
            min = new BlockPos(nbt.getInt("xMin"), nbt.getInt("yMin"), nbt.getInt("zMin"));
            max = new BlockPos(nbt.getInt("xMax"), nbt.getInt("yMax"), nbt.getInt("zMax"));
        } else {
            min = NbtUtils.readBlockPos(nbt, "min").orElse(null);
            max = NbtUtils.readBlockPos(nbt, "max").orElse(null);
        }
        if (min != null && max != null) {
            extendToEncompassBoth(min, max);
        }
    }

    public void writeToNBT(CompoundTag nbt) {
        if (min != null) nbt.put("min", NbtUtils.writeBlockPos(min));
        if (max != null) nbt.put("max", NbtUtils.writeBlockPos(max));
    }

    public CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();
        writeToNBT(nbt);
        return nbt;
    }

    public void initializeCenter(BlockPos center, int size) {
        initializeCenter(center, new BlockPos(size, size, size));
    }

    public void initializeCenter(BlockPos center, Vec3i size) {
        extendToEncompassBoth(center.subtract(size), center.offset(size));
    }

    public List<BlockPos> getBlocksInArea() {
        List<BlockPos> blocks = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            blocks.add(pos.immutable());
        }
        return blocks;
    }

    public List<BlockPos> getBlocksOnEdge() {
        return PositionUtil.getAllOnEdge(min, max);
    }

    @Override
    public Box expand(int amount) {
        if (!isInitialized()) return this;
        Vec3i am = new BlockPos(amount, amount, amount);
        setMin(min().subtract(am));
        setMax(max().offset(am));
        return this;
    }

    @Override
    public IBox contract(int amount) {
        return expand(-amount);
    }

    @Override
    public boolean contains(Vec3 p) {
        AABB bb = getBoundingBox();
        if (p.x < bb.minX || p.x >= bb.maxX) return false;
        if (p.y < bb.minY || p.y >= bb.maxY) return false;
        if (p.z < bb.minZ || p.z >= bb.maxZ) return false;
        return true;
    }

    public boolean contains(BlockPos i) {
        return contains(VecUtil.convert(i));
    }

    @Override
    public BlockPos min() {
        return min;
    }

    @Override
    public BlockPos max() {
        return max;
    }

    @Override
    public BlockPos size() {
        if (!isInitialized()) return BlockPos.ZERO;
        return max.subtract(min).offset(VecUtil.POS_ONE);
    }

    public BlockPos center() {
        return BlockPos.containing(centerExact());
    }

    public Vec3 centerExact() {
        return VecUtil.convert(size()).scale(0.5).add(VecUtil.convert(min()));
    }

    @Override
    public String toString() {
        return "Box[min = " + min + ", max = " + max + "]";
    }

    public Box extendToEncompass(@Nullable IBox toBeContained) {
        if (toBeContained == null) {
            return this;
        }
        extendToEncompassBoth(toBeContained.min(), toBeContained.max());
        return this;
    }

    /** IMPORTANT: Use {@link #contains(Vec3)} instead of the returned {@link AABB#contains(Vec3)} as the logic is
     * different! */
    public AABB getBoundingBox() {
        return new AABB(VecUtil.convert(min), VecUtil.convert(max.offset(VecUtil.POS_ONE)));
    }

    public Box extendToEncompass(Vec3 toBeContained) {
        setMin(VecUtil.min(min, VecUtil.convertFloor(toBeContained)));
        setMax(VecUtil.max(max, VecUtil.convertCeiling(toBeContained)));
        return this;
    }

    public Box extendToEncompass(BlockPos toBeContained) {
        setMin(VecUtil.min(min, toBeContained));
        setMax(VecUtil.max(max, toBeContained));
        return this;
    }

    @Override
    public double distanceTo(BlockPos index) {
        return Math.sqrt(distanceToSquared(index));
    }

    @Override
    public double distanceToSquared(BlockPos index) {
        return closestInsideTo(index).distSqr(index);
    }

    public BlockPos closestInsideTo(BlockPos toTest) {
        return VecUtil.max(min, VecUtil.min(max, toTest));
    }

    @Override
    public BlockPos getRandomBlockPos(RandomSource rand) {
        return PositionUtil.randomBlockPos(rand, min, max.offset(1, 1, 1));
    }

    /** Delegate for {@link PositionUtil#isCorner(BlockPos, BlockPos, BlockPos)} */
    public boolean isCorner(BlockPos pos) {
        return PositionUtil.isCorner(min, max, pos);
    }

    /** Delegate for {@link PositionUtil#isOnEdge(BlockPos, BlockPos, BlockPos)} */
    public boolean isOnEdge(BlockPos pos) {
        return PositionUtil.isOnEdge(min, max, pos);
    }

    /** Delegate for {@link PositionUtil#isOnFace(BlockPos, BlockPos, BlockPos)} */
    public boolean isOnFace(BlockPos pos) {
        return PositionUtil.isOnFace(min, max, pos);
    }

    public boolean doesIntersectWith(Box box) {
        if (isInitialized() && box.isInitialized()) {
            return min.getX() <= box.max.getX() && max.getX() >= box.min.getX()//
                && min.getY() <= box.max.getY() && max.getY() >= box.min.getY() //
                && min.getZ() <= box.max.getZ() && max.getZ() >= box.min.getZ();
        }
        return false;
    }

    /** @return The intersection box (if these two boxes are intersecting) or null if they were not. */
    @Nullable
    public Box getIntersect(Box box) {
        if (doesIntersectWith(box)) {
            BlockPos min2 = VecUtil.max(min, box.min);
            BlockPos max2 = VecUtil.min(max, box.max);
            return new Box(min2, max2);
        }
        return null;
    }

    /** @return The size of the list returned by {@link #getBlocksOnEdge()}. */
    public int getBlocksOnEdgeCount() {
        return PositionUtil.getCountOnEdge(min(), max());
    }

    public void readData(FriendlyByteBuf stream) {
        if (stream.readBoolean()) {
            min = stream.readBlockPos();
            max = stream.readBlockPos();
        } else {
            min = null;
            max = null;
        }
    }

    public void writeData(FriendlyByteBuf stream) {
        boolean isValid = isInitialized();
        stream.writeBoolean(isValid);
        if (isValid) {
            stream.writeBlockPos(min);
            stream.writeBlockPos(max);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        Box box = (Box) obj;
        return Objects.equals(min, box.min) && Objects.equals(max, box.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }
}
