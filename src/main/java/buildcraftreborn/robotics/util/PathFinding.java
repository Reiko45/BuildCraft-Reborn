/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraftreborn.api.core.BuildCraftAPI;
import buildcraftreborn.api.core.IWorldProperty;

/** A* search for a flying robot, through blocks that are free for a path (moving diagonally only when the blocks
 * beside the diagonal are free too).
 * <p>
 * 1.21: the open nodes are kept in a priority queue rather than searched in full every step, and each step does a
 * tenth of 7.1's work (see {@link IterableAlgorithmRunner}). */
public class PathFinding implements IIterableAlgorithm {

    public static int PATH_ITERATIONS = 100;

    private final Level world;
    private final BlockPos start;
    private final BlockPos end;
    private double maxDistanceToEndSq = 0;
    private float maxTotalDistanceSq = 0;

    private final HashMap<BlockPos, Node> openList = new HashMap<>();
    private final PriorityQueue<Node> openQueue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.totalWeight));
    private final HashMap<BlockPos, Node> closedList = new HashMap<>();

    private Node nextIteration;

    private LinkedList<BlockPos> result;

    private boolean endReached = false;

    public PathFinding(Level iWorld, BlockPos iStart, BlockPos iEnd) {
        world = iWorld;
        start = iStart;
        end = iEnd;

        Node startNode = new Node();
        startNode.parent = null;
        startNode.movementCost = 0;
        startNode.destinationCost = distanceSq(start, end);
        startNode.totalWeight = startNode.movementCost + startNode.destinationCost;
        startNode.index = iStart;
        openList.put(start, startNode);
        nextIteration = startNode;
    }

    public PathFinding(Level iWorld, BlockPos iStart, BlockPos iEnd, double iMaxDistanceToEnd) {
        this(iWorld, iStart, iEnd);

        maxDistanceToEndSq = iMaxDistanceToEnd * iMaxDistanceToEnd;
    }

    public PathFinding(Level iWorld, BlockPos iStart, BlockPos iEnd, double iMaxDistanceToEnd,
        float iMaxTotalDistance) {
        this(iWorld, iStart, iEnd, iMaxDistanceToEnd);

        maxTotalDistanceSq = iMaxTotalDistance * iMaxTotalDistance;
    }

    @Override
    public void iterate() {
        iterate(PATH_ITERATIONS);
    }

    public void iterate(int itNumber) {
        for (int i = 0; i < itNumber; ++i) {
            if (nextIteration == null) {
                return;
            }

            if (endReached) {
                result = new LinkedList<>();

                while (nextIteration != null) {
                    result.addFirst(nextIteration.index);
                    nextIteration = nextIteration.parent;
                }

                return;
            } else {
                nextIteration = iterate(nextIteration);
            }
        }
    }

    @Override
    public boolean isDone() {
        return nextIteration == null;
    }

    public LinkedList<BlockPos> getResult() {
        if (result != null) {
            return result;
        } else {
            return new LinkedList<>();
        }
    }

    public BlockPos end() {
        return end;
    }

    @Nullable
    private Node iterate(Node from) {
        openList.remove(from.index);
        closedList.put(from.index, from);

        byte[][][] resultMoves = movements(from);

        for (int dx = -1; dx <= +1; ++dx) {
            for (int dy = -1; dy <= +1; ++dy) {
                for (int dz = -1; dz <= +1; ++dz) {
                    if (resultMoves[dx + 1][dy + 1][dz + 1] == 0) {
                        continue;
                    }

                    Node nextNode = new Node();
                    nextNode.parent = from;
                    nextNode.index = from.index.offset(dx, dy, dz);

                    if (resultMoves[dx + 1][dy + 1][dz + 1] == 2) {
                        endReached = true;
                        return nextNode;
                    }

                    nextNode.movementCost = from.movementCost + distanceSq(nextNode.index, from.index);
                    nextNode.destinationCost = distanceSq(nextNode.index, end);
                    nextNode.totalWeight = nextNode.movementCost + nextNode.destinationCost;

                    if (maxTotalDistanceSq > 0 && nextNode.totalWeight > maxTotalDistanceSq) {
                        if (!closedList.containsKey(nextNode.index)) {
                            closedList.put(nextNode.index, nextNode);
                        }
                        continue;
                    }
                    if (closedList.containsKey(nextNode.index)) {
                        continue;
                    }
                    Node tentative = openList.get(nextNode.index);
                    if (tentative == null || tentative.movementCost >= nextNode.movementCost) {
                        openList.put(nextNode.index, nextNode);
                        openQueue.add(nextNode);
                    }
                }
            }
        }

        return findSmallerWeight();
    }

    /** @return The open node with the smallest weight (skipping ones that were replaced by a cheaper node). */
    @Nullable
    private Node findSmallerWeight() {
        Node found;
        while ((found = openQueue.poll()) != null) {
            if (openList.get(found.index) == found) {
                return found;
            }
        }
        return null;
    }

    private static class Node {
        public Node parent;
        public double movementCost;
        public double destinationCost;
        public double totalWeight;
        public BlockPos index;
    }

    private static double distanceSq(BlockPos i1, BlockPos i2) {
        double dx = (double) i1.getX() - (double) i2.getX();
        double dy = (double) i1.getY() - (double) i2.getY();
        double dz = (double) i1.getZ() - (double) i2.getZ();

        return dx * dx + dy * dy + dz * dz;
    }

    /** @return True if a robot can fly through the block (see the "freepath" world property). */
    public static boolean isFreeForPath(Level world, BlockPos pos) {
        IWorldProperty freePath = BuildCraftAPI.getWorldProperty("freepath");
        return freePath != null ? freePath.get(world, pos) : BuildCraftAPI.isSoftBlock(world, pos);
    }

    private boolean endReached(BlockPos pos) {
        if (maxDistanceToEndSq == 0) {
            return end.equals(pos);
        } else {
            return isFreeForPath(world, pos) && distanceSq(pos, end) <= maxDistanceToEndSq;
        }
    }

    private byte[][][] movements(Node from) {
        byte[][][] resultMoves = new byte[3][3][3];

        for (int dx = -1; dx <= +1; ++dx) {
            for (int dy = -1; dy <= +1; ++dy) {
                for (int dz = -1; dz <= +1; ++dz) {
                    BlockPos pos = from.index.offset(dx, dy, dz);

                    if (world.isOutsideBuildHeight(pos)) {
                        resultMoves[dx + 1][dy + 1][dz + 1] = 0;
                    } else if (endReached(pos)) {
                        resultMoves[dx + 1][dy + 1][dz + 1] = 2;
                    } else if (!isFreeForPath(world, pos)) {
                        resultMoves[dx + 1][dy + 1][dz + 1] = 0;
                    } else {
                        resultMoves[dx + 1][dy + 1][dz + 1] = 1;
                    }
                }
            }
        }

        resultMoves[1][1][1] = 0;

        if (resultMoves[0][1][1] == 0) {
            for (int i = 0; i <= 2; ++i) {
                for (int j = 0; j <= 2; ++j) {
                    resultMoves[0][i][j] = 0;
                }
            }
        }

        if (resultMoves[2][1][1] == 0) {
            for (int i = 0; i <= 2; ++i) {
                for (int j = 0; j <= 2; ++j) {
                    resultMoves[2][i][j] = 0;
                }
            }
        }

        if (resultMoves[1][0][1] == 0) {
            for (int i = 0; i <= 2; ++i) {
                for (int j = 0; j <= 2; ++j) {
                    resultMoves[i][0][j] = 0;
                }
            }
        }

        if (resultMoves[1][2][1] == 0) {
            for (int i = 0; i <= 2; ++i) {
                for (int j = 0; j <= 2; ++j) {
                    resultMoves[i][2][j] = 0;
                }
            }
        }

        if (resultMoves[1][1][0] == 0) {
            for (int i = 0; i <= 2; ++i) {
                for (int j = 0; j <= 2; ++j) {
                    resultMoves[i][j][0] = 0;
                }
            }
        }

        if (resultMoves[1][1][2] == 0) {
            for (int i = 0; i <= 2; ++i) {
                for (int j = 0; j <= 2; ++j) {
                    resultMoves[i][j][2] = 0;
                }
            }
        }

        if (resultMoves[0][0][1] == 0) {
            resultMoves[0][0][0] = 0;
            resultMoves[0][0][2] = 0;
        }

        if (resultMoves[0][2][1] == 0) {
            resultMoves[0][2][0] = 0;
            resultMoves[0][2][2] = 0;
        }

        if (resultMoves[2][0][1] == 0) {
            resultMoves[2][0][0] = 0;
            resultMoves[2][0][2] = 0;
        }

        if (resultMoves[2][2][1] == 0) {
            resultMoves[2][2][0] = 0;
            resultMoves[2][2][2] = 0;
        }

        if (resultMoves[0][1][0] == 0) {
            resultMoves[0][0][0] = 0;
            resultMoves[0][2][0] = 0;
        }

        if (resultMoves[0][1][2] == 0) {
            resultMoves[0][0][2] = 0;
            resultMoves[0][2][2] = 0;
        }

        if (resultMoves[2][1][0] == 0) {
            resultMoves[2][0][0] = 0;
            resultMoves[2][2][0] = 0;
        }

        if (resultMoves[2][1][2] == 0) {
            resultMoves[2][0][2] = 0;
            resultMoves[2][2][2] = 0;
        }

        if (resultMoves[1][0][0] == 0) {
            resultMoves[0][0][0] = 0;
            resultMoves[2][0][0] = 0;
        }

        if (resultMoves[1][0][2] == 0) {
            resultMoves[0][0][2] = 0;
            resultMoves[2][0][2] = 0;
        }

        if (resultMoves[1][2][0] == 0) {
            resultMoves[0][2][0] = 0;
            resultMoves[2][2][0] = 0;
        }

        if (resultMoves[1][2][2] == 0) {
            resultMoves[0][2][2] = 0;
            resultMoves[2][2][2] = 0;
        }

        return resultMoves;
    }
}
