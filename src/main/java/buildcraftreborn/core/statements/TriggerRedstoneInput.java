/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.statements;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.ITriggerInternal;
import buildcraftreborn.api.statements.containers.IRedstoneStatementContainer;
import buildcraftreborn.api.statements.containers.ISidedStatementContainer;
import buildcraftreborn.core.BCCoreSprites;
import buildcraftreborn.core.BCCoreStatements;
import buildcraftreborn.lib.misc.LocaleUtil;

public class TriggerRedstoneInput extends BCStatement implements ITriggerInternal {
    public final boolean active;

    public TriggerRedstoneInput(boolean active) {
        super("buildcraft:redstone.input." + (active ? "active" : "inactive"),
            "buildcraft.redstone.input." + (active ? "active" : "inactive"));
        this.active = active;
    }

    @Override
    public ISprite getSprite() {
        return active ? BCCoreSprites.TRIGGER_REDSTONE_ACTIVE : BCCoreSprites.TRIGGER_REDSTONE_INACTIVE;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.redstone.input." + (active ? "active" : "inactive"));
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return index == 0 ? StatementParamGateSideOnly.ANY : null;
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        if (container instanceof IRedstoneStatementContainer redstone) {
            int level = redstone.getRedstoneInput(null);
            if (parameters.length > 0 && parameters[0] instanceof StatementParamGateSideOnly param
                && param.isSpecific && container instanceof ISidedStatementContainer sided) {
                level = redstone.getRedstoneInput(sided.getSide());
            }
            return active ? level > 0 : level == 0;
        }
        return false;
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_REDSTONE;
    }

    @Override
    public <T> T convertTo(Class<T> clazz) {
        T obj = super.convertTo(clazz);
        if (obj != null) {
            return obj;
        }
        if (active && clazz.isInstance(BCCoreStatements.ACTION_REDSTONE)) {
            return clazz.cast(BCCoreStatements.ACTION_REDSTONE);
        }
        return null;
    }
}
