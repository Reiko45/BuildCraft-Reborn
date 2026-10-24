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

/** Goes home and sleeps. */
public class AIRobotGotoSleep extends AIRobot {

    public AIRobotGotoSleep(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public void start() {
        robot.getRegistry().releaseResources(robot);
        if (robot.getLinkedStation() == null) {
            startDelegateAI(new AIRobotSleep(robot));
        } else {
            startDelegateAI(new AIRobotGotoStation(robot, robot.getLinkedStation()));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStation) {
            startDelegateAI(new AIRobotSleep(robot));
        } else if (ai instanceof AIRobotSleep) {
            terminate();
        }
    }
}
