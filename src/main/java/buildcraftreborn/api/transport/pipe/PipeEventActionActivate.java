/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pipe;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.statements.IAction;
import buildcraftreborn.api.statements.IStatementParameter;

public class PipeEventActionActivate extends PipeEvent {
    public final IAction action;
    public final IStatementParameter[] params;
    public final EnumPipePart part;

    public PipeEventActionActivate(IPipeHolder holder, IAction action, IStatementParameter[] params, EnumPipePart part) {
        super(holder);
        this.action = action;
        this.params = params;
        this.part = part;
    }
}
