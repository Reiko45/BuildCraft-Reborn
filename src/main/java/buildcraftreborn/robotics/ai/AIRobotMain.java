/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;

/** The root of every robot's AI: runs the board, and takes over to recharge (or shut down) when power runs low, and
 * when a gate sends the robot somewhere. */
public class AIRobotMain extends AIRobot {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    private AIRobot overridingAI;
    private int rechargeCooldown;

    public AIRobotMain(EntityRobotBase iRobot) {
        super(iRobot);
        rechargeCooldown = 0;
    }

    @Override
    public long getPowerCost() {
        return 0;
    }

    @Override
    public void preempt(@Nullable AIRobot ai) {
        if (robot.getPower() <= EntityRobotBase.SHUTDOWN_POWER
            && (robot.getDockingStation() == null || !robot.getDockingStation().providesPower())) {
            if (!(ai instanceof AIRobotShutdown)) {
                LOGGER.info("Shutting down robot {} - no power", robot);
                startDelegateAI(new AIRobotShutdown(robot));
            }
        } else if (robot.getPower() < EntityRobotBase.SAFETY_POWER) {
            if (!(ai instanceof AIRobotRecharge) && !(ai instanceof AIRobotShutdown)) {
                if (rechargeCooldown-- <= 0) {
                    startDelegateAI(new AIRobotRecharge(robot));
                }
            }
        } else if (!(ai instanceof AIRobotRecharge)) {
            if (overridingAI != null && ai != overridingAI) {
                startDelegateAI(overridingAI);
            }
        }
    }

    @Override
    public void update() {
        AIRobot board = robot.getBoard();

        if (board != null) {
            startDelegateAI(board);
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotRecharge) {
            if (!ai.success()) {
                rechargeCooldown = 120;
            }
        }
        if (ai == overridingAI) {
            overridingAI = null;
        }
    }

    public void setOverridingAI(AIRobot ai) {
        if (overridingAI == null) {
            overridingAI = ai;
        }
    }

    @Nullable
    public AIRobot getOverridingAI() {
        return overridingAI;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }
}
