/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/** Block position helpers. Only the parts the ported code uses so far; the rest of 1.12's {@code PositionUtil} (paths,
 * arcs, rotations) comes with the modules that need it. */
public final class PositionUtil {
    private PositionUtil() {}

    /** @return The exact direction from the first position to the second. Returns null if more than one axis value is
     *         different, or they are the same position. */
    @Nullable
    public static Direction getDirectFacingOffset(BlockPos from, BlockPos to) {
        BlockPos diff = to.subtract(from);
        boolean x = diff.getX() != 0;
        boolean y = diff.getY() != 0;
        boolean z = diff.getZ() != 0;
        if (x && y || x && z || y && z) return null;
        if (x) return diff.getX() > 0 ? Direction.EAST : Direction.WEST;
        if (y) return diff.getY() > 0 ? Direction.UP : Direction.DOWN;
        if (z) return diff.getZ() > 0 ? Direction.SOUTH : Direction.NORTH;
        return null;
    }

    public static Set<BlockPos> getCorners(@Nullable BlockPos min, @Nullable BlockPos max) {
        if (min == null || max == null) return ImmutableSet.of();
        if (min.equals(max)) return ImmutableSet.of(min);
        ImmutableSet.Builder<BlockPos> set = ImmutableSet.builder();
        set.add(min);
        set.add(new BlockPos(max.getX(), min.getY(), min.getZ()));
        set.add(new BlockPos(min.getX(), max.getY(), min.getZ()));
        set.add(new BlockPos(max.getX(), max.getY(), min.getZ()));
        set.add(new BlockPos(min.getX(), min.getY(), max.getZ()));
        set.add(new BlockPos(max.getX(), min.getY(), max.getZ()));
        set.add(new BlockPos(min.getX(), max.getY(), max.getZ()));
        set.add(max);
        return set.build();
    }

    private static int getBoxAxisCount(@Nullable BlockPos min, @Nullable BlockPos max, @Nullable BlockPos pos) {
        if (min == null || max == null || pos == null) {
            return 0;
        }
        int same = 0;

        int x = pos.getX();
        if (min.getX() == x || max.getX() == x) {
            same++;
        } else if (min.getX() > x || max.getX() < x) {
            return 0;
        }

        int y = pos.getY();
        if (min.getY() == y || max.getY() == y) {
            same++;
        } else if (min.getY() > y || max.getY() < y) {
            return 0;
        }

        int z = pos.getZ();
        if (min.getZ() == z || max.getZ() == z) {
            same++;
        } else if (min.getZ() > z || max.getZ() < z) {
            return 0;
        }
        return same;
    }

    /** @return True if the position is a corner of the box given by min and max. */
    public static boolean isCorner(BlockPos min, BlockPos max, BlockPos pos) {
        return getBoxAxisCount(min, max, pos) == 3;
    }

    /** @return True if the position is on one of the edges of the box given by min and max. */
    public static boolean isOnEdge(BlockPos min, BlockPos max, BlockPos pos) {
        return getBoxAxisCount(min, max, pos) >= 2;
    }

    /** @return True if the position is on one of the faces of the box given by min and max. */
    public static boolean isOnFace(BlockPos min, BlockPos max, BlockPos pos) {
        return getBoxAxisCount(min, max, pos) >= 1;
    }

    public static boolean isNextTo(BlockPos one, BlockPos two) {
        BlockPos diff = one.subtract(two);
        boolean x = diff.getX() == 1 || diff.getX() == -1;
        boolean y = diff.getY() == 1 || diff.getY() == -1;
        if (x && y) return false;
        boolean z = diff.getZ() == 1 || diff.getZ() == -1;
        if (y && z) return false;
        return x != z;
    }

