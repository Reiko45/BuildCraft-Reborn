/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.filler;

import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;

import buildcraftreborn.api.filler.IFillerPattern;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.containers.IFillerStatementContainer;

import buildcraftreborn.lib.statement.FullStatement;

import buildcraftreborn.builders.snapshot.Template;

public class FillerUtil {
    public static Template.BuildingInfo createBuildingInfo(IFillerStatementContainer filler,
                                                           FullStatement<IFillerPattern> patternStatement,
                                                           IStatementParameter[] params,
                                                           boolean inverted) {
        Template.FilledTemplate filledTemplate = (Template.FilledTemplate) patternStatement.get().createTemplate(
            filler,
            params
        );
        if (filledTemplate == null) {
            return null;
        }
        if (inverted) {
            filledTemplate.getTemplate().invert();
        }
        return filledTemplate.getTemplate().new BuildingInfo(BlockPos.ZERO, Rotation.NONE);
    }
}
