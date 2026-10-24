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
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraftreborn.api.core.EnumHandlerPriority;

public interface IStripesRegistry {
    /** Adds a handler with a {@link EnumHandlerPriority} of {@linkplain EnumHandlerPriority#NORMAL} */
    default void addHandler(IStripesHandlerItem handler) {
        addHandler(handler, EnumHandlerPriority.NORMAL);
    }

    void addHandler(IStripesHandlerItem handler, EnumHandlerPriority priority);

    /** Adds a handler with a {@link EnumHandlerPriority} of {@linkplain EnumHandlerPriority#NORMAL NORMAL} */
    default void addHandler(IStripesHandlerBlock handler) {
        addHandler(handler, EnumHandlerPriority.NORMAL);
    }

    void addHandler(IStripesHandlerBlock handler, EnumHandlerPriority priority);

    /** @param pos The position of the stripes pipe.
     * @return True if a handler handled the itemstack, false otherwise (and so nothing has been done) */
    boolean handleItem(Level level,
                       BlockPos pos,
                       Direction direction,
                       ItemStack stack,
                       Player player,
                       IStripesActivator activator);

    /** @return True if a handler broke a block, false otherwise (and so nothing has been done) */
    boolean handleBlock(Level level,
                        BlockPos pos,
                        Direction direction,
                        Player player,
                        IStripesActivator activator);
}
