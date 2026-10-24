/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.template;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraftreborn.api.core.EnumHandlerPriority;

public interface ITemplateRegistry {
    /** Adds a handler with a {@link EnumHandlerPriority} of {@linkplain EnumHandlerPriority#NORMAL} */
    default void addHandler(ITemplateHandler handler) {
        addHandler(handler, EnumHandlerPriority.NORMAL);
    }

    void addHandler(ITemplateHandler handler, EnumHandlerPriority priority);

    boolean handle(Level level, BlockPos pos, Player player, ItemStack stack);
}
