/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.Entity;

import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.entity.EntityRobot;

/** Flies to an entity and hits it every second until it dies. */
public class AIRobotAttack extends AIRobot {

    @Nullable
    private Entity target;

    private int delay = 10;

    public AIRobotAttack(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotAttack(EntityRobotBase iRobot, Entity iTarget) {
        this(iRobot);

        target = iTarget;
    }

    @Override
    public void preempt(@Nullable AIRobot ai) {
        if (ai instanceof AIRobotGotoBlock) {
            // target may become null in the event of a load. In that case, just
            // go to the expected location.
            if (target != null && robot.distanceTo(target) <= 2.0) {
                abortDelegateAI();
                robot.setItemActive(true);
            }
        }
    }

    @Override
    public void update() {
        if (target == null || !target.isAlive()) {
            terminate();
            return;
        }

        if (robot.distanceTo(target) > 2.0) {
            startDelegateAI(new AIRobotGotoBlock(robot, target.blockPosition()));
            robot.setItemActive(false);

            return;
        }

        delay++;

        if (delay > 20) {
            delay = 0;
            ((EntityRobot) robot).attackTargetEntityWithCurrentItem(target);
            robot.aimItemAt(target.blockPosition());
        }
    }

    @Override
    public void end() {
        robot.setItemActive(false);
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoBlock) {
            if (!ai.success() && target != null) {
                robot.unreachableEntityDetected(target);
            }
            terminate();
        }
    }

    @Override
    public long getPowerCost() {
        return AIRobotBreak.BREAK_POWER * 2 / 20;
    }
}
