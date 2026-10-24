/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import net.minecraft.world.phys.Vec3;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;

/** Goes to a station on a kinesis pipe and waits there until the battery is (nearly) full. */
public class AIRobotRecharge extends AIRobot {

    public AIRobotRecharge(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public void start() {
        robot.getRegistry().releaseResources(robot);
        robot.setDeltaMovement(Vec3.ZERO);

        startDelegateAI(new AIRobotSearchAndGotoStation(robot, station -> station.providesPower(), null));
    }

    @Override
    public long getPowerCost() {
        return 0;
    }

    @Override
    public void update() {
        // 7.1: until 500 RF short of full
        if (robot.getPower() >= EntityRobotBase.MAX_POWER - 50 * MjAPI.MJ) {
            terminate();
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoStation) {
            if (!ai.success()) {
                setSuccess(false);
                terminate();
            }
        }
    }
}
