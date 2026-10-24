/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.statements;

import java.util.Locale;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.statements.IActionExternal;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.tiles.IControllable;
import buildcraftreborn.api.tiles.IControllable.Mode;
import buildcraftreborn.api.tiles.TilesAPI;
import buildcraftreborn.core.BCCoreSprites;
import buildcraftreborn.core.BCCoreStatements;
import buildcraftreborn.lib.misc.CapUtil;
import buildcraftreborn.lib.misc.LocaleUtil;

public class ActionMachineControl extends BCStatement implements IActionExternal {
    public final Mode mode;

    public ActionMachineControl(Mode mode) {
        super("buildcraft:machine." + mode.name().toLowerCase(Locale.ROOT),
            "buildcraft.machine." + mode.name().toLowerCase(Locale.ROOT));
        this.mode = mode;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.machine." + mode.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public void actionActivate(BlockEntity target, Direction side, IStatementContainer source,
        IStatementParameter[] parameters) {
        IControllable controllable = CapUtil.getCapability(target, TilesAPI.CAP_CONTROLLABLE, side.getOpposite());
        if (controllable != null && controllable.acceptsControlMode(mode)) {
            controllable.setControlMode(mode);
        }
    }

    @Override
    public ISprite getSprite() {
        return BCCoreSprites.ACTION_MACHINE_CONTROL.get(mode);
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.ACTION_MACHINE_CONTROL;
    }
}
