/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.boards;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import buildcraftreborn.api.boards.RedstoneBoardRobot;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.ai.AIRobotGotoBlock;
import buildcraftreborn.robotics.ai.AIRobotGotoSleep;
import buildcraftreborn.robotics.ai.AIRobotGotoStationAndLoad;
import buildcraftreborn.robotics.ai.AIRobotLoad;
import buildcraftreborn.robotics.ai.AIRobotSearchRandomGroundBlock;

/** Fetches TNT and drops it, lit, on random spots of its work area from 20 blocks up. */
public class BoardRobotBomber extends RedstoneBoardRobot {

    private static final IStackFilter TNT_FILTER = stack -> stack.is(Items.TNT);

    private final int flyingHeight = 20;

    public BoardRobotBomber(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("bomber");
    }

    @Override
    public final void update() {
        if (!robot.containsItems()) {
            startDelegateAI(new AIRobotGotoStationAndLoad(robot, TNT_FILTER, AIRobotLoad.ANY_QUANTITY));
        } else {
            startDelegateAI(new AIRobotSearchRandomGroundBlock(robot, 100,
                (world, pos) -> pos.getY() < world.getMaxBuildHeight() - flyingHeight && !world.isEmptyBlock(pos),
                robot.getZoneToWork()));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationAndLoad) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotSearchRandomGroundBlock aiFind) {
            if (ai.success() && aiFind.blockFound != null) {
                startDelegateAI(new AIRobotGotoBlock(robot, aiFind.blockFound.offset(0, flyingHeight, 0)));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotGotoBlock) {
            if (ai.success()) {
                ItemStack stack = ItemStack.EMPTY;
                for (int i = 0; i < robot.getSlots() && stack.isEmpty(); i++) {
                    if (TNT_FILTER.matches(robot.getStackInSlot(i))) {
                        stack = robot.extractItem(i, 1, false);
                    }
                }

                if (!stack.isEmpty()) {
                    PrimedTnt tnt = new PrimedTnt(robot.level(), robot.getX() + 0.25, robot.getY() - 1,
                        robot.getZ() + 0.25, robot);
                    tnt.setFuse(37);
                    robot.level().addFreshEntity(tnt);
                    robot.level().playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.TNT_PRIMED,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }
}
