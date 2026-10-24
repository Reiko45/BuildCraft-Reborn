/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.statements.containers;

import net.minecraft.core.Direction;

import buildcraftreborn.api.statements.IStatementContainer;

/** Created by asie on 3/14/15. */
public interface ISidedStatementContainer extends IStatementContainer {
    Direction getSide();
}
