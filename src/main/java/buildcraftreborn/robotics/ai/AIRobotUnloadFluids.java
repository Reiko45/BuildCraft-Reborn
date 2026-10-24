/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.statements.ActionRobotFilter;
import buildcraftreborn.robotics.statements.ActionStationAcceptFluids;

/** Empties the robot's tank into a station's fluid pipe, a bucket every two seconds. */
public class AIRobotUnloadFluids extends AIRobot {

    private int waitedCycles = 0;

    public AIRobotUnloadFluids(EntityRobotBase iRobot) {
        super(iRobot);
        setSuccess(false);
    }

    @Override
    public void update() {
        waitedCycles++;

        if (waitedCycles > 40) {
            if (unload(robot, robot.getDockingStation(), true) == 0) {
                terminate();
            } else {
                setSuccess(true);
                waitedCycles = 0;
            }
        }
    }

    public static int unload(EntityRobotBase robot, @Nullable DockingStation station, boolean doUnload) {
        if (station == null) {
            return 0;
        }

        FluidStack tank = robot.getFluidInTank(0);
        if (!ActionRobotFilter.canInteractWithFluid(station, f -> FluidStack.isSameFluidSameComponents(f, tank),
            ActionStationAcceptFluids.class)) {
            return 0;
        }

        IFluidHandler fluidHandler = station.getFluidOutput();
        if (fluidHandler == null) {
            return 0;
        }

        FluidStack drainable = robot.drain(FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (drainable.isEmpty()) {
            return 0;
        }

        drainable = drainable.copy();
        int filled = fluidHandler.fill(drainable, doUnload ? FluidAction.EXECUTE : FluidAction.SIMULATE);

        if (filled > 0 && doUnload) {
            robot.drain(drainable.copyWithAmount(filled), FluidAction.EXECUTE);
        }
        return filled;
    }

    @Override
    public long getPowerCost() {
        return MjAPI.MJ;
    }
}
