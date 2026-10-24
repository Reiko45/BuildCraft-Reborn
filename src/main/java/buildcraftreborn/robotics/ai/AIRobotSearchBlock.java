/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import java.util.Iterator;
import java.util.LinkedList;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import buildcraftreborn.api.core.IZone;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.robots.ResourceIdBlock;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.robotics.util.BlockScannerExpanding;
import buildcraftreborn.robotics.util.BlockScannerRandom;
import buildcraftreborn.robotics.util.BlockScannerZoneRandom;
import buildcraftreborn.robotics.util.IBlockFilter;
import buildcraftreborn.robotics.util.IterableAlgorithmRunner;
import buildcraftreborn.robotics.util.PathFindingSearch;

/** Looks for a block matching a filter (nearest first, or at random, in the robot's work area) with a path to it. */
public class AIRobotSearchBlock extends AIRobot {

    @Nullable
    public BlockPos blockFound;
    @Nullable
    public LinkedList<BlockPos> path;
    @Nullable
    private PathFindingSearch blockScanner = null;
    @Nullable
    private IterableAlgorithmRunner blockScannerJob;
    private IBlockFilter pathFound;
    private Iterator<BlockPos> blockIter;
    private double maxDistanceToEnd;
    @Nullable
    private IZone zone;

    public AIRobotSearchBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotSearchBlock(EntityRobotBase iRobot, boolean random, IBlockFilter iPathFound,
        double iMaxDistanceToEnd) {
        super(iRobot);

        pathFound = iPathFound;
        zone = iRobot.getZoneToWork();
        if (!random) {
            blockIter = new BlockScannerExpanding().iterator();
        } else {
            if (zone != null) {
                blockIter = new BlockScannerZoneRandom(iRobot.blockPosition(), iRobot.level().random, zone).iterator();
            } else {
                blockIter = new BlockScannerRandom(iRobot.level().random, 64).iterator();
            }
        }
        blockFound = null;
        path = null;
        maxDistanceToEnd = iMaxDistanceToEnd;
    }

    @Override
    public void start() {
        if (pathFound == null) {
            return;
        }
        blockScanner = new PathFindingSearch(robot.level(), robot.blockPosition(), blockIter, pathFound,
            maxDistanceToEnd, 96, zone);
        blockScannerJob = new IterableAlgorithmRunner(blockScanner);
        blockScannerJob.start();
    }

    @Override
    public void update() {
        if (blockScannerJob == null) {
            // This is probably due to a load from NBT. Abort the ai in
            // that case, since there's no filter to analyze either.
            abort();
            return;
        }

        if (blockScannerJob.isDone()) {
            path = blockScanner.getResult();

            if (path != null && path.size() > 0) {
                path.removeLast();
                blockFound = blockScanner.getResultTarget();
            } else {
                path = null;
            }

            terminate();
        }
    }

    @Override
    public void end() {
        if (blockScannerJob != null) {
            blockScannerJob.terminate();
        }
    }

    @Override
    public boolean success() {
        return blockFound != null;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        if (blockFound != null) {
            nbt.put("blockFound", NBTUtilBC.writeBlockPos(blockFound));
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.contains("blockFound")) {
            blockFound = NBTUtilBC.readBlockPos(nbt.get("blockFound"));
        }
    }

    public boolean takeResource() {
        boolean taken = blockFound != null && robot.getRegistry().take(new ResourceIdBlock(blockFound), robot);
        unreserve();
        return taken;
    }

    public void unreserve() {
        if (blockScanner != null && blockFound != null) {
            blockScanner.unreserve(blockFound);
        }
    }

    @Override
    public long getPowerCost() {
        // 2 RF per tick in 7.1
        return MjAPI.MJ / 5;
    }
}
