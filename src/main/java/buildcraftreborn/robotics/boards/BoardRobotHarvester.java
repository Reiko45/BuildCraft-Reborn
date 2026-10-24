/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.boards;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.core.BuildCraftAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.ai.AIRobotHarvest;

/** Harvests mature crops. */
public class BoardRobotHarvester extends BoardRobotGenericSearchBlock {

    public BoardRobotHarvester(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("harvester");
    }

    @Override
    public boolean isExpectedBlock(Level world, BlockPos pos) {
        return BuildCraftAPI.getWorldProperty("harvestable").get(world, pos);
    }

    @Override
    public void update() {
        if (blockFound() != null) {
            startDelegateAI(new AIRobotHarvest(robot, blockFound()));
        } else {
            super.update();
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotHarvest) {
            releaseBlockFound(ai.success());
        }
        super.delegateAIEnded(ai);
    }
}
