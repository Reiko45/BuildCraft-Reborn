/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;

/** Unloads everything; if no station takes it, drops it. */
public class AIRobotDisposeItems extends AIRobot {

    public AIRobotDisposeItems(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public void start() {
        startDelegateAI(new AIRobotGotoStationAndUnload(robot));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationAndUnload) {
            if (ai.success()) {
                if (robot.containsItems()) {
                    startDelegateAI(new AIRobotGotoStationAndUnload(robot));
                } else {
                    terminate();
                }
            } else {
                for (int i = 0; i < robot.getSlots(); i++) {
                    ItemStack stack = robot.extractItem(i, Integer.MAX_VALUE, false);
                    if (!stack.isEmpty()) {
                        robot.spawnAtLocation(stack, 0);
                    }
                }
                terminate();
            }
        }
    }
}
