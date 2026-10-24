/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.statements;

import java.util.Collection;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.ITriggerExternal;
import buildcraftreborn.api.statements.ITriggerInternal;
import buildcraftreborn.api.statements.ITriggerInternalSided;
import buildcraftreborn.api.statements.ITriggerProvider;
import buildcraftreborn.robotics.BCRoboticsStatements;
import buildcraftreborn.robotics.RobotUtils;

/** The robot triggers of gates on pipes with a robot station. */
public enum RobotsTriggerProvider implements ITriggerProvider {
    INSTANCE;

    @Override
    public void addInternalTriggers(Collection<ITriggerInternal> result, IStatementContainer container) {
        if (!RobotUtils.getStations(container.getTile()).isEmpty()) {
            result.add(BCRoboticsStatements.TRIGGER_ROBOT_SLEEP);
            result.add(BCRoboticsStatements.TRIGGER_ROBOT_IN_STATION);
            result.add(BCRoboticsStatements.TRIGGER_ROBOT_LINKED);
            result.add(BCRoboticsStatements.TRIGGER_ROBOT_RESERVED);
        }
    }

    @Override
    public void addInternalSidedTriggers(Collection<ITriggerInternalSided> triggers, IStatementContainer container,
        Direction side) {}

    @Override
    public void addExternalTriggers(Collection<ITriggerExternal> triggers, Direction side, BlockEntity tile) {

    }
}
