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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import buildcraftreborn.api.transport.IStripesActivator;
import buildcraftreborn.api.transport.IStripesHandlerItem;

/** Uses the item on a random living entity in front of the pipe (shearing sheep, milking cows, dyeing...). */
public enum StripesHandlerEntityInteract implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(Level level, BlockPos pos, Direction direction, ItemStack stack, Player player,
        IStripesActivator activator) {
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos.relative(direction)));
        Collections.shuffle(entities);
        for (LivingEntity entity : entities) {
            if (player.interactOn(entity, InteractionHand.MAIN_HAND).consumesAction()) {
                return true;
            }
        }
        return false;
    }
}
