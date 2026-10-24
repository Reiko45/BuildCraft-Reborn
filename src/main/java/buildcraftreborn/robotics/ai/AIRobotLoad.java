/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.statements.ActionRobotFilter;
import buildcraftreborn.robotics.statements.ActionStationProvideItems;

/** Waits a little at the station, then takes items from the inventory the station's wooden pipe takes from. */
public class AIRobotLoad extends AIRobot {

    public static final int ANY_QUANTITY = -1;
    private IStackFilter filter;
    private int quantity;
    private int waitedCycles = 0;

    public AIRobotLoad(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotLoad(EntityRobotBase iRobot, IStackFilter iFilter, int iQuantity) {
        super(iRobot);

        filter = iFilter;
        quantity = iQuantity;
    }

    @Override
    public void update() {
        if (filter == null) {
            terminate();
            return;
        }

        waitedCycles++;

        if (waitedCycles > 40) {
            setSuccess(load(robot, robot.getDockingStation(), filter, quantity, true));
            terminate();
        }
    }

    private static boolean canTake(DockingStation station, IStackFilter filter, ItemStack stack) {
        return !stack.isEmpty() && filter.matches(stack) && ActionStationProvideItems.canExtractItem(station, stack)
            && ActionRobotFilter.canInteractWithItem(station, filter, ActionStationProvideItems.class);
    }

    /** Similar method to {@link #load(EntityRobotBase, DockingStation, IStackFilter, int, boolean)} but returns the
     * itemstack rather than loading it onto the robot.
     *
     * Only loads a single item at once. */
    public static ItemStack takeSingle(@Nullable DockingStation station, IStackFilter filter, boolean doTake) {
        if (station == null) {
            return ItemStack.EMPTY;
        }

        IItemHandler tileInventory = station.getItemInput();
        if (tileInventory == null) {
            return ItemStack.EMPTY;
        }

        for (int i = 0; i < tileInventory.getSlots(); i++) {
            ItemStack stack = tileInventory.getStackInSlot(i);

            if (!canTake(station, filter, stack) || tileInventory.extractItem(i, 1, true).isEmpty()) {
                continue;
            }

            return tileInventory.extractItem(i, 1, !doTake);
        }
        return ItemStack.EMPTY;
    }

    public static boolean load(EntityRobotBase robot, @Nullable DockingStation station, IStackFilter filter,
        int quantity, boolean doLoad) {
        if (station == null) {
            return false;
        }

        int loaded = 0;

        IItemHandler tileInventory = station.getItemInput();
        if (tileInventory == null) {
            return false;
        }

        for (int i = 0; i < tileInventory.getSlots(); i++) {
            ItemStack stack = tileInventory.getStackInSlot(i);

            if (!canTake(station, filter, stack)) {
                continue;
            }

            int wanted = quantity == ANY_QUANTITY ? stack.getCount() : Math.min(stack.getCount(), quantity - loaded);
            ItemStack available = tileInventory.extractItem(i, wanted, true);
            if (available.isEmpty()) {
                continue;
            }
            ItemStack left = ItemHandlerHelper.insertItemStacked(robot, available, true);
            int fits = available.getCount() - left.getCount();
            if (fits <= 0) {
                continue;
            }
            if (doLoad) {
                ItemStack taken = tileInventory.extractItem(i, fits, false);
                ItemStack notStored = ItemHandlerHelper.insertItemStacked(robot, taken, false);
                if (!notStored.isEmpty()) {
                    ItemStack back = tileInventory.insertItem(i, notStored, false);
                    if (!back.isEmpty()) {
                        robot.spawnAtLocation(back);
                    }
                }
            }

            if (quantity == ANY_QUANTITY) {
                return true;
            }
            loaded += fits;
            if (quantity - loaded <= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getPowerCost() {
        // 8 RF per tick in 7.1
        return 8 * MjAPI.MJ / 10;
    }
}
