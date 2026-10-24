/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.filler;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;

import buildcraftreborn.api.statements.IStatement;

public interface IFillerRegistry {
    void addPattern(IFillerPattern pattern);

    /** @return An {@link IFillerPattern} from its {@link IStatement#getUniqueTag()} */
    @Nullable
    IFillerPattern getPattern(String name);

    Collection<IFillerPattern> getPatterns();

    IFilledTemplate createFilledTemplate(BlockPos pos, BlockPos size);
}
