/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.filler;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.containers.IFillerStatementContainer;

/** A type of statement that is used for filler patterns. */
public interface IFillerPattern extends IStatement {
    /** @param filler The filler to create the pattern for.
     *            <br>
     *            NOTE: This method should never be called when {@link IFillerStatementContainer#hasBox()} returns
     *            false
     * @return The template to fill (should be created with
     * {@link IFillerRegistry#createFilledTemplate(BlockPos, BlockPos)}),
     * or {@code null} if this shouldn't make a template for the given filer. */
    @Nullable
    IFilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params);

    @Override
    IFillerPattern[] getPossible();

    /** Note that this sprite *must* be stitched to the texture atlas, as it is drawn on the side of the filler
     * block. */
    @Override
    ISprite getSprite();
}
