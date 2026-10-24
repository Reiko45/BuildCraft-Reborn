/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.statement;

import net.minecraft.core.Direction;

import buildcraftreborn.api.gates.IGate;
import buildcraftreborn.api.statements.IActionInternalSided;
import buildcraftreborn.api.statements.IActionSingle;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pluggable.PipePluggable;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.misc.LocaleUtil;

import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.silicon.BCSiliconSprites;
import buildcraftreborn.silicon.BCSiliconStatements;
import buildcraftreborn.silicon.plug.PluggablePulsar;

public class ActionPowerPulsar extends BCStatement implements IActionInternalSided, IActionSingle {

    public final boolean constant;

    public ActionPowerPulsar(boolean constant) {
        super("buildcraft:pulsar." + (constant ? "constant" : "single"),
            "buildcraft.pulsar.constant" + (constant ? "constant" : "single"));
        this.constant = constant;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize(constant ? "gate.action.pulsar.constant" : "gate.action.pulsar.single");
    }

    @Override
    public void actionActivate(Direction side, IStatementContainer source, IStatementParameter[] parameters) {
        if (source instanceof IGate) {
            IGate gate = (IGate) source;
            IPipeHolder pipe = gate.getPipeHolder();
            PipePluggable plug = pipe.getPluggable(side);
            if (plug instanceof PluggablePulsar) {
                PluggablePulsar pulsar = (PluggablePulsar) plug;
                if (constant) {
                    pulsar.enablePulsar();
                } else {
                    pulsar.addSinglePulse();
                }
            }
        }
    }

    @Override
    public boolean singleActionTick() {
        return !constant;
    }

    @Override
    public ISprite getSprite() {
        return constant ? BCSiliconSprites.ACTION_PULSAR_CONSTANT : BCSiliconSprites.ACTION_PULSAR_SINGLE;
    }

    @Override
    public IStatement[] getPossible() {
        return BCSiliconStatements.ACTION_PULSAR;
    }
}
