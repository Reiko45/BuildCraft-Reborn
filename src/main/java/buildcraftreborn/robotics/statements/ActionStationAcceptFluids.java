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
import buildcraftreborn.api.statements.StatementParameterItemStack;
import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.robotics.BCRoboticsSprites;

/** Lets robots unload fluids (those in the parameters, or any) into the station's fluid pipe. */
public class ActionStationAcceptFluids extends BCStatement implements IActionInternal {

    public ActionStationAcceptFluids() {
        super("buildcraft:station.accept_fluids");
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.station.accept_fluids");
    }

    @Override
    public ISprite getSprite() {
        return BCRoboticsSprites.ACTION_STATION_ACCEPT_FLUIDS;
    }

    @Override
    public int minParameters() {
        return 0;
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}
}
