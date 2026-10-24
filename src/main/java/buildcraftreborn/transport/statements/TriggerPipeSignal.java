/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.statements;

import java.util.Locale;

import net.minecraft.world.item.DyeColor;

import buildcraftreborn.api.gates.IGate;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.ITriggerInternal;
import buildcraftreborn.api.transport.IWireManager;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.misc.ColourUtil;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.StringUtilBC;

import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.transport.BCTransportSprites;
import buildcraftreborn.transport.BCTransportStatements;

public class TriggerPipeSignal extends BCStatement implements ITriggerInternal {

    private final boolean active;
    private final DyeColor colour;

    public TriggerPipeSignal(boolean active, DyeColor colour) {
        super(
            "buildcraft:pipe.wire.input." + colour.getName().toLowerCase(Locale.ROOT)
                + (active ? ".active" : ".inactive"), //
            "buildcraft.pipe.wire.input." + colour.getName().toLowerCase(Locale.ROOT)
                + (active ? ".active" : ".inactive"));

        this.active = active;
        this.colour = colour;
    }

    public static boolean doesGateHaveColour(IGate gate, DyeColor c) {
        // FIXME: replace with a check to wires.hasWire(colour)!
        return gate.getPipeHolder().getWireManager().hasPartOfColor(c);
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public String getDescription() {
        return StringUtilBC.formatSafe(LocaleUtil.localize("gate.trigger.pipe.wire." + (active ? "active" : "inactive")), ColourUtil.getTextFullTooltip(colour));
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        if (!(container instanceof IGate)) {
            return false;
        }

        IGate gate = (IGate) container;
        IWireManager wires = gate.getPipeHolder().getWireManager();

        if (this.active != wires.isAnyPowered(this.colour)) {
            return false;
        }

        for (IStatementParameter param : parameters) {
            if (param != null && param instanceof TriggerParameterSignal) {
                TriggerParameterSignal signal = (TriggerParameterSignal) param;
                if (signal.colour == null) {
                    continue;
                }
                if (signal.active != wires.isAnyPowered(signal.colour)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return TriggerParameterSignal.EMPTY;
    }

    @Override
    public ISprite getSprite() {
        return BCTransportSprites.getPipeSignal(active, colour);
    }

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.TRIGGER_PIPE_SIGNAL;
    }
}
