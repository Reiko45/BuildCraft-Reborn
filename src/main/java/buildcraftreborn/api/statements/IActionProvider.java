/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.statements;

import java.util.Collection;


import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;

public interface IActionProvider {
    void addInternalActions(Collection<IActionInternal> actions, IStatementContainer container);

    void addInternalSidedActions(Collection<IActionInternalSided> actions, IStatementContainer container, Direction side);

    void addExternalActions(Collection<IActionExternal> actions, Direction side, BlockEntity tile);
}
