/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.statements;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.ITriggerInternal;
import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.robotics.BCRoboticsSprites;
import buildcraftreborn.robotics.RobotUtils;

/** Active while a robot is docked at one of the pipe's stations. */
public class TriggerRobotInStation extends BCStatement implements ITriggerInternal {

    public TriggerRobotInStation() {
        super("buildcraft:robot.in.station");
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.robot.in.station");
    }

    @Override
    public ISprite getSprite() {
        return BCRoboticsSprites.TRIGGER_ROBOT_IN_STATION;
    }

    @Override
    public int maxParameters() {
        // 7.1: "TODO: Discuss whether we actually want to allow parameters here."
        return 0;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterRobot();
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        for (DockingStation station : RobotUtils.getStations(container.getTile())) {
            EntityRobotBase robot = station.robotTaking();
            if (robot != null && robot.getDockingStation() == station) {
                if (parameters.length > 0 && parameters[0] != null && !parameters[0].getItemStack().isEmpty()) {
                    if (StatementParameterRobot.matches(parameters[0], robot)) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }

        return false;
    }
}
