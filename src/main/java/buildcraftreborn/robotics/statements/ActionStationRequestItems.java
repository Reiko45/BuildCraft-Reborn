/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.statements;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.core.statements.StatementParameterItemStackExact;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.robotics.BCRoboticsSprites;

/** Asks delivery robots for the stacks in the parameters, delivered into the station's item pipe. */
public class ActionStationRequestItems extends ActionStationInputItems {

    public ActionStationRequestItems() {
        super("buildcraft:station.request_items");
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.station.request_items");
    }

    @Override
    public ISprite getSprite() {
        return BCRoboticsSprites.ACTION_STATION_REQUEST_ITEMS;
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public int minParameters() {
        return 1;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStackExact();
    }
}
