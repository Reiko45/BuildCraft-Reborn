/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.robots.ResourceIdBlock;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.robotics.util.IBlockFilter;

/** Finds a block, reserves it, and flies next to it. */
public class AIRobotSearchAndGotoBlock extends AIRobot {

    @Nullable
    private BlockPos blockFound;

    @Nullable
    private IBlockFilter filter;
    private boolean random;
    private double maxDistanceToEnd;

    public AIRobotSearchAndGotoBlock(EntityRobotBase iRobot) {
        super(iRobot);

        blockFound = null;

        random = false;
        filter = null;
    }

    public AIRobotSearchAndGotoBlock(EntityRobotBase iRobot, boolean iRandom, IBlockFilter iPathFound) {
        this(iRobot, iRandom, iPathFound, 0);
    }

    public AIRobotSearchAndGotoBlock(EntityRobotBase iRobot, boolean iRandom, IBlockFilter iPathFound,
        double iMaxDistanceToEnd) {
        this(iRobot);

        random = iRandom;
        filter = iPathFound;
        maxDistanceToEnd = iMaxDistanceToEnd;
    }

    @Override
    public void start() {
        if (filter == null) {
            terminate();
            return;
        }
        startDelegateAI(new AIRobotSearchBlock(robot, random, filter, maxDistanceToEnd));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchBlock searchAI) {
            if (ai.success()) {
                if (searchAI.takeResource() && searchAI.path != null) {
                    blockFound = searchAI.blockFound;
                    if (searchAI.path.isEmpty()) {
                        // Already next to it
                        terminate();
                    } else {
                        startDelegateAI(new AIRobotGotoBlock(robot, searchAI.path));
                    }
                } else {
                    terminate();
                }
            } else {
                terminate();
            }
        } else if (ai instanceof AIRobotGotoBlock) {
            if (!ai.success()) {
                releaseBlockFound();
            }
            terminate();
        }
    }

    @Override
    public boolean success() {
        return blockFound != null;
    }

    private void releaseBlockFound() {
        if (blockFound != null) {
            robot.getRegistry().release(new ResourceIdBlock(blockFound));
            blockFound = null;
        }
    }

    @Nullable
    public BlockPos getBlockFound() {
        return blockFound;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        if (blockFound != null) {
            nbt.put("indexStored", NBTUtilBC.writeBlockPos(blockFound));
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.contains("indexStored")) {
            blockFound = NBTUtilBC.readBlockPos(nbt.get("indexStored"));
        }
    }
}
