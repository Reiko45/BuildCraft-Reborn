/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IStripesHandlerBlock {

    /** @param level
     * @param pos
     * @param direction
     * @param player
     * @param activator
     * @return True if this broke a block, false otherwise (note that this handler MUST NOT return false if it has
     *         changed the level in any way) */
    boolean handle(Level level, BlockPos pos, Direction direction, Player player, IStripesActivator activator);
}
