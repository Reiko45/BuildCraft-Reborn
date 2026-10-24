/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.gates;

import java.util.List;

import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.StatementSlot;
import buildcraftreborn.api.statements.containers.ISidedStatementContainer;
import buildcraftreborn.api.transport.pipe.IPipeHolder;

public interface IGate extends ISidedStatementContainer {

    IPipeHolder getPipeHolder();

    List<IStatement> getTriggers();

    List<IStatement> getActions();

    List<StatementSlot> getActiveActions();

    List<IStatementParameter> getTriggerParameters(int slot);

    List<IStatementParameter> getActionParameters(int slot);
}
