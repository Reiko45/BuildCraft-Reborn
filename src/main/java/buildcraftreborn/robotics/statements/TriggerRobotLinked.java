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

/** Active while one of the pipe's stations is a robot's home ("linked"), or taken by any robot ("reserved"). */
public class TriggerRobotLinked extends BCStatement implements ITriggerInternal {
    private final boolean reserved;

    public TriggerRobotLinked(boolean reserved) {
        super("buildcraft:robot." + (reserved ? "reserved" : "linked"));
        this.reserved = reserved;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.robot." + (reserved ? "reserved" : "linked"));
    }

    @Override
    public ISprite getSprite() {
        return reserved ? BCRoboticsSprites.TRIGGER_ROBOT_RESERVED : BCRoboticsSprites.TRIGGER_ROBOT_LINKED;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        for (DockingStation station : RobotUtils.getStations(container.getTile())) {
            if (station.isTaken() && (reserved || station.isMainStation())) {
                return true;
            }
        }

        return false;
    }
}
