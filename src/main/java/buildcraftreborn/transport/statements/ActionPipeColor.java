/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.statements;

import net.minecraft.world.item.DyeColor;


import buildcraftreborn.api.statements.IActionInternal;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.misc.ColourUtil;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.StringUtilBC;

import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.transport.BCTransportSprites;
import buildcraftreborn.transport.BCTransportStatements;

public class ActionPipeColor extends BCStatement implements IActionInternal {

    public final DyeColor color;

    public ActionPipeColor(DyeColor color) {
        super("buildcraft:pipe.color." + color.getName(), "buildcraft.pipe." + color.getName());
        this.color = color;
    }

    @Override
    public String getDescription() {
        return StringUtilBC.formatSafe(LocaleUtil.localize("gate.action.pipe.item.color"), ColourUtil.getTextFullTooltip(color));
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
        // Pipes listen for this -- we don't need to do anything here
    }

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.ACTION_PIPE_COLOUR;
    }

    @Override
    public ISprite getSprite() {
        return BCTransportSprites.ACTION_PIPE_COLOUR[color.ordinal()];
    }
}
