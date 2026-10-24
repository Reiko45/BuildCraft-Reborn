/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.list;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.lists.ListMatchHandler;

/** "Same type" for food: any food matches any other. (1.12 did this by registering {@code ItemFood} as a type class;
 * food is a data component in 1.21, not an item class.) */
public class ListMatchHandlerFood extends ListMatchHandler {
    @Override
    public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
        return type == Type.TYPE && isFood(stack) && isFood(target);
    }

    @Override
    public boolean isValidSource(Type type, ItemStack stack) {
        return type == Type.TYPE && isFood(stack);
    }

    private static boolean isFood(ItemStack stack) {
        return stack.has(DataComponents.FOOD);
    }
}
