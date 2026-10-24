/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraftreborn.api.statements.IActionExternal;
import buildcraftreborn.api.statements.IActionInternal;
import buildcraftreborn.api.statements.IActionInternalSided;
import buildcraftreborn.api.statements.IActionProvider;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.containers.IFillerStatementContainer;

/** Gives gates next to a filler its patterns as actions. */
public enum BCBuildersActionProvider implements IActionProvider {
    INSTANCE;

    @Override
    public void addInternalActions(Collection<IActionInternal> res, IStatementContainer container) {}

    @Override
    public void addInternalSidedActions(Collection<IActionInternalSided> actions, IStatementContainer container,
        Direction side) {}

    @Override
    public void addExternalActions(Collection<IActionExternal> res, Direction side, BlockEntity tile) {
        if (tile instanceof IFillerStatementContainer) {
            Collections.addAll(res, BCBuildersStatements.PATTERNS);
        }
    }
}
