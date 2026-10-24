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

public interface ITriggerProvider {
    void addInternalTriggers(Collection<ITriggerInternal> triggers, IStatementContainer container);

    void addInternalSidedTriggers(Collection<ITriggerInternalSided> triggers, IStatementContainer container, Direction side);

    /** Returns the list of triggers available to a gate next to the given block. */
    void addExternalTriggers(Collection<ITriggerExternal> triggers, Direction side, BlockEntity tile);
}
