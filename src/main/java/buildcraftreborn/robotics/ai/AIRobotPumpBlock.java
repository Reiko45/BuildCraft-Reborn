/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.lib.misc.BlockUtil;

/** Pumps a fluid source block into the robot's tank. */
public class AIRobotPumpBlock extends AIRobot {

    @Nullable
    private BlockPos blockToPump;
    private long waited = 0;
    private int pumped = 0;

    public AIRobotPumpBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotPumpBlock(EntityRobotBase iRobot, BlockPos iBlockToPump) {
        this(iRobot);

        blockToPump = iBlockToPump;
    }

    @Override
    public void start() {
        if (blockToPump != null) {
            robot.aimItemAt(blockToPump);
        }
    }

    @Override
    public void update() {
        if (blockToPump == null) {
            terminate();
            return;
        }
        if (waited < 40) {
            waited++;
        } else {
            FluidStack fluidStack = BlockUtil.drainBlock(robot.level(), blockToPump, false);
            if (fluidStack != null && !fluidStack.isEmpty()) {
                if (robot.fill(fluidStack, FluidAction.SIMULATE) == fluidStack.getAmount()) {
                    robot.fill(BlockUtil.drainBlock(robot.level(), blockToPump, true), FluidAction.EXECUTE);
                    pumped++;
                }
            }
            terminate();
        }
    }

    @Override
    public long getPowerCost() {
        // 5 RF per tick in 7.1
        return MjAPI.MJ / 2;
    }

    @Override
    public boolean success() {
        return pumped > 0;
    }
}
