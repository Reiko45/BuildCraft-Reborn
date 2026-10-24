/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;

public class AIRobotGotoStationToUnloadFluids extends AIRobot {

    public AIRobotGotoStationToUnloadFluids(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public void update() {
        startDelegateAI(new AIRobotSearchAndGotoStation(robot,
            station -> AIRobotUnloadFluids.unload(robot, station, false) > 0, robot.getZoneToLoadUnload()));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoStation) {
            setSuccess(ai.success());
            terminate();
        }
    }
}
