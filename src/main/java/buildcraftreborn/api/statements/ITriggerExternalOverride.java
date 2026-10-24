/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.statements;

import net.minecraft.core.Direction;

/** This interface can be used by tiles to override external trigger behaviour.
 *
 * Please use wisely. */
public interface ITriggerExternalOverride {
    enum Result {
        TRUE,
        FALSE,
        IGNORE
    }

    Result override(Direction side, IStatementContainer source, ITriggerExternal trigger, IStatementParameter[] parameters);
}
