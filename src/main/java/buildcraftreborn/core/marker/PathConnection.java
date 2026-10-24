/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.marker;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;

import buildcraftreborn.lib.marker.MarkerConnection;
import buildcraftreborn.lib.marker.MarkerSubCache;

/** A path of path markers, in order. It can loop back to its start. */
public class PathConnection extends MarkerConnection<PathConnection> {
    private final Deque<BlockPos> positions = new LinkedList<>();
    private boolean loop = false;

    public static boolean tryCreateConnection(PathSubCache subCache, BlockPos from, BlockPos to) {
        PathConnection connection = new PathConnection(subCache);
        connection.positions.add(from);
        connection.positions.add(to);
        subCache.addConnection(connection);
        return true;
    }

    public PathConnection(MarkerSubCache<PathConnection> subCache) {
        super(subCache);
    }

    public PathConnection(PathSubCache subCache, List<BlockPos> positions) {
        super(subCache);
        for (BlockPos p : positions) {
            if (p.equals(this.positions.peekFirst())) {
                loop = true;
                break;
            } else {
                this.positions.addLast(p);
            }
        }
    }

    public boolean isLoop() {
        return loop;
    }

    @Override
    public void removeMarker(BlockPos pos) {
        if (positions.isEmpty()) {
            return;
        }
        if (positions.getFirst().equals(pos)) {
            positions.removeFirst();
            loop = false;
            if (positions.size() < 2) {
                positions.clear();
            }
            subCache.refreshConnection(this);
        } else if (positions.getLast().equals(pos)) {
            positions.removeLast();
            loop = false;
            if (positions.size() < 2) {
                positions.clear();
            }
            subCache.refreshConnection(this);
        } else if (positions.contains(pos)) {
            List<BlockPos> a = new ArrayList<>();
            List<BlockPos> b = new ArrayList<>();
            boolean hasReached = false;
            for (BlockPos p : positions) {
                if (p.equals(pos)) {
                    hasReached = true;
                } else if (hasReached) {
                    b.add(p);
                } else {
                    a.add(p);
                }
            }
            loop = false;
            PathConnection conA = new PathConnection(subCache);
            PathConnection conB = new PathConnection(subCache);
            conA.positions.addAll(a);
            conB.positions.addAll(b);
            positions.clear();
            subCache.destroyConnection(this);
            subCache.addConnection(conA);
            subCache.addConnection(conB);
        }
    }

    public boolean addMarker(BlockPos from, BlockPos toAdd) {
        if (loop) {
            return false;
        }
        boolean contains = positions.contains(toAdd);
        if (positions.getFirst().equals(from)) {
            if (positions.getLast().equals(toAdd)) {
                loop = true;
            } else if (!contains) {
                positions.addFirst(toAdd);
            } else {
                return false;
            }
            subCache.refreshConnection(this);
            return true;
        } else if (positions.getLast().equals(from)) {
            if (positions.getFirst().equals(toAdd)) {
                loop = true;
                return true;
            } else if (!contains) {
                positions.addLast(toAdd);
            } else {
                return false;
            }
            subCache.refreshConnection(this);
            return true;
        } else {
            return false;
        }
    }

    public boolean canAddMarker(BlockPos from, BlockPos toAdd) {
        if (loop) {
            return false;
        }
        boolean contains = positions.contains(toAdd);
        if (positions.getFirst().equals(from)) {
            if (contains) {
                return positions.getLast().equals(toAdd);
            } else {
                return true;
            }
        } else if (positions.getLast().equals(from)) {
            if (contains) {
                return positions.getLast().equals(toAdd);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public boolean mergeWith(PathConnection conTo, BlockPos from, BlockPos to) {
        if (loop || conTo.loop) {
            return false;
        } else if (conTo == this) {
            if (positions.size() <= 2) {
                return false;
            }
            if (positions.getFirst().equals(to) && positions.getLast().equals(from)) {
                loop = true;
                subCache.refreshConnection(this);
                return true;
            } else {
                return false;
            }
        } else if (positions.getLast().equals(from) && conTo.positions.getFirst().equals(to)) {
            subCache.destroyConnection(conTo);
            positions.addAll(conTo.positions);
            subCache.refreshConnection(this);
            return true;
        } else {
            return false;
        }
    }

    public boolean canMergeWith(PathConnection conTo, BlockPos from, BlockPos to) {
        if (loop || conTo.loop) {
            return false;
        } else if (conTo == this) {
            return positions.size() > 2 && positions.getFirst().equals(to) && positions.getLast().equals(from);
        } else {
            return positions.getLast().equals(from) && conTo.positions.getFirst().equals(to);
        }
    }

    @Override
    public ImmutableList<BlockPos> getMarkerPositions() {
        if (loop && !positions.isEmpty()) {
            ImmutableList.Builder<BlockPos> list = ImmutableList.builder();
            list.addAll(positions);
            list.add(positions.getFirst());
            return list.build();
        }
        return ImmutableList.copyOf(positions);
    }

    /** The markers in order, without repeating the first one for loops. */
    public ImmutableList<BlockPos> getPathPositions() {
        return ImmutableList.copyOf(positions);
    }

    public void reverseDirection() {
        Deque<BlockPos> list = new LinkedList<>();
        while (!positions.isEmpty()) {
            list.addFirst(positions.removeFirst());
        }
        positions.clear();
        positions.addAll(list);
        subCache.refreshConnection(this);
    }
}
