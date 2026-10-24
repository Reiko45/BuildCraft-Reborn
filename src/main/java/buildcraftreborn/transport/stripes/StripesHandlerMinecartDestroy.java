/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.stripes;

import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import buildcraftreborn.api.transport.IStripesActivator;
import buildcraftreborn.api.transport.IStripesHandlerBlock;

/** Breaks a minecart in front of the pipe, sending it (and anything it carries) back down the pipe. */
public enum StripesHandlerMinecartDestroy implements IStripesHandlerBlock {
    INSTANCE;

    @Override
    public boolean handle(Level level, BlockPos pos, Direction direction, Player player, IStripesActivator activator) {
        List<AbstractMinecart> minecarts = level.getEntitiesOfClass(AbstractMinecart.class, new AABB(pos));
        if (minecarts.isEmpty()) {
            return false;
        }
        Collections.shuffle(minecarts);
        AbstractMinecart cart = minecarts.get(0);
        if (cart instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack s = container.removeItemNoUpdate(i);
                if (!s.isEmpty()) {
                    activator.sendItem(s, direction);
                }
            }
        }
        ItemStack cartItem = cart.getPickResult();
        cart.discard();
        if (cartItem != null && !cartItem.isEmpty()) {
            activator.sendItem(cartItem, direction);
        }
        return true;
    }
}
