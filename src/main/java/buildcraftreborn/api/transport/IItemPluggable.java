/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;

import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pluggable.PipePluggable;

/** Designates an item that can be placed onto a pipe as a {@link PipePluggable}. */
public interface IItemPluggable {
    /** Called when this item is placed onto a pipe holder. This can return null if this item does not make a valid
     * pluggable. Note that if you return a non-null pluggable then it will *definitely* be added to the pipe, and you
     * are responsible for making all the effects yourself (like the sound effect).
     * 
     * @param stack The stack that holds this item
     * @param holder The pipe holder
     * @param side The side that the pluggable should be placed on
     * @return A pluggable to place onto the pipe */
    @Nullable
    PipePluggable onPlace(ItemStack stack, IPipeHolder holder, Direction side, Player player,
        InteractionHand hand);
}
