/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.boards;

import java.util.HashSet;
import java.util.Set;

import buildcraftreborn.api.boards.RedstoneBoardRobot;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.ai.AIRobotFetchItem;
import buildcraftreborn.robotics.ai.AIRobotGotoSleep;
import buildcraftreborn.robotics.ai.AIRobotGotoStationAndUnload;
import buildcraftreborn.robotics.statements.ActionRobotFilter;

/** Picks up items on the ground and unloads them at a station. */
public class BoardRobotPicker extends RedstoneBoardRobot {
    /** The items picker robots are flying to (by entity id), so two robots don't go for the same one. */
    public static final Set<Integer> targettedItems = new HashSet<>();

    public BoardRobotPicker(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public static void onServerStart() {
        targettedItems.clear();
    }

    private void fetchNewItem() {
        startDelegateAI(new AIRobotFetchItem(robot, 250, robot.getLinkedStation() == null ? null
            : ActionRobotFilter.getGateFilter(robot.getLinkedStation()), robot.getZoneToWork()));
    }

    @Override
    public void update() {
        fetchNewItem();
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotFetchItem) {
            if (ai.success()) {
                // if we find an item - that may have been cancelled.
                // let's try to get another one
                fetchNewItem();
            } else if (robot.containsItems()) {
                startDelegateAI(new AIRobotGotoStationAndUnload(robot));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotGotoStationAndUnload) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("picker");
    }
}