    /** Finds the point on the line that is closest to the ray from start along direction. */
    public static LineSkewResult findLineSkewPoint(Line line, Vec3 start, Vec3 direction) {
        double ia = 0, ib = 1;
        double da = 0, db = 0;
        double id = 0.5;
        Vec3 va, vb;

        Vec3 best = null;
        for (int i = 0; i < 10; i++) {
            Vec3 a = line.interpolate(ia);
            Vec3 b = line.interpolate(ib);
            va = closestPointOnLineToPoint(a, start, direction);
            vb = closestPointOnLineToPoint(b, start, direction);
            da = a.distanceToSqr(va);
            db = b.distanceToSqr(vb);
            if (da < db) {
                // We work out the square root at the end to get the actual distance
                best = a;
                ib -= id;
            } else /* if (db < da) */ {
                // We work out the square root at the end to get the actual distance
                best = b;
                ia += id;
            }
            id /= 2.0;
        }
        return new LineSkewResult(best, Math.sqrt(Math.min(da, db)));
    }

    public record LineSkewResult(Vec3 closestPos, double distFromLine) {}

    public static Vec3 closestPointOnLineToPoint(Vec3 point, Vec3 linePoint, Vec3 lineVector) {
        Vec3 v = lineVector.normalize();
        Vec3 p1 = linePoint;
        Vec3 p2 = point;

        // Its maths. Its allowed to deviate from normal naming rules.
        Vec3 p2_minus_p1 = p2.subtract(p1);
        double _dot_v = VecUtil.dot(p2_minus_p1, v);
        Vec3 _scale_v = v.scale(_dot_v);
        return p1.add(_scale_v);
    }

    public record Line(Vec3 start, Vec3 end) {
        public static Line createLongLine(Vec3 start, Vec3 direction) {
            return new Line(start, direction.scale(1024));
        }

        public Vec3 interpolate(double interp) {
            return start.scale(1 - interp).add(end.scale(interp));
        }
    }

    /** Returns a list of all the block positions on the edge of the given box. */
    public static ImmutableList<BlockPos> getAllOnEdge(BlockPos min, BlockPos max) {
        ImmutableList.Builder<BlockPos> list = ImmutableList.builder();
        boolean addX = max.getX() != min.getX();
        boolean addY = max.getY() != min.getY();
        boolean addZ = max.getZ() != min.getZ();
        for (int x = min.getX(); x <= max.getX(); x++) {
            list.add(new BlockPos(x, min.getY(), min.getZ()));
            if (addY) {
                list.add(new BlockPos(x, max.getY(), min.getZ()));
                if (addZ) {
                    list.add(new BlockPos(x, max.getY(), max.getZ()));
                }
            }
            if (addZ) {
                list.add(new BlockPos(x, min.getY(), max.getZ()));
            }
        }
        if (addY) {
            for (int y = min.getY() + 1; y < max.getY(); y++) {
                list.add(new BlockPos(min.getX(), y, min.getZ()));
                if (addX) {
                    list.add(new BlockPos(max.getX(), y, min.getZ()));
                    if (addZ) {
                        list.add(new BlockPos(max.getX(), y, max.getZ()));
                    }
                }
                if (addZ) {
                    list.add(new BlockPos(min.getX(), y, max.getZ()));
                }
            }
        }
        if (addZ) {
            for (int z = min.getZ() + 1; z < max.getZ(); z++) {
                list.add(new BlockPos(min.getX(), min.getY(), z));
                if (addX) {
                    list.add(new BlockPos(max.getX(), min.getY(), z));
                    if (addY) {
                        list.add(new BlockPos(max.getX(), max.getY(), z));
                    }
                }
                if (addY) {
                    list.add(new BlockPos(min.getX(), max.getY(), z));
                }
            }
        }
        return list.build();
    }

    /** @return The size of the list returned by {@link #getAllOnEdge(BlockPos, BlockPos)}. */
    public static int getCountOnEdge(BlockPos min, BlockPos max) {
        return getAllOnEdge(min, max).size();
    }

