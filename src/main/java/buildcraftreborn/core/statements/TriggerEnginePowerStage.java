/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.statements;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.enums.EnumPowerStage;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.ITriggerExternal;
import buildcraftreborn.core.BCCoreSprites;
import buildcraftreborn.core.BCCoreStatements;
import buildcraftreborn.lib.engine.TileEngineBase_BC8;
import buildcraftreborn.lib.misc.LocaleUtil;

public class TriggerEnginePowerStage extends BCStatement implements ITriggerExternal {
    public final EnumPowerStage stage;

    public TriggerEnginePowerStage(EnumPowerStage stage) {
        super("buildcraft:engine.stage." + stage.getModelName());
        this.stage = stage;
    }

    public static boolean isTriggeringTile(BlockEntity tile) {
        return tile instanceof TileEngineBase_BC8;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.engine." + stage.getModelName());
    }

    @Override
    public ISprite getSprite() {
        return BCCoreSprites.TRIGGER_POWER_STAGE.get(stage);
    }

    @Override
    public boolean isTriggerActive(BlockEntity target, Direction side, IStatementContainer source,
        IStatementParameter[] parameters) {
        return target instanceof TileEngineBase_BC8 engine && engine.getPowerStage() == stage;
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_POWER_STAGES;
    }
}
