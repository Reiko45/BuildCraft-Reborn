/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.statements;

import org.jetbrains.annotations.Nullable;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.gates.IGate;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.ITriggerInternal;
import buildcraftreborn.api.transport.pipe.PipeFlow;

import buildcraftreborn.lib.misc.LocaleUtil;

import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.transport.BCTransportSprites;
import buildcraftreborn.transport.pipe.flow.PipeFlowPower;
import buildcraftreborn.transport.pipe.flow.PipeFlowRedstoneFlux;

public class TriggerPowerRequested extends BCStatement implements ITriggerInternal {

    public TriggerPowerRequested() {
        super("buildcraft:powerRequested");
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        if (!(source instanceof IGate)) {
            return false;
        }
        PipeFlow f = ((IGate) source).getPipeHolder().getPipe().getFlow();
        if (f instanceof PipeFlowPower) {
            return ((PipeFlowPower) f).getPowerRequested(null) > 0;
        } else if (f instanceof PipeFlowRedstoneFlux) {
            return ((PipeFlowRedstoneFlux) f).getPowerRequested(null) > 0;
        } else {
            return false;
        }
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.pipe.requestsEnergy");
    }

    @Nullable
    @Override
    public ISprite getSprite() {
        return BCTransportSprites.TRIGGER_POWER_REQUESTED;
    }

}
