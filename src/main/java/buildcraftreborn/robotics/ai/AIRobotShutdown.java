/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import net.minecraft.world.phys.Vec3;

import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;

/** Without power (or a home station), the robot drifts down to the ground and stays there. */
public class AIRobotShutdown extends AIRobot {
    private static final double FALL = -0.075;

    private int skip;
    private final double motionX;
    private final double motionZ;

    public AIRobotShutdown(EntityRobotBase iRobot) {
        super(iRobot);
        skip = 0;
        motionX = robot.getDeltaMovement().x;
        motionZ = robot.getDeltaMovement().z;
    }

    @Override
    public void start() {
        robot.undock();
        robot.setDeltaMovement(motionX, FALL, motionZ);
    }

    private boolean isBlocked(double yOffset) {
        Vec3 motion = robot.getDeltaMovement();
        return !robot.level().noCollision(robot, robot.getBoundingBox().expandTowards(motion.x, yOffset, motion.z));
    }

    @Override
    public void update() {
        if (skip == 0) {
            Vec3 motion = robot.getDeltaMovement();
            if (!isBlocked(FALL)) {
                robot.setDeltaMovement(motion.x, FALL, motion.z);
            } else {
                int guard = 0;
                while (isBlocked(0) && guard++ < 40) {
                    robot.setPos(robot.getX(), robot.getY() + 0.075, robot.getZ());
                }
                if (motion.x != 0 || motion.z != 0) {
                    robot.setDeltaMovement(Vec3.ZERO);
                    skip = 0;
                } else {
                    robot.setDeltaMovement(Vec3.ZERO);
                    skip = 20;
                }
            }
        } else {
            skip--;
        }
    }

    @Override
    public long getPowerCost() {
        return 0;
    }
}
