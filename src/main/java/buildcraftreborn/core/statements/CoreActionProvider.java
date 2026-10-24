/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.statements;

import java.util.Collection;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraftreborn.api.statements.IActionExternal;
import buildcraftreborn.api.statements.IActionInternal;
import buildcraftreborn.api.statements.IActionInternalSided;
import buildcraftreborn.api.statements.IActionProvider;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.containers.IRedstoneStatementContainer;
import buildcraftreborn.api.tiles.IControllable;
import buildcraftreborn.api.tiles.TilesAPI;
import buildcraftreborn.core.BCCoreStatements;
import buildcraftreborn.lib.misc.CapUtil;

public enum CoreActionProvider implements IActionProvider {
    INSTANCE;

    @Override
    public void addInternalActions(Collection<IActionInternal> res, IStatementContainer container) {
        if (container instanceof IRedstoneStatementContainer) {
            res.add(BCCoreStatements.ACTION_REDSTONE);
        }
    }

    @Override
    public void addInternalSidedActions(Collection<IActionInternalSided> actions, IStatementContainer container,
        Direction side) {}

    @Override
    public void addExternalActions(Collection<IActionExternal> res, Direction side, BlockEntity tile) {
        IControllable controllable = CapUtil.getCapability(tile, TilesAPI.CAP_CONTROLLABLE, side.getOpposite());
        if (controllable != null) {
            for (ActionMachineControl action : BCCoreStatements.ACTION_MACHINE_CONTROL) {
                if (controllable.acceptsControlMode(action.mode)) {
                    res.add(action);
                }
            }
        }
    }
}
