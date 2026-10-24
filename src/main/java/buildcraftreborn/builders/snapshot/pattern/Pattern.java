/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot.pattern;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;

import buildcraftreborn.api.filler.FillerManager;
import buildcraftreborn.api.filler.IFillerPattern;
import buildcraftreborn.api.statements.IActionExternal;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.containers.IFillerStatementContainer;

import buildcraftreborn.lib.misc.LocaleUtil;

import buildcraftreborn.builders.BCBuildersStatements;
import buildcraftreborn.core.statements.BCStatement;

public abstract class Pattern extends BCStatement implements IFillerPattern, IActionExternal {
    private final String desc;

    public Pattern(String tag) {
        super("buildcraft:" + tag);
        desc = "fillerpattern." + tag;
        FillerManager.registry.addPattern(this);
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize(desc);
    }

    @Override
    public void actionActivate(BlockEntity target, Direction side, IStatementContainer source, IStatementParameter[] parameters) {
        if (source instanceof IFillerStatementContainer) {
            ((IFillerStatementContainer) source).setPattern(this, parameters);
        } else if (target instanceof IFillerStatementContainer) {
            ((IFillerStatementContainer) target).setPattern(this, parameters);
        }
    }

    @Override
    public IFillerPattern[] getPossible() {
        return BCBuildersStatements.PATTERNS;
    }
}
