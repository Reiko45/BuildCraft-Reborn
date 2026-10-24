/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;

import buildcraftreborn.api.core.IZone;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.station.IStationFilter;

public class AIRobotSearchAndGotoStation extends AIRobot {

    private IStationFilter filter;
    @Nullable
    private IZone zone;

    public AIRobotSearchAndGotoStation(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotSearchAndGotoStation(EntityRobotBase iRobot, IStationFilter iFilter, @Nullable IZone iZone) {
        this(iRobot);

        filter = iFilter;
        zone = iZone;
    }

    @Override
    public void start() {
        startDelegateAI(new AIRobotSearchStation(robot, filter, zone));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchStation search) {
            if (ai.success() && search.targetStation != null) {
                startDelegateAI(new AIRobotGotoStation(robot, search.targetStation));
            } else {
                setSuccess(false);
                terminate();
            }
        } else if (ai instanceof AIRobotGotoStation) {
            setSuccess(ai.success());
            terminate();
        }
    }
}
