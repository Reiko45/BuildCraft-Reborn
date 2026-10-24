/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.statements;

import java.util.Collection;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.statements.IActionExternal;
import buildcraftreborn.api.statements.IActionInternal;
import buildcraftreborn.api.statements.IActionInternalSided;
import buildcraftreborn.api.statements.IActionProvider;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.transport.pipe.IFlowFluid;
import buildcraftreborn.api.transport.pipe.IFlowItems;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.robotics.BCRoboticsStatements;
import buildcraftreborn.robotics.RobotUtils;

/** The robot and station actions of gates on pipes with a robot station. */
public enum RobotsActionProvider implements IActionProvider {
    INSTANCE;

    @Override
    public void addInternalActions(Collection<IActionInternal> result, IStatementContainer container) {
        BlockEntity tile = container.getTile();

        if (!(tile instanceof IPipeHolder pipeTile) || pipeTile.getPipe() == null) {
            return;
        }

        List<DockingStation> stations = RobotUtils.getStations(pipeTile);

        if (stations.isEmpty()) {
            return;
        }

        result.add(BCRoboticsStatements.ACTION_ROBOT_GOTO_STATION);
        result.add(BCRoboticsStatements.ACTION_ROBOT_WORK_IN_AREA);
        result.add(BCRoboticsStatements.ACTION_ROBOT_LOAD_UNLOAD_AREA);
        result.add(BCRoboticsStatements.ACTION_ROBOT_WAKE_UP);
        result.add(BCRoboticsStatements.ACTION_ROBOT_FILTER);
        result.add(BCRoboticsStatements.ACTION_ROBOT_FILTER_TOOL);
        result.add(BCRoboticsStatements.ACTION_STATION_FORBID_ROBOT);
        result.add(BCRoboticsStatements.ACTION_STATION_FORCE_ROBOT);

        if (pipeTile.getPipe().getFlow() instanceof IFlowItems) {
            result.add(BCRoboticsStatements.ACTION_STATION_REQUEST_ITEMS);
            result.add(BCRoboticsStatements.ACTION_STATION_ACCEPT_ITEMS);
        }

        if (pipeTile.getPipe().getFlow() instanceof IFlowFluid) {
            result.add(BCRoboticsStatements.ACTION_STATION_ACCEPT_FLUIDS);
        }

        for (DockingStation station : stations) {
            if (station.getItemInput() != null) {
                result.add(BCRoboticsStatements.ACTION_STATION_PROVIDE_ITEMS);
            }

            if (station.getFluidInput() != null) {
                result.add(BCRoboticsStatements.ACTION_STATION_PROVIDE_FLUIDS);
            }

            if (station.getRequestProvider() != null) {
                result.add(BCRoboticsStatements.ACTION_STATION_MACHINE_REQUEST);
            }
        }
    }

    @Override
    public void addInternalSidedActions(Collection<IActionInternalSided> actions, IStatementContainer container,
        Direction side) {}

    @Override
    public void addExternalActions(Collection<IActionExternal> actions, Direction side, BlockEntity tile) {

    }
}
