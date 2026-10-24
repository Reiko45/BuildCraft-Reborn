/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.boards;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import buildcraftreborn.api.boards.RedstoneBoardRobot;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.robots.ResourceIdBlock;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraftreborn.robotics.ai.AIRobotGotoSleep;
import buildcraftreborn.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraftreborn.robotics.ai.AIRobotStripesHandler;

/** Uses any item it is given at random spots, as a stripes pipe would. */
public class BoardRobotStripes extends RedstoneBoardRobot {

    @Nullable
    private BlockPos blockFound;

    public BoardRobotStripes(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("stripes");
    }

    @Override
    public void update() {
        if (robot.getMainHandItem().isEmpty()) {
            startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, stack -> !stack.isEmpty()));
        } else {
            startDelegateAI(new AIRobotSearchAndGotoBlock(robot, true, (world, pos) -> world.isEmptyBlock(pos)
                && !robot.getRegistry().isTaken(new ResourceIdBlock(pos))));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoBlock search) {
            if (ai.success() && search.getBlockFound() != null) {
                blockFound = search.getBlockFound();
                startDelegateAI(new AIRobotStripesHandler(robot, blockFound));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotFetchAndEquipItemStack) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotStripesHandler) {
            releaseBlockFound();
        }
    }

    private void releaseBlockFound() {
        if (blockFound != null) {
            robot.getRegistry().release(new ResourceIdBlock(blockFound));
            blockFound = null;
        }
    }

    @Override
    public void end() {
        releaseBlockFound();
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
}
