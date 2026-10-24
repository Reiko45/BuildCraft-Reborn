/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

/** Vector helpers. Only the parts the ported code uses so far; 1.12's {@code VecUtil} had many more, and they come
 * with the modules that need them. */
public final class VecUtil {
    public static final BlockPos POS_ONE = new BlockPos(1, 1, 1);
    public static final Vec3 VEC_HALF = new Vec3(0.5, 0.5, 0.5);
    public static final Vec3 VEC_ONE = new Vec3(1, 1, 1);

    private VecUtil() {}

    public static Vec3 offset(Vec3 from, Direction direction, double by) {
        return from.add(direction.getStepX() * by, direction.getStepY() * by, direction.getStepZ() * by);
    }

    public static double dot(Vec3 a, Vec3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static Direction getFacing(Axis axis, boolean positive) {
        return Direction.fromAxisAndDirection(axis, positive ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE);
    }

    public static BlockPos replaceValue(BlockPos old, Axis axis, int with) {
        return new BlockPos(axis == Axis.X ? with : old.getX(), axis == Axis.Y ? with : old.getY(),
            axis == Axis.Z ? with : old.getZ());
    }

    public static Vec3 convertCenter(Vec3i pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static Vec3 convert(Vec3i pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos convertFloor(Vec3 vec) {
        return BlockPos.containing(Math.floor(vec.x), Math.floor(vec.y), Math.floor(vec.z));
    }

    public static BlockPos convertCeiling(Vec3 vec) {
        return BlockPos.containing(Math.ceil(vec.x), Math.ceil(vec.y), Math.ceil(vec.z));
    }

    @Nullable
    public static BlockPos min(@Nullable BlockPos a, @Nullable BlockPos b) {
        if (a == null) return b;
        if (b == null) return a;
        return new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()),
            Math.min(a.getZ(), b.getZ()));
    }

    @Nullable
    public static BlockPos min(@Nullable BlockPos a, @Nullable BlockPos b, @Nullable BlockPos c) {
        return min(min(a, b), c);
    }

    @Nullable
    public static BlockPos max(@Nullable BlockPos a, @Nullable BlockPos b) {
        if (a == null) return b;
        if (b == null) return a;
        return new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()),
            Math.max(a.getZ(), b.getZ()));
    }

    @Nullable
    public static BlockPos max(@Nullable BlockPos a, @Nullable BlockPos b, @Nullable BlockPos c) {
        return max(max(a, b), c);
    }
    public static Vec3 add(Vec3 a, net.minecraft.core.Vec3i b) {
        return a.add(b.getX(), b.getY(), b.getZ());
    }

    public static Vec3 scale(Vec3 vec, double scale) {
        return vec.scale(scale);
    }

    public static BlockPos absolute(BlockPos val) {
        return new BlockPos(Math.abs(val.getX()), Math.abs(val.getY()), Math.abs(val.getZ()));
    }

    public static Vec3 replaceValue(Vec3 old, Axis axis, double with) {
        return new Vec3(
            axis == Axis.X ? with : old.x,
            axis == Axis.Y ? with : old.y,
            axis == Axis.Z ? with : old.z);
    }

    public static double getValue(Vec3 from, Axis axis) {
        return axis == Axis.X ? from.x : axis == Axis.Y ? from.y : from.z;
    }

    public static int getValue(net.minecraft.core.Vec3i from, Axis axis) {
        return axis == Axis.X ? from.getX() : axis == Axis.Y ? from.getY() : from.getZ();
    }

    public static double getValue(Vec3 negative, Vec3 positive, Direction face) {
        return switch (face) {
            case DOWN -> negative.y;
            case UP -> positive.y;
            case NORTH -> negative.z;
            case SOUTH -> positive.z;
            case WEST -> negative.x;
            case EAST -> positive.x;
        };
    }

    public static int getValue(net.minecraft.core.Vec3i negative, net.minecraft.core.Vec3i positive, Direction face) {
        return switch (face) {
            case DOWN -> negative.getY();
            case UP -> positive.getY();
            case NORTH -> negative.getZ();
            case SOUTH -> positive.getZ();
            case WEST -> negative.getX();
            case EAST -> positive.getX();
        };
    }

    @Nullable
    public static Vec3 min(@Nullable Vec3 a, @Nullable Vec3 b) {
        if (a == null) return b;
        if (b == null) return a;
        return new Vec3(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z));
    }

    @Nullable
    public static Vec3 min(@Nullable Vec3 a, @Nullable Vec3 b, @Nullable Vec3 c) {
        return min(min(a, b), c);
    }

    @Nullable
    public static Vec3 max(@Nullable Vec3 a, @Nullable Vec3 b) {
        if (a == null) return b;
        if (b == null) return a;
        return new Vec3(Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z));
    }

    @Nullable
    public static Vec3 max(@Nullable Vec3 a, @Nullable Vec3 b, @Nullable Vec3 c) {
        return max(max(a, b), c);
    }
}
