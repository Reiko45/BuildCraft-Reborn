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
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.ITriggerExternal;
import buildcraftreborn.api.tiles.IHasWork;
import buildcraftreborn.api.tiles.TilesAPI;
import buildcraftreborn.core.BCCoreSprites;
import buildcraftreborn.core.BCCoreStatements;
import buildcraftreborn.lib.misc.CapUtil;
import buildcraftreborn.lib.misc.LocaleUtil;

public class TriggerMachine extends BCStatement implements ITriggerExternal {
    public final boolean active;

    public TriggerMachine(boolean active) {
        super("buildcraft:work." + (active ? "scheduled" : "done"), "buildcraft.work." + (active ? "scheduled" : "done"));
        this.active = active;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.machine." + (active ? "scheduled" : "done"));
    }

    @Override
    public ISprite getSprite() {
        return active ? BCCoreSprites.TRIGGER_MACHINE_ACTIVE : BCCoreSprites.TRIGGER_MACHINE_INACTIVE;
    }

    @Override
    public boolean isTriggerActive(BlockEntity tile, Direction side, IStatementContainer container,
        IStatementParameter[] parameters) {
        IHasWork hasWork = CapUtil.getCapability(tile, TilesAPI.CAP_HAS_WORK, side.getOpposite());
        return hasWork != null && hasWork.hasWork() == active;
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_MACHINE;
    }
}
