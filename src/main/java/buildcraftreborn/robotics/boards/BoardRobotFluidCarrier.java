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
import buildcraftreborn.api.core.IFluidFilter;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.lib.inventory.filter.PassThroughFluidFilter;
import buildcraftreborn.robotics.ai.AIRobotGotoSleep;
import buildcraftreborn.robotics.ai.AIRobotGotoStationAndLoadFluids;
import buildcraftreborn.robotics.ai.AIRobotGotoStationAndUnloadFluids;
import buildcraftreborn.robotics.statements.ActionRobotFilter;

/** Carries fluids from stations that provide them to stations that accept them (1.12's "Tank" board). */
public class BoardRobotFluidCarrier extends RedstoneBoardRobot {

    public BoardRobotFluidCarrier(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("fluid_carrier");
    }

    @Override
    public void update() {
        if (robot.getFluidInTank(0).isEmpty()) {
            IFluidFilter filter = robot.getLinkedStation() == null ? new PassThroughFluidFilter()
                : ActionRobotFilter.getGateFluidFilter(robot.getLinkedStation());
            startDelegateAI(new AIRobotGotoStationAndLoadFluids(robot, filter));
        } else {
            startDelegateAI(new AIRobotGotoStationAndUnloadFluids(robot));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationAndLoadFluids || ai instanceof AIRobotGotoStationAndUnloadFluids) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }
}
