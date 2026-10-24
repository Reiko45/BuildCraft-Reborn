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
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.ai.AIRobotGotoSleep;

public class BoardRobotEmpty extends RedstoneBoardRobot {

    public BoardRobotEmpty(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return RedstoneBoardRobotEmptyNBT.instance;
    }

    @Override
    public void update() {
        startDelegateAI(new AIRobotGotoSleep(robot));
    }
}
