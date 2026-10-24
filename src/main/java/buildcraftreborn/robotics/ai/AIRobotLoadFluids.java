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

import buildcraftreborn.api.core.IFluidFilter;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.statements.ActionRobotFilter;
import buildcraftreborn.robotics.statements.ActionStationProvideFluids;

/** Fills the robot's tank from the tank a station's wooden fluid pipe takes from, a bucket every two seconds. */
public class AIRobotLoadFluids extends AIRobot {

    private int waitedCycles = 0;
    private IFluidFilter filter;

    public AIRobotLoadFluids(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotLoadFluids(EntityRobotBase iRobot, IFluidFilter iFilter) {
        this(iRobot);

        filter = iFilter;
        setSuccess(false);
    }

    @Override
    public void update() {
        if (filter == null) {
            terminate();
            return;
        }

        waitedCycles++;

        if (waitedCycles > 40) {
            if (load(robot, robot.getDockingStation(), filter, true) == 0) {
                terminate();
            } else {
                setSuccess(true);
                waitedCycles = 0;
            }
        }
    }

    public static int load(EntityRobotBase robot, @Nullable DockingStation station, IFluidFilter filter,
        boolean doLoad) {
        if (station == null) {
            return 0;
        }

        if (!ActionRobotFilter.canInteractWithFluid(station, filter, ActionStationProvideFluids.class)) {
            return 0;
        }

        IFluidHandler handler = station.getFluidInput();
        if (handler == null) {
            return 0;
        }

        FluidStack drainable = handler.drain(FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (drainable.isEmpty() || !filter.matches(drainable)) {
            return 0;
        }

        drainable = drainable.copy();
        int filled = robot.fill(drainable, doLoad ? FluidAction.EXECUTE : FluidAction.SIMULATE);

        if (filled > 0 && doLoad) {
            handler.drain(drainable.copyWithAmount(filled), FluidAction.EXECUTE);
        }
        return filled;
    }

    @Override
    public long getPowerCost() {
        return 8 * MjAPI.MJ / 10;
    }
}
