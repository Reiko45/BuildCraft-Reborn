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
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.ITriggerInternal;
import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.robotics.BCRoboticsSprites;
import buildcraftreborn.robotics.RobotUtils;
import buildcraftreborn.robotics.entity.EntityRobot;

/** Active while a robot of one of the pipe's stations is asleep (or shut down). */
public class TriggerRobotSleep extends BCStatement implements ITriggerInternal {

    public TriggerRobotSleep() {
        super("buildcraft:robot.sleep");
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.robot.sleep");
    }

    @Override
    public ISprite getSprite() {
        return BCRoboticsSprites.TRIGGER_ROBOT_SLEEP;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        for (DockingStation station : RobotUtils.getStations(container.getTile())) {
            if (station.robotTaking() instanceof EntityRobot robot && robot.isAsleep()) {
                return true;
            }
        }

        return false;
    }
}
