/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import java.util.LinkedList;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.robotics.util.IterableAlgorithmRunner;
import buildcraftreborn.robotics.util.PathFinding;

/** Finds a path to a block (or follows a given path) and flies along it. */
public class AIRobotGotoBlock extends AIRobotGoto {

    @Nullable
    private PathFinding pathSearch;
    @Nullable
    private IterableAlgorithmRunner pathSearchJob;
    @Nullable
    private LinkedList<BlockPos> path;
    private double prevDistance = Double.MAX_VALUE;
    private BlockPos finalPos;
    private double maxDistance = 0;
    @Nullable
    private BlockPos lastBlockInPath;
    private boolean loadedFromNBT;
    private boolean skipLastIfNotSoft;
    @Nullable
    private BlockPos lastBlock;

    public AIRobotGotoBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotGotoBlock(EntityRobotBase robot, BlockPos pos) {
        this(robot);
        finalPos = pos;
    }

    public AIRobotGotoBlock(EntityRobotBase robot, BlockPos pos, double iMaxDistance) {
        this(robot, pos);
        maxDistance = iMaxDistance;
    }

    public AIRobotGotoBlock(EntityRobotBase robot, LinkedList<BlockPos> iPath) {
        this(robot);
        path = iPath;
        finalPos = path.getLast();
        setNextInPath();
    }

    public AIRobotGotoBlock(EntityRobotBase robot, BlockPos pos, boolean iSkipLastIfNotSoft) {
        this(robot);
        finalPos = pos;
        skipLastIfNotSoft = iSkipLastIfNotSoft;
    }

    @Override
    public void start() {
        robot.undock();
    }

    @Override
    public void update() {
        if (loadedFromNBT) {
            // Prevent a race condition with terminate() being called in
            // setNextInPath.
            setNextInPath();
            loadedFromNBT = false;
        }

        if (path == null && pathSearch == null) {
            if (finalPos == null) {
                setSuccess(false);
                terminate();
                return;
            }
            pathSearch = new PathFinding(robot.level(), robot.blockPosition(), finalPos, maxDistance);

            pathSearchJob = new IterableAlgorithmRunner(pathSearch, 50);
            pathSearchJob.start();
        } else if (path != null && next != null) {
            double distance = robot.position().distanceTo(next);

            if (!robot.isMoving() || distance > prevDistance) {
                if (path.size() > 0) {
                    path.removeFirst();
                }

                setNextInPath();
            } else {
                prevDistance = distance;
            }
        } else if (pathSearchJob != null) {
            if (pathSearchJob.isDone()) {
                path = pathSearch.getResult();

                if (path.size() == 0) {
                    setSuccess(false);
                    terminate();
                    return;
                }

                lastBlockInPath = path.getLast();

                setNextInPath();
            }
        }

        if (path != null && path.size() == 0) {
            robot.setDeltaMovement(Vec3.ZERO);

            if (lastBlockInPath != null) {
                robot.setPos(lastBlockInPath.getX() + 0.5, lastBlockInPath.getY() + 0.5, lastBlockInPath.getZ() + 0.5);
            }
            terminate();
        }
    }

    private void setNextInPath() {
        if (path != null && path.size() > 0) {

            boolean isFirst = prevDistance == Double.MAX_VALUE;

            BlockPos nextPos = path.getFirst();
            if (isFirst || PathFinding.isFreeForPath(robot.level(), nextPos)) {
                lastBlock = nextPos;
                setDestination(robot, Vec3.atCenterOf(nextPos));
                prevDistance = Double.MAX_VALUE;
                robot.aimItemAt(nextPos);
            } else if (skipLastIfNotSoft && path.size() == 1) {
                lastBlockInPath = lastBlock;
                path.clear();
            } else {
                // Path invalid!
                path = null;
                next = null;

                if (pathSearchJob != null) {
                    pathSearchJob.terminate();
                }
                robot.setDeltaMovement(Vec3.ZERO);

                // If the Path is invalid it should be recalculated or this AI should fail
                pathSearch = null;
                pathSearchJob = null;
            }
        }
    }

    @Override
    public void end() {
        if (pathSearchJob != null) {
            pathSearchJob.terminate();
        }
        robot.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        if (finalPos != null) {
            nbt.put("finalPos", NBTUtilBC.writeBlockPos(finalPos));
        }
        nbt.putDouble("maxDistance", maxDistance);
        nbt.putBoolean("skipLastIfNotSoft", skipLastIfNotSoft);

        if (path != null) {
            ListTag pathList = new ListTag();

            for (BlockPos i : path) {
                pathList.add(NBTUtilBC.writeBlockPos(i));
            }

            nbt.put("path", pathList);
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        finalPos = nbt.contains("finalPos") ? NBTUtilBC.readBlockPos(nbt.get("finalPos")) : null;
        maxDistance = nbt.getDouble("maxDistance");
        skipLastIfNotSoft = nbt.getBoolean("skipLastIfNotSoft");

        if (nbt.contains("path")) {
            ListTag pathList = nbt.getList("path", Tag.TAG_INT_ARRAY);

            path = new LinkedList<>();

            for (int i = 0; i < pathList.size(); ++i) {
                path.add(NBTUtilBC.readBlockPos(pathList.get(i)));
            }
        }

        loadedFromNBT = true;
    }
}
