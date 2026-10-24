/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.list;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.lists.ListMatchHandler;
import buildcraftreborn.api.lists.ListRegistry;

/** "Same type" for items whose class is in {@link ListRegistry#itemClassAsType}. */
public class ListMatchHandlerClass extends ListMatchHandler {
    @Override
    public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
            Class<?> kl = stack.getItem().getClass();
            // 1.12 compared the item class with the target stack's class, which never matched
            return ListRegistry.itemClassAsType.contains(kl) && kl.equals(target.getItem().getClass());
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, ItemStack stack) {
        if (type == Type.TYPE) {
            return ListRegistry.itemClassAsType.contains(stack.getItem().getClass());
        }
        return false;
    }
}
