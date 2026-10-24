/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.ItemHandlerHelper;

import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.core.IZone;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.boards.BoardRobotPicker;

/** Flies to the nearest item on the ground that fits in the robot and picks it up. */
public class AIRobotFetchItem extends AIRobot {

    @Nullable
    private ItemEntity target;

    private float maxRange;
    @Nullable
    private IStackFilter stackFilter;
    private int pickTime = -1;
    @Nullable
    private IZone zone;

    public AIRobotFetchItem(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotFetchItem(EntityRobotBase iRobot, float iMaxRange, @Nullable IStackFilter iStackFilter,
        @Nullable IZone iZone) {
        this(iRobot);

        maxRange = iMaxRange;
        stackFilter = iStackFilter;
        zone = iZone;
    }

    @Override
    public void preempt(@Nullable AIRobot ai) {
        if (target != null && !target.isAlive()) {
            terminate();
        }
    }

    @Override
    public void update() {
        if (target == null) {
            scanForItem();
        } else {
            pickTime++;

            if (pickTime > 5) {
                ItemStack stack = target.getItem();
                ItemStack left = ItemHandlerHelper.insertItemStacked(robot, stack.copy(), false);

                if (left.isEmpty()) {
                    target.discard();
                } else {
                    target.setItem(left);
                }

                terminate();
            }
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoBlock) {
            if (target == null) {
                // This would happen after a load. As we reached the item
                // location already, just consider that the item is not there
                // anymore and allow user to try to find another one.
                setSuccess(false);
                terminate();
                return;
            }

            if (!ai.success()) {
                robot.unreachableEntityDetected(target);
                setSuccess(false);
                terminate();
            }
        }
    }

    @Override
    public void end() {
        if (target != null) {
            BoardRobotPicker.targettedItems.remove(target.getId());
        }
    }

    private void scanForItem() {
        double previousDistance = Double.MAX_VALUE;
        double maxDistance = maxRange * maxRange;

        for (ItemEntity item : robot.level().getEntitiesOfClass(ItemEntity.class,
            robot.getBoundingBox().inflate(maxRange))) {
            if (!item.isAlive() || BoardRobotPicker.targettedItems.contains(item.getId())
                || robot.isKnownUnreachable(item) || (zone != null && !zone.contains(item.position()))) {
                continue;
            }
            double sqrDistance = item.distanceToSqr(robot);

            if (sqrDistance >= maxDistance) {
                continue;
            } else if (stackFilter != null && !stackFilter.matches(item.getItem())) {
                continue;
            }
            ItemStack left = ItemHandlerHelper.insertItemStacked(robot, item.getItem().copy(), true);
            if (left.getCount() < item.getItem().getCount()) {
                if (target == null || sqrDistance < previousDistance) {
                    previousDistance = sqrDistance;
                    target = item;
                }
            }
        }

        if (target != null) {
            BoardRobotPicker.targettedItems.add(target.getId());
            if (!target.blockPosition().equals(robot.blockPosition())) {
                startDelegateAI(new AIRobotGotoBlock(robot, target.blockPosition(), true));
            }
        } else {
            // No item was found, terminate this AI
            setSuccess(false);
            terminate();
        }
    }

    @Override
    public long getPowerCost() {
        // 15 RF per tick in 7.1
        return 15 * MjAPI.MJ / 10;
    }
}
