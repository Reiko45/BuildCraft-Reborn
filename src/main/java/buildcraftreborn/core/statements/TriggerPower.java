/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.statements;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.mj.IMjReadable;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.ITriggerExternal;
import buildcraftreborn.api.statements.ITriggerInternal;
import buildcraftreborn.core.BCCoreSprites;
import buildcraftreborn.core.BCCoreStatements;
import buildcraftreborn.lib.misc.CapUtil;
import buildcraftreborn.lib.misc.LocaleUtil;

public class TriggerPower extends BCStatement implements ITriggerInternal, ITriggerExternal {
    private final boolean high;

    public TriggerPower(boolean high) {
        super("buildcraft:energyStored" + (high ? "high" : "low"));
        this.high = high;
    }

    @Override
    public ISprite getSprite() {
        return high ? BCCoreSprites.TRIGGER_POWER_HIGH : BCCoreSprites.TRIGGER_POWER_LOW;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.machine.energyStored." + (high ? "high" : "low"));
    }

    public boolean isTriggeredMjConnector(@Nullable IMjReadable readable) {
        if (readable == null) {
            return false;
        }
        long stored = readable.getStored();
        long max = readable.getCapacity();
        if (max > 0) {
            double level = stored / (double) max;
            return high ? level > 0.95 : level < 0.05;
        }
        return false;
    }

    public static boolean isTriggeringTile(BlockEntity tile) {
        return isTriggeringTile(tile, null);
    }

    public static boolean isTriggeringTile(BlockEntity tile, @Nullable Direction face) {
        return CapUtil.hasCapability(tile, MjAPI.CAP_READABLE, face);
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        return isTriggeredMjConnector(CapUtil.getCapability(source.getTile(), MjAPI.CAP_READABLE, null));
    }

    @Override
    public boolean isTriggerActive(BlockEntity target, Direction side, IStatementContainer source,
        IStatementParameter[] parameters) {
        return isTriggeredMjConnector(CapUtil.getCapability(target, MjAPI.CAP_READABLE, side.getOpposite()));
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_POWER;
    }
}
