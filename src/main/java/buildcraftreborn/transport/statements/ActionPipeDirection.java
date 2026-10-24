/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.statements;

import java.util.Locale;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;


import buildcraftreborn.api.statements.IActionInternal;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.misc.ColourUtil;
import buildcraftreborn.lib.misc.LocaleUtil;

import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.transport.BCTransportSprites;
import buildcraftreborn.transport.BCTransportStatements;

public class ActionPipeDirection extends BCStatement implements IActionInternal {
    public final Direction direction;

    public ActionPipeDirection(Direction direction) {
        super("buildcraft:pipe.dir." + direction.name().toLowerCase(Locale.ROOT), "buildcraft.pipe.dir." + direction.name().toLowerCase(Locale.ROOT));
        this.direction = direction;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.pipe.direction", ColourUtil.getTextFullTooltip(direction));
    }

    @Override
    public IStatement rotateLeft() {
        Direction face = direction.getAxis() == Axis.Y ? direction : direction.getClockWise();
        return BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()];
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}

    @Override
    public String toString() {
        return "ActionPipeDirection[" + direction + "]";
    }

    @Override
    public ISprite getSprite() {
        return BCTransportSprites.getPipeDirection(direction);
    }

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.ACTION_PIPE_DIRECTION;
    }
}
