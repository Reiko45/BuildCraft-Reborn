/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.boards;

import buildcraftreborn.api.boards.RedstoneBoardRobot;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.lib.inventory.filter.PassThroughStackFilter;
import buildcraftreborn.robotics.ai.AIRobotGotoSleep;
import buildcraftreborn.robotics.ai.AIRobotGotoStationAndLoad;
import buildcraftreborn.robotics.ai.AIRobotGotoStationAndUnload;
import buildcraftreborn.robotics.ai.AIRobotLoad;
import buildcraftreborn.robotics.statements.ActionRobotFilter;

/** Carries items from stations that provide them to stations that accept them. */
public class BoardRobotCarrier extends RedstoneBoardRobot {

    public BoardRobotCarrier(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("carrier");
    }

    @Override
    public void update() {
        if (!robot.containsItems()) {
            IStackFilter filter = robot.getLinkedStation() == null ? new PassThroughStackFilter()
                : ActionRobotFilter.getGateFilter(robot.getLinkedStation());
            startDelegateAI(new AIRobotGotoStationAndLoad(robot, filter, AIRobotLoad.ANY_QUANTITY));
        } else {
            startDelegateAI(new AIRobotGotoStationAndUnload(robot));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationAndLoad || ai instanceof AIRobotGotoStationAndUnload) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }
}
