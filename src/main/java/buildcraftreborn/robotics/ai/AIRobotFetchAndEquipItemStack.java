/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.lib.inventory.filter.AggregateFilter;
import buildcraftreborn.robotics.statements.ActionRobotFilterTool;

/** Goes to a station that provides a matching item (a tool, seeds...) and takes one to hold. */
public class AIRobotFetchAndEquipItemStack extends AIRobot {

    private IStackFilter filter;
    private int delay = 0;

    public AIRobotFetchAndEquipItemStack(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotFetchAndEquipItemStack(EntityRobotBase iRobot, IStackFilter iFilter) {
        this(iRobot);

        filter = iRobot.getLinkedStation() == null ? iFilter
            : new AggregateFilter(ActionRobotFilterTool.getGateFilter(iRobot.getLinkedStation()), iFilter);
    }

    @Override
    public void start() {
        startDelegateAI(new AIRobotGotoStationToLoad(robot, filter, 1));
    }

    @Override
    public void update() {
        if (robot.getDockingStation() == null) {
            setSuccess(false);
            terminate();
            return;
        }

        if (delay++ > 40) {
            if (equipItemStack()) {
                terminate();
            } else {
                delay = 0;
                startDelegateAI(new AIRobotGotoStationToLoad(robot, filter, 1));
            }
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationToLoad) {
            if (filter == null) {
                // filter can't be retreived, usually because of a load operation.
                // Force a hard abort, preventing parent AI to continue normal
                // sequence of actions and possibly re-starting this.
                abort();
                return;
            }
            if (!ai.success()) {
                setSuccess(false);
                terminate();
            }
        }
    }

    private boolean equipItemStack() {
        if (robot.getDockingStation() == null || robot.getDockingStation().getItemInput() == null) {
            return false;
        }

        ItemStack possible = AIRobotLoad.takeSingle(robot.getDockingStation(), filter, true);
        if (possible.isEmpty()) {
            return false;
        }
        robot.setItemInUse(possible);
        return true;
    }
}
