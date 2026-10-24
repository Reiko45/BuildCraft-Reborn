/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;

/** Goes to a station it can load the items from. */
public class AIRobotGotoStationToLoad extends AIRobot {

    private IStackFilter filter;
    private int quantity;

    public AIRobotGotoStationToLoad(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotGotoStationToLoad(EntityRobotBase iRobot, IStackFilter iFilter, int iQuantity) {
        this(iRobot);

        filter = iFilter;
        quantity = iQuantity;
    }

    @Override
    public void update() {
        if (filter == null) {
            setSuccess(false);
            terminate();
            return;
        }
        startDelegateAI(new AIRobotSearchAndGotoStation(robot,
            station -> AIRobotLoad.load(robot, station, filter, quantity, false), robot.getZoneToLoadUnload()));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoStation) {
            setSuccess(ai.success());
            terminate();
        }
    }
}
