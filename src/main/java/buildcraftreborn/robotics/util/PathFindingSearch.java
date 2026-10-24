/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.api.core.BuildCraftAPI;
import buildcraftreborn.api.core.IZone;

/** Looks for blocks that match a filter (in the order a block iterator gives), and for a path to them. The first
 * target with a path is reserved, so other robots look for something else. */
public class PathFindingSearch implements IIterableAlgorithm {

    public static final int PATH_ITERATIONS = PathFinding.PATH_ITERATIONS;

    private static final Map<ResourceKey<Level>, Set<BlockPos>> reservations = new HashMap<>();

    private final Level world;
    private final BlockPos start;
    private final List<PathFinding> pathFinders;
    private final IBlockFilter pathFound;
    @Nullable
    private final IZone zone;
    private final float maxDistance;
    private final Iterator<BlockPos> blockIter;

    private final double maxDistanceToEnd;

    public PathFindingSearch(Level iWorld, BlockPos iStart, Iterator<BlockPos> iBlockIter, IBlockFilter iPathFound,
        double iMaxDistanceToEnd, float iMaxDistance, @Nullable IZone iZone) {
        world = iWorld;
        start = iStart;
        pathFound = iPathFound;

        maxDistance = iMaxDistance;
        maxDistanceToEnd = iMaxDistanceToEnd;
        zone = iZone;
        blockIter = iBlockIter;

        pathFinders = new LinkedList<>();
    }

    @Override
    public void iterate() {
        if (pathFinders.size() < 5 && blockIter.hasNext()) {
            iterateSearch(PATH_ITERATIONS * 10);
        }
        iteratePathFind(PATH_ITERATIONS);
    }

    private void iterateSearch(int itNumber) {
        for (int i = 0; i < itNumber; ++i) {
            if (!blockIter.hasNext()) {
                return;
            }

            BlockPos delta = blockIter.next();
            int y = Math.max(start.getY() + delta.getY(), world.getMinBuildHeight());
            BlockPos block = new BlockPos(start.getX() + delta.getX(), y, start.getZ() + delta.getZ());
            if (world.isLoaded(block)) {
                if (isTarget(block)) {
                    pathFinders.add(new PathFinding(world, start, block, maxDistanceToEnd, maxDistance));
                }
            }

            if (pathFinders.size() >= 5) {
                return;
            }
        }
    }

    private boolean isTarget(BlockPos block) {
        if (zone != null && !zone.contains(Vec3.atCenterOf(block))) {
            return false;
        }
        if (!pathFound.matches(world, block)) {
            return false;
        }
        Set<BlockPos> dimReservations = reservations.get(world.dimension());
        if (dimReservations != null && dimReservations.contains(block)) {
            return false;
        }
        for (BlockPos side : new BlockPos[] { block.west(), block.east(), block.north(), block.south(), block.below(),
            block.above() }) {
            if (BuildCraftAPI.isSoftBlock(world, side)) {
                return true;
            }
        }
        return false;
    }

    public void iteratePathFind(int itNumber) {
        for (PathFinding pathFinding : new ArrayList<>(pathFinders)) {
            pathFinding.iterate(itNumber / pathFinders.size());
            if (pathFinding.isDone()) {
                LinkedList<BlockPos> path = pathFinding.getResult();
                if (path != null && path.size() > 0) {
                    if (reserve(pathFinding.end())) {
                        return;
                    }
                }
                pathFinders.remove(pathFinding);
            }
        }
    }

    @Override
    public boolean isDone() {
        for (PathFinding pathFinding : pathFinders) {
            if (pathFinding.isDone()) {
                return true;
            }
        }
        return !blockIter.hasNext();
    }

    public LinkedList<BlockPos> getResult() {
        for (PathFinding pathFinding : pathFinders) {
            if (pathFinding.isDone()) {
                return pathFinding.getResult();
            }
        }
        return new LinkedList<>();
    }

    @Nullable
    public BlockPos getResultTarget() {
        for (PathFinding pathFinding : pathFinders) {
            if (pathFinding.isDone()) {
                return pathFinding.end();
            }
        }
        return null;
    }

    private boolean reserve(BlockPos block) {
        Set<BlockPos> dimReservations = reservations.computeIfAbsent(world.dimension(), k -> new HashSet<>());
        return dimReservations.add(block);
    }

    public void unreserve(BlockPos block) {
        Set<BlockPos> dimReservations = reservations.get(world.dimension());
        if (dimReservations != null) {
            dimReservations.remove(block);
        }
    }

    /** Forgets every reservation (when the server stops). */
    public static void clearReservations() {
        reservations.clear();
    }
}
