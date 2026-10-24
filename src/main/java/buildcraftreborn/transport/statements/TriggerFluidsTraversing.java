/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.statements;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.gates.IGate;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.ITriggerInternal;
import buildcraftreborn.api.transport.pipe.PipeFlow;

import buildcraftreborn.lib.misc.LocaleUtil;

import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.transport.BCTransportSprites;
import buildcraftreborn.transport.pipe.flow.PipeFlowFluids;

public class TriggerFluidsTraversing extends BCStatement implements ITriggerInternal {

    public TriggerFluidsTraversing() {
        super("buildcraft:pipe_contains_fluids");
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.pipe.containsFluids");
    }

    @Override
    public ISprite getSprite() {
        return BCTransportSprites.TRIGGER_FLUIDS_TRAVERSING;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        if (source instanceof IGate) {
            PipeFlow flow = ((IGate) source).getPipeHolder().getPipe().getFlow();
            if (flow instanceof PipeFlowFluids) {
                return ((PipeFlowFluids) flow).doesContainFluid();
            }
        }
        return false;
    }
}
