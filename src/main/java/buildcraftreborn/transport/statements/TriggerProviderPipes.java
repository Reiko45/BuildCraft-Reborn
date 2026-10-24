/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.statements;

import java.util.Collection;


import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;

import buildcraftreborn.api.gates.IGate;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.ITriggerExternal;
import buildcraftreborn.api.statements.ITriggerInternal;
import buildcraftreborn.api.statements.ITriggerInternalSided;
import buildcraftreborn.api.statements.ITriggerProvider;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pipe.PipeEventStatement;

import buildcraftreborn.lib.misc.ColourUtil;

import buildcraftreborn.transport.BCTransportStatements;
import buildcraftreborn.transport.pipe.flow.PipeFlowPower;
import buildcraftreborn.transport.pipe.flow.PipeFlowRedstoneFlux;

public enum TriggerProviderPipes implements ITriggerProvider {
    INSTANCE;

    @Override
    public void addInternalTriggers(Collection<ITriggerInternal> triggers, IStatementContainer container) {
        if (container instanceof IGate) {
            IGate gate = (IGate) container;
            IPipeHolder holder = gate.getPipeHolder();
            holder.fireEvent(new PipeEventStatement.AddTriggerInternal(holder, triggers));

            for (DyeColor colour : ColourUtil.COLOURS) {
                if (TriggerPipeSignal.doesGateHaveColour(gate, colour)) {
                    triggers.add(BCTransportStatements.TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 0]);
                    triggers.add(BCTransportStatements.TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 1]);
                }
            }

            if (holder.getPipe().getFlow() instanceof PipeFlowPower) {
                triggers.add(BCTransportStatements.TRIGGER_POWER_REQUESTED);
            }

            if (holder.getPipe().getFlow() instanceof PipeFlowRedstoneFlux) {
                triggers.add(BCTransportStatements.TRIGGER_POWER_REQUESTED);
            }
        }
    }

    @Override
    public void addInternalSidedTriggers(Collection<ITriggerInternalSided> triggers, IStatementContainer container, Direction side) {
        if (container instanceof IGate) {
            IGate gate = (IGate) container;
            IPipeHolder holder = gate.getPipeHolder();
            holder.fireEvent(new PipeEventStatement.AddTriggerInternalSided(holder, triggers, side));
        }
    }

    @Override
    public void addExternalTriggers(Collection<ITriggerExternal> triggers, Direction side, BlockEntity tile) {

    }
}
