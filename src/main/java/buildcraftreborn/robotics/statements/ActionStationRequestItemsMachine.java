/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.statements;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.statements.IActionInternal;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.robotics.BCRoboticsSprites;

/** Has robots deliver the items a neighbouring machine (such as a requester) asks for. */
public class ActionStationRequestItemsMachine extends BCStatement implements IActionInternal {

    public ActionStationRequestItemsMachine() {
        super("buildcraft:station.provide_machine_request");
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.station.provide_machine_request");
    }

    @Override
    public ISprite getSprite() {
        return BCRoboticsSprites.ACTION_STATION_MACHINE_REQUEST;
    }

    @Override
    public int minParameters() {
        return 0;
    }

    @Override
    public int maxParameters() {
        return 0;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return null;
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}
}
