/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IStripesHandlerItem {

    /** Called to handle the given {@link ItemStack} within the level. Note that the player's inventory will be empty,
     * except that the target stack will be set into its {@link InteractionHand#MAIN_HAND}. Any items left in the players
     * inventory will be returned back through the activator with
     * {@link IStripesActivator#sendItem(ItemStack, Direction)}
     * 
     * @param level
     * @param pos
     * @param direction
     * @param stack The {@link ItemStack} being used
     * @param player
     * @param activator
     * @return True if this used the item, false otherwise (note that this handler MUST NOT return false if it has
     *         changed the level in any way) */
    boolean handle(Level level, BlockPos pos, Direction direction, ItemStack stack, Player player, IStripesActivator activator);
}
