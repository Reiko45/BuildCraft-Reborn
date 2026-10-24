/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.statements;


import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.statements.IActionInternal;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pipe.PipeApi.PowerTransferInfo;
import buildcraftreborn.api.transport.pipe.PipeApi.RedstoneFluxTransferInfo;
import buildcraftreborn.api.transport.pipe.PipeDefinition;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.misc.LocaleUtil;

import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.transport.BCTransportPipes;
import buildcraftreborn.transport.BCTransportSprites;
import buildcraftreborn.transport.BCTransportStatements;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourLimiter;

public abstract class ActionPowerLimit extends BCStatement implements IActionInternal {

    public final PipeDefinition pipe;

    /** Behaves identically to {@link PipeBehaviourLimiter} */
    public final int limitShift;

    public ActionPowerLimit(PipeDefinition pipe, int limitShift, String... uniqueTags) {
        super(uniqueTags);
        this.pipe = pipe;
        this.limitShift = limitShift;
    }

    public ActionPowerLimit(String suffix, PipeDefinition pipe, int limitShift) {
        this(pipe, limitShift, "buildcraft:pipe.power_limit." + suffix + "_s" + limitShift);
    }

    protected boolean isRf() {
        return false;
    }

    @Override
    public String getDescription() {
        if (isRf()) {
            RedstoneFluxTransferInfo pipeInfo = PipeApi.rfTransferData.get(pipe);
            final Object max;
            if (limitShift == PipeBehaviourLimiter.MAX_SHIFT) {
                max = 0;
            } else if (pipeInfo == null) {
                max = "??[INVALID_PIPE]??";
            } else {
                max = pipeInfo.transferPerTick >> limitShift;
            }
            return LocaleUtil.localize("gate.action.pipe.rf_limit", max);
        }
        PowerTransferInfo pipeInfo = PipeApi.powerTransferData.get(pipe);
        final Object max;
        if (limitShift == PipeBehaviourLimiter.MAX_SHIFT) {
            max = 0;
        } else if (pipeInfo == null) {
            max = "??[INVALID_PIPE]??";
        } else {
            max = (int) ((pipeInfo.transferPerTick >> limitShift) / MjAPI.MJ);
        }
        return LocaleUtil.localize("gate.action.pipe.power_limit", max);
    }

    @Override
    public ISprite getSprite() {
        ISprite[] sprites;
        if (!isRf()) {
            sprites = BCTransportSprites.POWER_LIMIT;
        } else {
            sprites = BCTransportSprites.POWER_LIMIT_RF;
        }
        return sprites[limitShift];
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
        // The behaviour handles this
    }

    @Override
    public abstract IStatement[] getPossible();

    public static class ActionIronPowerLimit extends ActionPowerLimit {

        public ActionIronPowerLimit(int limitShift) {
            super("iron", BCTransportPipes.ironPower, limitShift);
        }

        @Override
        public IStatement[] getPossible() {
            return BCTransportStatements.ACTION_IRON_POWER_LIMIT;
        }
    }

    public static class ActionDiamondPowerLimit extends ActionPowerLimit {

        public ActionDiamondPowerLimit(int limitShift) {
            super("diamond", BCTransportPipes.diamondPower, limitShift);
        }

        @Override
        public IStatement[] getPossible() {
            return BCTransportStatements.ACTION_DIAMOND_POWER_LIMIT;
        }
    }

    public static class ActionIronRfLimit extends ActionPowerLimit {

        public ActionIronRfLimit(int limitShift) {
            super("iron_rf", BCTransportPipes.ironRf, limitShift);
        }

        @Override
        public IStatement[] getPossible() {
            return BCTransportStatements.ACTION_IRON_RF_LIMIT;
        }

        @Override
        protected boolean isRf() {
            return true;
        }
    }

    public static class ActionDiamondRfLimit extends ActionPowerLimit {

        public ActionDiamondRfLimit(int limitShift) {
            super("diamond_rf", BCTransportPipes.diamondRf, limitShift);
        }

        @Override
        public IStatement[] getPossible() {
            return BCTransportStatements.ACTION_DIAMOND_RF_LIMIT;
        }

        @Override
        protected boolean isRf() {
            return true;
        }
    }
}
