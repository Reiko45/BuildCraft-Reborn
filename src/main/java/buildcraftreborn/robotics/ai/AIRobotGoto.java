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

/** Flies in a straight line at a tenth of a block per tick. */
public abstract class AIRobotGoto extends AIRobot {

    protected Vec3 next, dir;

    public AIRobotGoto(EntityRobotBase iRobot) {
        super(iRobot);
    }

    protected void setDestination(EntityRobotBase robot, Vec3 dest) {
        next = dest;
        dir = next.subtract(robot.position());

        double magnitude = dir.length();

        if (magnitude != 0) {
            dir = dir.scale(1 / magnitude);
        } else {
            dir = Vec3.ZERO;
        }

        robot.setDeltaMovement(dir.scale(0.1));
    }

    @Override
    public long getPowerCost() {
        // 3 RF per tick in 7.1
        return 3 * MjAPI.MJ / 10;
    }
}
