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
import buildcraftreborn.api.statements.IActionInternal;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.StatementSlot;
import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.robotics.BCRoboticsSprites;

/** Keeps the robots in the parameters away from the station ("forbid"), or lets only them use it ("force"). */
public class ActionStationForbidRobot extends BCStatement implements IActionInternal {
    private final boolean invert;

    public ActionStationForbidRobot(boolean invert) {
        super("buildcraft:station." + (invert ? "force" : "forbid") + "_robot");
        this.invert = invert;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.station." + (invert ? "force" : "forbid") + "_robot");
    }

    @Override
    public ISprite getSprite() {
        return invert ? BCRoboticsSprites.ACTION_STATION_ROBOT_MANDATORY
            : BCRoboticsSprites.ACTION_STATION_ROBOT_FORBIDDEN;
    }

    @Override
    public int minParameters() {
        return 1;
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterRobot();
    }

    public static boolean isForbidden(DockingStation station, EntityRobotBase robot) {
        for (StatementSlot s : station.getActiveActions()) {
            if (s.statement instanceof ActionStationForbidRobot forbid) {
                if (forbid.invert ^ isForbidden(s, robot)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isForbidden(StatementSlot slot, EntityRobotBase robot) {
        for (IStatementParameter p : slot.parameters) {
            if (p != null && StatementParameterRobot.matches(p, robot)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}
}
