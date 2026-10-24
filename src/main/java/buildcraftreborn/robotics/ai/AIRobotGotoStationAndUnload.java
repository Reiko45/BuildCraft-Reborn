/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;

import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;

public class AIRobotGotoStationAndUnload extends AIRobot {

    @Nullable
    private DockingStation station;

    public AIRobotGotoStationAndUnload(EntityRobotBase iRobot) {
        super(iRobot);

        station = null;
    }

    public AIRobotGotoStationAndUnload(EntityRobotBase iRobot, DockingStation iStation) {
        super(iRobot);

        station = iStation;
    }

    @Override
    public void start() {
        if (station == null) {
            startDelegateAI(new AIRobotGotoStationToUnload(robot));
        } else {
            startDelegateAI(new AIRobotGotoStation(robot, station));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationToUnload || ai instanceof AIRobotGotoStation) {
            if (ai.success()) {
                startDelegateAI(new AIRobotUnload(robot));
            } else {
                setSuccess(false);
                terminate();
            }
        } else if (ai instanceof AIRobotUnload) {
            // 1.21: 7.1 left this running (it ended with the board's next update)
            setSuccess(ai.success());
            terminate();
        }
    }
}