    public static BlockPos randomBlockPos(RandomSource rand, BlockPos min, BlockPos max) {
        return new BlockPos(min.getX() + rand.nextInt(max.getX() - min.getX()),
            min.getY() + rand.nextInt(max.getY() - min.getY()), min.getZ() + rand.nextInt(max.getZ() - min.getZ()));
    }

    /** @return Every block on the straight line from {@code from} to {@code to}, excluding {@code from}. */
    public static ImmutableList<BlockPos> getAllOnPath(BlockPos from, BlockPos to) {
        ImmutableList.Builder<BlockPos> interp = ImmutableList.builder();
        forAllOnPath(from, to, interp::add);
        return interp.build();
    }

    public static void forAllOnPath(BlockPos from, BlockPos to, java.util.function.Consumer<BlockPos> iter) {
        final BlockPos difference = to.subtract(from);
        final int ax = Math.abs(difference.getX());
        final int ay = Math.abs(difference.getY());
        final int az = Math.abs(difference.getZ());

        int count = ax + ay + az;
        BlockPos current = from;
        final int ddx = difference.getX() > 0 ? 1 : -1;
        final int ddy = difference.getY() > 0 ? 1 : -1;
        final int ddz = difference.getZ() > 0 ? 1 : -1;

        // start from 1/2 in a block
        // (as we want to compare to the centre of blocks rather than the lower corner)
        int dx = count / 2;
        int dy = count / 2;
        int dz = count / 2;

        for (int j = 0; j < count; j++) {
            dx += ax;
            dy += ay;
            dz += az;
            boolean changed = false;
            if (dx >= count) {
                changed = true;
                dx -= count;
                current = current.offset(ddx, 0, 0);
            }
            if (dy >= count) {
                changed = true;
                dy -= count;
                current = current.offset(0, ddy, 0);
            }
            if (dz >= count) {
                changed = true;
                dz -= count;
                current = current.offset(0, 0, ddz);
            }
            if (changed) {
                iter.accept(current);
            }
        }
    }

    // 2D lines, for the filler's 2D shape patterns

    public static void forAllOnPath2d(int a1, int b1, int a2, int b2, PathIterator2d iter) {
        // Find the smallest number 'm' and smallest number 'o'
        // such that a * m + o = b
        // then draw a straight line (1, m)

        // First swap a with b if b is smaller than a

        int diff_a = a2 - a1;
        int diff_b = b2 - b1;

        int max_a = Math.abs(diff_a);
        int max_b = Math.abs(diff_b);

        int size_a = max_a + 1;
        int size_b = max_b + 1;

        int mult_a = diff_a > 0 ? 1 : -1;
        int mult_b = diff_b > 0 ? 1 : -1;

        boolean reverse = false;
        int multiplier;
        int offset;

        if (size_a == size_b) {
            multiplier = 1;
            offset = 0;
        } else {
            if (size_a > size_b) {
                int temp = size_a;
                size_a = size_b;
                size_b = temp;
                reverse = true;
            }
            multiplier = size_b / size_a;
            offset = size_b % size_a;
        }

        // Offset is distributed from the start of the line
        // Which is wrong atm -- need to distribute it across the line

        int normalLength = multiplier;
        int currentOffsetA = 0;
        int currentOffsetB = 0;
        int count = size_a;
        for (int i = 0; i < count; i++) {
            int length = normalLength;
            if (i < offset) {
                length++;
            }
            for (int l = 0; l < length; l++) {
                if (reverse) {
                    iter.iterate(a1 + mult_a * currentOffsetB, b1 + mult_b * currentOffsetA);
                } else {
                    iter.iterate(a1 + mult_a * currentOffsetA, b1 + mult_b * currentOffsetB);
                }
                currentOffsetB++;
            }
            currentOffsetA++;
        }
    }

    @FunctionalInterface
    public interface PathIterator2d {
        void iterate(int a, int b);
    }
}
