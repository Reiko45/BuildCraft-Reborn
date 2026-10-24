/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.transport.IInjectable;
import buildcraftreborn.lib.inventory.filter.ArrayStackOrListFilter;
import buildcraftreborn.robotics.statements.ActionRobotFilter;
import buildcraftreborn.robotics.statements.ActionStationAcceptItems;

/** Waits a little at the station, then puts its items into the station's item pipe, a stack at a time. */
public class AIRobotUnload extends AIRobot {

    private int waitedCycles = 0;

    public AIRobotUnload(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public void update() {
        waitedCycles++;

        if (waitedCycles > 40) {
            if (unload(robot, robot.getDockingStation(), true)) {
                waitedCycles = 0;
            } else {
                setSuccess(!robot.containsItems());
                terminate();
            }
        }
    }

    public static boolean unload(EntityRobotBase robot, @Nullable DockingStation station, boolean doUnload) {
        if (station == null) {
            return false;
        }

        IInjectable output = station.getItemOutput();
        if (output == null) {
            return false;
        }

        Direction injectSide = station.getItemOutputSide().face;
        if (!output.canInjectItems(injectSide)) {
            return false;
        }

        for (int i = 0; i < robot.getSlots(); i++) {
            ItemStack stack = robot.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (!ActionRobotFilter.canInteractWithItem(station, new ArrayStackOrListFilter(stack),
                ActionStationAcceptItems.class)) {
                continue;
            }

            ItemStack left = output.injectItem(stack.copy(), doUnload, injectSide, null, 0);
            int used = stack.getCount() - left.getCount();

            if (used > 0) {
                if (doUnload) {
                    robot.extractItem(i, used, false);
                }
                return true;
            }
        }

        ItemStack held = robot.getMainHandItem();
        if (!held.isEmpty()) {
            if (!ActionRobotFilter.canInteractWithItem(station, new ArrayStackOrListFilter(held),
                ActionStationAcceptItems.class)) {
                return false;
            }

            ItemStack left = output.injectItem(held.copy(), doUnload, injectSide, null, 0);
            int used = held.getCount() - left.getCount();

            if (used > 0) {
                if (doUnload) {
                    if (held.getCount() <= used) {
                        robot.setItemInUse(ItemStack.EMPTY);
                    } else {
                        robot.setItemInUse(held.copyWithCount(held.getCount() - used));
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public long getPowerCost() {
        // 10 RF per tick in 7.1
        return MjAPI.MJ;
    }
}
