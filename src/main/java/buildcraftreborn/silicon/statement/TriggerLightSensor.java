/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.statement;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;


import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.ITriggerInternalSided;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.misc.LocaleUtil;

import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.silicon.BCSiliconSprites;
import buildcraftreborn.silicon.BCSiliconStatements;

public class TriggerLightSensor extends BCStatement implements ITriggerInternalSided {
    private final boolean bright;

    public TriggerLightSensor(boolean bright) {
        super("buildcraft:light_" + (bright ? "bright" : "dark"));
        this.bright = bright;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.light." + (bright ? "bright" : "dark"));
    }

    @Override
    public boolean isTriggerActive(Direction side, IStatementContainer source, IStatementParameter[] parameters) {
        BlockEntity tile = source.getTile();
        BlockPos pos = tile.getBlockPos().relative(side);
        int light = tile.getLevel().getMaxLocalRawBrightness(pos);
        return (light < 8) ^ bright;
    }

    @Override
    public IStatement[] getPossible() {
        return BCSiliconStatements.TRIGGER_LIGHT;
    }

    @Override
    public ISprite getSprite() {
        return bright ? BCSiliconSprites.TRIGGER_LIGHT_HIGH : BCSiliconSprites.TRIGGER_LIGHT_LOW;
    }
}
