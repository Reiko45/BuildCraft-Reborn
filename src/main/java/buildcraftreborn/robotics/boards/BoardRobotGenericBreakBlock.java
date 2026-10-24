/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.boards;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.ai.AIRobotBreak;
import buildcraftreborn.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraftreborn.robotics.ai.AIRobotGotoSleep;
import buildcraftreborn.robotics.ai.AIRobotGotoStationAndUnload;

/** A board that breaks a kind of block with a kind of tool, fetching the tool from a station first. */
public abstract class BoardRobotGenericBreakBlock extends BoardRobotGenericSearchBlock {

    public BoardRobotGenericBreakBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    /** @param stack The tool, or {@link ItemStack#EMPTY} to ask whether the board needs one. */
    public abstract boolean isExpectedTool(ItemStack stack);

    protected static boolean isBroken(ItemStack stack) {
        return stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage();
    }

    @Override
    public final void update() {
        ItemStack held = robot.getMainHandItem();
        if (!isExpectedTool(ItemStack.EMPTY) && held.isEmpty()) {
            startDelegateAI(new AIRobotFetchAndEquipItemStack(robot,
                stack -> !stack.isEmpty() && !isBroken(stack) && isExpectedTool(stack)));
        } else if (!held.isEmpty() && isBroken(held)) {
            startDelegateAI(new AIRobotGotoStationAndUnload(robot));
        } else if (blockFound() != null) {
            startDelegateAI(new AIRobotBreak(robot, blockFound()));
        } else {
            super.update();
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotFetchAndEquipItemStack || ai instanceof AIRobotGotoStationAndUnload) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotBreak) {
            releaseBlockFound(ai.success());
        }
        super.delegateAIEnded(ai);
    }
}
