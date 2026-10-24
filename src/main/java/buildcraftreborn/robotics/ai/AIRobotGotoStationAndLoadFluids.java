/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import buildcraftreborn.api.core.IFluidFilter;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;

public class AIRobotGotoStationAndLoadFluids extends AIRobot {

    private IFluidFilter filter;

    public AIRobotGotoStationAndLoadFluids(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotGotoStationAndLoadFluids(EntityRobotBase iRobot, IFluidFilter iFilter) {
        this(iRobot);

        filter = iFilter;
    }

    @Override
    public void start() {
        startDelegateAI(new AIRobotGotoStationToLoadFluids(robot, filter));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationToLoadFluids) {
            if (filter != null && ai.success()) {
                startDelegateAI(new AIRobotLoadFluids(robot, filter));
            } else {
                setSuccess(false);
                terminate();
            }
        } else if (ai instanceof AIRobotLoadFluids) {
            // 1.21: 7.1 left this running (it ended with the board's next update)
            setSuccess(ai.success());
            terminate();
        }
    }
}
