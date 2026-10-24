/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.gate;

import java.util.List;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.statements.IStatement;

import buildcraftreborn.lib.gui.ISimpleDrawable;
import buildcraftreborn.lib.misc.ColourUtil;
import buildcraftreborn.lib.statement.StatementContext;

public class GateContext<T extends IStatement> implements StatementContext<T> {

    public final List<GateGroup<T>> groups;

    public GateContext(List<GateGroup<T>> groups) {
        this.groups = groups;
    }

    @Override
    public List<? extends StatementGroup<T>> getAllPossible() {
        return groups;
    }

    public static class GateGroup<T extends IStatement> implements StatementGroup<T> {
        public final EnumPipePart part;
        public final List<T> statements;

        public GateGroup(EnumPipePart part, List<T> statements) {
            this.part = part;
            this.statements = statements;
        }

        @Override
        public List<T> getValues() {
            return statements;
        }

        @Override
        public ISimpleDrawable getSourceIcon() {
            return null;
        }

        @Override
        public int getLedgerColour() {
            if (part == EnumPipePart.CENTER) {
                return 0;
            }
            return ColourUtil.getColourForSide(part.face);
        }
    }
}
