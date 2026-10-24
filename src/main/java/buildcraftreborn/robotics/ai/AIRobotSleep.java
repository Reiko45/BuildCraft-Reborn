/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.statements.StatementSlot;
import buildcraftreborn.robotics.statements.ActionRobotWakeUp;

/** Sleeps for a minute (or until a gate wakes the robot up). */
public class AIRobotSleep extends AIRobot {

    private static final int SLEEPING_TIME = 60 * 20;
    private int sleptTime = 0;

    public AIRobotSleep(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public void preempt(@Nullable AIRobot ai) {
        DockingStation linked = robot.getLinkedStation();
        if (linked == null) {
            return;
        }
        for (StatementSlot s : linked.getActiveActions()) {
            if (s.statement instanceof ActionRobotWakeUp) {
                terminate();
                return;
            }
        }
    }

    @Override
    public void update() {
        sleptTime++;

        if (sleptTime > SLEEPING_TIME) {
            terminate();
        }
    }

    @Override
    public long getPowerCost() {
        // 0.1 RF per tick in 7.1
        return MjAPI.MJ / 100;
    }
}
