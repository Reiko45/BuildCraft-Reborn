/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.statements;

import net.minecraft.core.Direction;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.statements.IActionInternal;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.containers.IRedstoneStatementContainer;
import buildcraftreborn.api.statements.containers.ISidedStatementContainer;
import buildcraftreborn.core.BCCoreSprites;
import buildcraftreborn.core.BCCoreStatements;
import buildcraftreborn.lib.misc.LocaleUtil;

public class ActionRedstoneOutput extends BCStatement implements IActionInternal {
    protected ActionRedstoneOutput(String s) {
        // Used by fader output
        super(s);
    }

    public ActionRedstoneOutput() {
        super("buildcraft:redstone.output", "buildcraft.redstone.output");
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.redstone.signal");
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return index == 0 ? StatementParamGateSideOnly.ANY : null;
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    protected boolean isSideOnly(IStatementParameter[] parameters) {
        if (parameters != null && parameters.length >= (getRGSOSlot() + 1)
            && parameters[getRGSOSlot()] instanceof StatementParamGateSideOnly param) {
            return param.isSpecific;
        }
        return false;
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
        if (source instanceof IRedstoneStatementContainer redstone) {
            Direction side = null;
            if (source instanceof ISidedStatementContainer sided && isSideOnly(parameters)) {
                side = sided.getSide();
            }
            redstone.setRedstoneOutput(side, getSignalLevel(parameters));
        }
    }

    protected int getRGSOSlot() {
        return 0;
    }

    protected int getSignalLevel(IStatementParameter[] parameters) {
        return 15;
    }

    @Override
    public ISprite getSprite() {
        return BCCoreSprites.ACTION_REDSTONE;
    }

    @Override
    public <T> T convertTo(Class<T> clazz) {
        T obj = super.convertTo(clazz);
        if (obj != null) {
            return obj;
        }
        if (clazz.isInstance(BCCoreStatements.TRIGGER_REDSTONE_ACTIVE)) {
            return clazz.cast(BCCoreStatements.TRIGGER_REDSTONE_ACTIVE);
        }
        return null;
    }
}
