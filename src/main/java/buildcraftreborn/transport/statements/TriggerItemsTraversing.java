/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.statements;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.gates.IGate;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.ITriggerInternal;
import buildcraftreborn.api.statements.StatementParameterItemStack;
import buildcraftreborn.api.transport.pipe.PipeFlow;

import buildcraftreborn.lib.misc.LocaleUtil;

import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.transport.BCTransportSprites;
import buildcraftreborn.transport.pipe.flow.PipeFlowItems;

public class TriggerItemsTraversing extends BCStatement implements ITriggerInternal {

    public TriggerItemsTraversing() {
        super("buildcraft:pipe_contains_items");
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.pipe.containsItems");
    }

    @Override
    public ISprite getSprite() {
        return BCTransportSprites.TRIGGER_ITEMS_TRAVERSING;
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return StatementParameterItemStack.EMPTY;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        if (source instanceof IGate) {
            PipeFlow flow = ((IGate) source).getPipeHolder().getPipe().getFlow();
            if (flow instanceof PipeFlowItems) {
                PipeFlowItems itemFlow = (PipeFlowItems) flow;

                ItemStack filter = getParam(0, parameters, StatementParameterItemStack.EMPTY).getItemStack();
                return itemFlow.containsItemMatching(filter);
            }
        }
        return false;
    }
}
